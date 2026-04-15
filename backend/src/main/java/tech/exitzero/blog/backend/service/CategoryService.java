package tech.exitzero.blog.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tech.exitzero.blog.backend.api.CategoryApi;
import tech.exitzero.blog.backend.domain.BlogCategory;
import tech.exitzero.blog.backend.domain.SiteEventType;
import tech.exitzero.blog.backend.repository.BlogCategoryRepository;
import tech.exitzero.blog.backend.support.ApiException;
import tech.exitzero.blog.backend.support.SlugUtils;

import java.util.List;

@Service
public class CategoryService {

    private final BlogCategoryRepository categoryRepository;
    private final BlogMapper blogMapper;
    private final TimelineService timelineService;

    public CategoryService(
        BlogCategoryRepository categoryRepository,
        BlogMapper blogMapper,
        TimelineService timelineService
    ) {
        this.categoryRepository = categoryRepository;
        this.blogMapper = blogMapper;
        this.timelineService = timelineService;
    }

    @Transactional(readOnly = true)
    public List<CategoryApi.CategoryResponse> listFlat() {
        return categoryRepository.findAllByOrderBySortNumAscNameAsc().stream()
            .map(blogMapper::toCategoryResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryApi.CategoryTreeNode> tree() {
        return blogMapper.toCategoryTree(categoryRepository.findAllByOrderBySortNumAscNameAsc());
    }

    @Transactional
    public CategoryApi.CategoryResponse create(CategoryApi.CategoryUpsertRequest request) {
        BlogCategory category = new BlogCategory();
        applyRequest(category, request, null);
        BlogCategory saved = categoryRepository.save(category);
        timelineService.recordSiteEvent(SiteEventType.SYSTEM, "新增分类", "分类「" + saved.getName() + "」已接入内容树。");
        return blogMapper.toCategoryResponse(saved);
    }

    @Transactional
    public CategoryApi.CategoryResponse update(Long id, CategoryApi.CategoryUpsertRequest request) {
        BlogCategory category = getRequiredCategory(id);
        applyRequest(category, request, id);
        BlogCategory saved = categoryRepository.save(category);
        timelineService.recordSiteEvent(SiteEventType.SYSTEM, "更新分类", "分类「" + saved.getName() + "」已更新层级或视觉配置。");
        return blogMapper.toCategoryResponse(saved);
    }

    @Transactional(readOnly = true)
    public BlogCategory getRequiredCategory(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "分类不存在: " + id));
    }

    private void applyRequest(BlogCategory category, CategoryApi.CategoryUpsertRequest request, Long selfId) {
        category.setName(request.name().trim());
        category.setSlug(generateUniqueSlug(request.slug(), request.name(), selfId));
        category.setDescription(defaultString(request.description()));
        category.setBannerImage(defaultString(request.bannerImage()));
        category.setBackgroundImage(defaultString(request.backgroundImage()));
        category.setSortNum(request.sortNum() == null ? 0 : request.sortNum());

        if (request.parentId() == null) {
            category.setParent(null);
            return;
        }

        if (selfId != null && selfId.equals(request.parentId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "分类不能把自己设置为父节点");
        }

        category.setParent(getRequiredCategory(request.parentId()));
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
            ? categoryRepository.existsBySlug(slug)
            : categoryRepository.existsBySlugAndIdNot(slug, selfId);
    }

    private String defaultString(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }
}
