package tech.exitzero.blog.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tech.exitzero.blog.backend.api.TagApi;
import tech.exitzero.blog.backend.domain.BlogTag;
import tech.exitzero.blog.backend.domain.SiteEventType;
import tech.exitzero.blog.backend.repository.BlogTagRepository;
import tech.exitzero.blog.backend.support.ApiException;
import tech.exitzero.blog.backend.support.SlugUtils;

import java.util.List;

@Service
public class TagService {

    private final BlogTagRepository tagRepository;
    private final BlogMapper blogMapper;
    private final TimelineService timelineService;

    public TagService(BlogTagRepository tagRepository, BlogMapper blogMapper, TimelineService timelineService) {
        this.tagRepository = tagRepository;
        this.blogMapper = blogMapper;
        this.timelineService = timelineService;
    }

    @Transactional(readOnly = true)
    public List<TagApi.TagResponse> list() {
        return tagRepository.findAllByOrderByNameAsc().stream()
            .map(blogMapper::toTagResponse)
            .toList();
    }

    @Transactional
    public TagApi.TagResponse create(TagApi.TagUpsertRequest request) {
        BlogTag tag = new BlogTag();
        applyRequest(tag, request, null);
        BlogTag saved = tagRepository.save(tag);
        timelineService.recordSiteEvent(SiteEventType.SYSTEM, "新增标签", "标签「" + saved.getName() + "」已加入内容维度。");
        return blogMapper.toTagResponse(saved);
    }

    @Transactional
    public TagApi.TagResponse update(Long id, TagApi.TagUpsertRequest request) {
        BlogTag tag = tagRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "标签不存在: " + id));
        applyRequest(tag, request, id);
        BlogTag saved = tagRepository.save(tag);
        timelineService.recordSiteEvent(SiteEventType.SYSTEM, "更新标签", "标签「" + saved.getName() + "」已更新。");
        return blogMapper.toTagResponse(saved);
    }

    private void applyRequest(BlogTag tag, TagApi.TagUpsertRequest request, Long selfId) {
        tag.setName(request.name().trim());
        tag.setSlug(generateUniqueSlug(request.slug(), request.name(), selfId));
        tag.setColor(StringUtils.hasText(request.color()) ? request.color().trim() : "#3a7cff");
    }

    private String generateUniqueSlug(String requestSlug, String fallbackName, Long selfId) {
        String baseSlug = SlugUtils.toSlug(StringUtils.hasText(requestSlug) ? requestSlug : fallbackName);
        String slug = baseSlug;
        int suffix = 2;
        while (slugExists(slug, selfId)) {
            slug = baseSlug + "-" + suffix++;
        }
        return slug;
    }

    private boolean slugExists(String slug, Long selfId) {
        return selfId == null
            ? tagRepository.existsBySlug(slug)
            : tagRepository.existsBySlugAndIdNot(slug, selfId);
    }
}
