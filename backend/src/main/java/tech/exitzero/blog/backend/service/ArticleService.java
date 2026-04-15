package tech.exitzero.blog.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tech.exitzero.blog.backend.api.ArticleApi;
import tech.exitzero.blog.backend.domain.ArticleStatus;
import tech.exitzero.blog.backend.domain.ArticleTimelineEventType;
import tech.exitzero.blog.backend.domain.BlogArticle;
import tech.exitzero.blog.backend.domain.BlogArticleTimeline;
import tech.exitzero.blog.backend.domain.BlogCategory;
import tech.exitzero.blog.backend.domain.BlogTag;
import tech.exitzero.blog.backend.domain.SiteEventType;
import tech.exitzero.blog.backend.repository.BlogArticleRepository;
import tech.exitzero.blog.backend.repository.BlogArticleTimelineRepository;
import tech.exitzero.blog.backend.repository.BlogCategoryRepository;
import tech.exitzero.blog.backend.repository.BlogTagRepository;
import tech.exitzero.blog.backend.support.ApiException;
import tech.exitzero.blog.backend.support.SlugUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    private final BlogArticleRepository articleRepository;
    private final BlogArticleTimelineRepository articleTimelineRepository;
    private final BlogCategoryRepository categoryRepository;
    private final BlogTagRepository tagRepository;
    private final MarkdownRenderService markdownRenderService;
    private final ArticleStateMachine articleStateMachine;
    private final TimelineService timelineService;
    private final BlogMapper blogMapper;

    public ArticleService(
        BlogArticleRepository articleRepository,
        BlogArticleTimelineRepository articleTimelineRepository,
        BlogCategoryRepository categoryRepository,
        BlogTagRepository tagRepository,
        MarkdownRenderService markdownRenderService,
        ArticleStateMachine articleStateMachine,
        TimelineService timelineService,
        BlogMapper blogMapper
    ) {
        this.articleRepository = articleRepository;
        this.articleTimelineRepository = articleTimelineRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.markdownRenderService = markdownRenderService;
        this.articleStateMachine = articleStateMachine;
        this.timelineService = timelineService;
        this.blogMapper = blogMapper;
    }

    @Transactional(readOnly = true)
    public List<ArticleApi.ArticleSummaryResponse> list(ArticleStatus status) {
        List<BlogArticle> articles = status == null
            ? articleRepository.findAllByOrderByUpdatedAtDesc()
            : articleRepository.findAllByStatusOrderByUpdatedAtDesc(status);
        return articles.stream().map(blogMapper::toArticleSummary).toList();
    }

    @Transactional(readOnly = true)
    public ArticleApi.ArticleDetailResponse get(Long id) {
        BlogArticle article = getRequiredArticle(id);
        return toDetail(article);
    }

    @Transactional
    public ArticleApi.ArticleDetailResponse createDraft(ArticleApi.DraftCreateRequest request) {
        BlogArticle article = new BlogArticle();
        applyDraft(article, request.title(), request.slug(), request.summary(), request.contentMarkdown(),
            request.categoryId(), request.tagIds(), request.coverImage(), request.bannerImage(), request.backgroundImage());
        article.setStatus(ArticleStatus.DRAFT);
        article.setCreatedBy(request.createdBy());
        article.setDraftSavedAt(LocalDateTime.now());

        BlogArticle saved = articleRepository.save(article);
        timelineService.recordArticleEvent(saved, ArticleTimelineEventType.CREATED, "创建文章草稿", "文章进入草稿箱。", request.createdBy());
        timelineService.recordSiteEvent(SiteEventType.CONTENT, "创建新草稿", "文章《" + saved.getTitle() + "》已进入草稿箱。");
        return toDetail(saved);
    }

    @Transactional
    public ArticleApi.ArticleDetailResponse saveDraft(Long id, ArticleApi.DraftSaveRequest request) {
        BlogArticle article = getRequiredArticle(id);
        applyDraft(article, request.title(), request.slug(), request.summary(), request.contentMarkdown(),
            request.categoryId(), request.tagIds(), request.coverImage(), request.bannerImage(), request.backgroundImage());
        article.setDraftSavedAt(LocalDateTime.now());
        BlogArticle saved = articleRepository.save(article);
        return toDetail(saved);
    }

    @Transactional
    public ArticleApi.ArticleDetailResponse changeStatus(Long id, ArticleApi.StatusUpdateRequest request) {
        BlogArticle article = getRequiredArticle(id);
        ArticleStatus nextStatus = request.status();
        articleStateMachine.assertAllowed(article.getStatus(), nextStatus);
        article.setStatus(nextStatus);
        if (nextStatus == ArticleStatus.PUBLISHED) {
            article.setPublishedAt(LocalDateTime.now());
        }
        BlogArticle saved = articleRepository.save(article);

        ArticleTimelineEventType eventType = mapEventType(nextStatus);
        String eventTitle = mapEventTitle(nextStatus);
        String eventNote = StringUtils.hasText(request.note()) ? request.note().trim() : defaultEventNote(saved, nextStatus);
        timelineService.recordArticleEvent(saved, eventType, eventTitle, eventNote, request.operatorId());
        timelineService.recordSiteEvent(
            nextStatus == ArticleStatus.PUBLISHED ? SiteEventType.RELEASE : SiteEventType.CONTENT,
            eventTitle + " · " + saved.getTitle(),
            eventNote
        );
        return toDetail(saved);
    }

    @Transactional(readOnly = true)
    public BlogArticle getRequiredArticle(Long id) {
        return articleRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "文章不存在: " + id));
    }

    private void applyDraft(
        BlogArticle article,
        String requestTitle,
        String requestSlug,
        String requestSummary,
        String requestContentMarkdown,
        Long categoryId,
        List<Long> tagIds,
        String requestCoverImage,
        String requestBannerImage,
        String requestBackgroundImage
    ) {
        String resolvedTitle = resolveTitle(article, requestTitle);
        String resolvedMarkdown = resolveMarkdown(article, requestContentMarkdown);
        article.setTitle(resolvedTitle);
        article.setSlug(generateUniqueSlug(requestSlug, resolvedTitle, article.getId()));
        article.setSummary(defaultString(requestSummary));
        article.setContentMarkdown(resolvedMarkdown);
        article.setContentHtml(markdownRenderService.render(resolvedMarkdown));
        article.setCoverImage(defaultString(requestCoverImage));
        article.setBannerImage(defaultString(requestBannerImage));
        article.setBackgroundImage(defaultString(requestBackgroundImage));
        article.setCategory(resolveCategory(categoryId));
        article.setTags(resolveTags(tagIds));
    }

    private ArticleApi.ArticleDetailResponse toDetail(BlogArticle article) {
        List<BlogArticleTimeline> timeline = articleTimelineRepository.findAllByArticleIdOrderByCreatedAtDesc(article.getId());
        return blogMapper.toArticleDetail(article, timeline, articleStateMachine.nextStatuses(article.getStatus()));
    }

    private String resolveTitle(BlogArticle article, String requestTitle) {
        if (StringUtils.hasText(requestTitle)) {
            return requestTitle.trim();
        }
        if (StringUtils.hasText(article.getTitle())) {
            return article.getTitle();
        }
        return "未命名文章";
    }

    private String resolveMarkdown(BlogArticle article, String requestContentMarkdown) {
        if (StringUtils.hasText(requestContentMarkdown)) {
            return requestContentMarkdown;
        }
        if (StringUtils.hasText(article.getContentMarkdown())) {
            return article.getContentMarkdown();
        }
        return "# 新文章\n\n从这里开始写作。";
    }

    private BlogCategory resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "分类不存在: " + categoryId));
    }

    private Set<BlogTag> resolveTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        List<Long> distinctIds = new ArrayList<>(tagIds.stream().distinct().toList());
        List<BlogTag> tags = tagRepository.findAllById(distinctIds);
        if (tags.size() != distinctIds.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "标签列表中包含不存在的 tagId");
        }

        Map<Long, BlogTag> tagMap = tags.stream().collect(Collectors.toMap(BlogTag::getId, Function.identity()));
        LinkedHashSet<BlogTag> orderedTags = new LinkedHashSet<>();
        distinctIds.forEach(id -> orderedTags.add(tagMap.get(id)));
        return orderedTags;
    }

    private String generateUniqueSlug(String requestSlug, String fallbackTitle, Long selfId) {
        String baseSlug = SlugUtils.toSlug(StringUtils.hasText(requestSlug) ? requestSlug : fallbackTitle);
        String slug = baseSlug;
        int suffix = 2;
        while (slugExists(slug, selfId)) {
            slug = baseSlug + "-" + suffix++;
        }
        return slug;
    }

    private boolean slugExists(String slug, Long selfId) {
        return selfId == null
            ? articleRepository.existsBySlug(slug)
            : articleRepository.existsBySlugAndIdNot(slug, selfId);
    }

    private String defaultString(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private ArticleTimelineEventType mapEventType(ArticleStatus status) {
        return switch (status) {
            case DRAFT -> ArticleTimelineEventType.RESTORED;
            case PENDING -> ArticleTimelineEventType.SUBMITTED;
            case PUBLISHED -> ArticleTimelineEventType.PUBLISHED;
            case HIDDEN -> ArticleTimelineEventType.HIDDEN;
            case OFFLINE -> ArticleTimelineEventType.OFFLINE;
        };
    }

    private String mapEventTitle(ArticleStatus status) {
        return switch (status) {
            case DRAFT -> "退回草稿箱";
            case PENDING -> "提交待发布";
            case PUBLISHED -> "正式发布";
            case HIDDEN -> "隐藏文章";
            case OFFLINE -> "下架文章";
        };
    }

    private String defaultEventNote(BlogArticle article, ArticleStatus status) {
        return switch (status) {
            case DRAFT -> "文章《" + article.getTitle() + "》已退回草稿箱。";
            case PENDING -> "文章《" + article.getTitle() + "》已提交到待发布队列。";
            case PUBLISHED -> "文章《" + article.getTitle() + "》已正式发布。";
            case HIDDEN -> "文章《" + article.getTitle() + "》已隐藏。";
            case OFFLINE -> "文章《" + article.getTitle() + "》已下架。";
        };
    }
}
