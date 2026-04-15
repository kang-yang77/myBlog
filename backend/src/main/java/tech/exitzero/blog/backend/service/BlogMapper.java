package tech.exitzero.blog.backend.service;

import org.springframework.stereotype.Component;
import tech.exitzero.blog.backend.api.ArticleApi;
import tech.exitzero.blog.backend.api.CategoryApi;
import tech.exitzero.blog.backend.api.TagApi;
import tech.exitzero.blog.backend.api.TimelineApi;
import tech.exitzero.blog.backend.api.VisualConfigApi;
import tech.exitzero.blog.backend.domain.BlogArticle;
import tech.exitzero.blog.backend.domain.BlogArticleTimeline;
import tech.exitzero.blog.backend.domain.BlogCategory;
import tech.exitzero.blog.backend.domain.BlogSiteEvent;
import tech.exitzero.blog.backend.domain.BlogSiteVisualConfig;
import tech.exitzero.blog.backend.domain.BlogTag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class BlogMapper {

    public ArticleApi.ArticleSummaryResponse toArticleSummary(BlogArticle article) {
        return new ArticleApi.ArticleSummaryResponse(
            article.getId(),
            article.getTitle(),
            article.getSlug(),
            article.getSummary(),
            article.getStatus(),
            toArticleCategoryView(article.getCategory()),
            article.getTags().stream().map(this::toArticleTagView).toList(),
            article.getCoverImage(),
            article.getBannerImage(),
            article.getBackgroundImage(),
            article.getCreatedAt(),
            article.getUpdatedAt(),
            article.getPublishedAt(),
            article.getDraftSavedAt()
        );
    }

    public ArticleApi.ArticleDetailResponse toArticleDetail(
        BlogArticle article,
        List<BlogArticleTimeline> timeline,
        List<tech.exitzero.blog.backend.domain.ArticleStatus> allowedTransitions
    ) {
        return new ArticleApi.ArticleDetailResponse(
            article.getId(),
            article.getTitle(),
            article.getSlug(),
            article.getSummary(),
            article.getContentMarkdown(),
            article.getContentHtml(),
            article.getStatus(),
            toArticleCategoryView(article.getCategory()),
            article.getTags().stream().map(this::toArticleTagView).toList(),
            article.getCoverImage(),
            article.getBannerImage(),
            article.getBackgroundImage(),
            article.getCreatedAt(),
            article.getUpdatedAt(),
            article.getPublishedAt(),
            article.getDraftSavedAt(),
            timeline.stream().map(this::toArticleTimelineView).toList(),
            allowedTransitions
        );
    }

    public CategoryApi.CategoryResponse toCategoryResponse(BlogCategory category) {
        return new CategoryApi.CategoryResponse(
            category.getId(),
            category.getName(),
            category.getSlug(),
            category.getParent() == null ? null : category.getParent().getId(),
            category.getDescription(),
            category.getBannerImage(),
            category.getBackgroundImage(),
            category.getSortNum(),
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }

    public List<CategoryApi.CategoryTreeNode> toCategoryTree(List<BlogCategory> categories) {
        Map<Long, List<BlogCategory>> childrenMap = categories.stream()
            .filter(category -> category.getParent() != null)
            .collect(Collectors.groupingBy(category -> category.getParent().getId()));

        return categories.stream()
            .filter(category -> category.getParent() == null)
            .sorted(categoryComparator())
            .map(category -> toCategoryTreeNode(category, childrenMap))
            .toList();
    }

    public TagApi.TagResponse toTagResponse(BlogTag tag) {
        return new TagApi.TagResponse(
            tag.getId(),
            tag.getName(),
            tag.getSlug(),
            tag.getColor(),
            tag.getCreatedAt(),
            tag.getUpdatedAt()
        );
    }

    public VisualConfigApi.VisualConfigResponse toVisualConfigResponse(BlogSiteVisualConfig config) {
        return new VisualConfigApi.VisualConfigResponse(
            config.getId(),
            config.getConfigKey(),
            config.getConfigName(),
            config.getImageUrl(),
            config.getOverlayCss(),
            config.getAccentColor(),
            config.getMotionClass(),
            config.isEnabled(),
            config.getCreatedAt(),
            config.getUpdatedAt()
        );
    }

    public TimelineApi.TimelineEntryResponse toTimelineEntry(BlogArticleTimeline timeline) {
        BlogArticle article = timeline.getArticle();
        BlogCategory category = article.getCategory();
        return new TimelineApi.TimelineEntryResponse(
            "ARTICLE",
            timeline.getEventType().name(),
            timeline.getEventTitle(),
            timeline.getEventNote(),
            timeline.getCreatedAt(),
            article.getId(),
            article.getTitle(),
            article.getStatus().name(),
            category == null ? null : category.getId(),
            category == null ? null : category.getName()
        );
    }

    public TimelineApi.TimelineEntryResponse toTimelineEntry(BlogSiteEvent siteEvent) {
        return new TimelineApi.TimelineEntryResponse(
            "SITE",
            siteEvent.getEventType().name(),
            siteEvent.getTitle(),
            siteEvent.getDescription(),
            siteEvent.getCreatedAt(),
            null,
            null,
            null,
            null,
            null
        );
    }

    private ArticleApi.ArticleCategoryView toArticleCategoryView(BlogCategory category) {
        if (category == null) {
            return null;
        }
        return new ArticleApi.ArticleCategoryView(
            category.getId(),
            category.getName(),
            category.getSlug(),
            category.getParent() == null ? null : category.getParent().getId()
        );
    }

    private ArticleApi.ArticleTagView toArticleTagView(BlogTag tag) {
        return new ArticleApi.ArticleTagView(
            tag.getId(),
            tag.getName(),
            tag.getSlug(),
            tag.getColor()
        );
    }

    private ArticleApi.ArticleTimelineView toArticleTimelineView(BlogArticleTimeline timeline) {
        return new ArticleApi.ArticleTimelineView(
            timeline.getId(),
            timeline.getEventType(),
            timeline.getEventTitle(),
            timeline.getEventNote(),
            timeline.getOperatorId(),
            timeline.getCreatedAt()
        );
    }

    private CategoryApi.CategoryTreeNode toCategoryTreeNode(
        BlogCategory category,
        Map<Long, List<BlogCategory>> childrenMap
    ) {
        List<BlogCategory> children = new ArrayList<>(childrenMap.getOrDefault(category.getId(), List.of()));
        children.sort(categoryComparator());
        return new CategoryApi.CategoryTreeNode(
            category.getId(),
            category.getName(),
            category.getSlug(),
            category.getDescription(),
            category.getBannerImage(),
            category.getBackgroundImage(),
            category.getSortNum(),
            children.stream().map(child -> toCategoryTreeNode(child, childrenMap)).toList()
        );
    }

    private Comparator<BlogCategory> categoryComparator() {
        return Comparator
            .comparing(BlogCategory::getSortNum, Comparator.nullsFirst(Integer::compareTo))
            .thenComparing(BlogCategory::getName, Comparator.nullsFirst(String::compareTo));
    }
}
