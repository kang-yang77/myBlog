package tech.exitzero.blog.backend.api;

import jakarta.validation.constraints.NotNull;
import tech.exitzero.blog.backend.domain.ArticleStatus;
import tech.exitzero.blog.backend.domain.ArticleTimelineEventType;

import java.time.LocalDateTime;
import java.util.List;

public final class ArticleApi {

    private ArticleApi() {
    }

    public record DraftCreateRequest(
        String title,
        String slug,
        String summary,
        String contentMarkdown,
        Long categoryId,
        List<Long> tagIds,
        String coverImage,
        String bannerImage,
        String backgroundImage,
        Long createdBy
    ) {
    }

    public record DraftSaveRequest(
        String title,
        String slug,
        String summary,
        String contentMarkdown,
        Long categoryId,
        List<Long> tagIds,
        String coverImage,
        String bannerImage,
        String backgroundImage
    ) {
    }

    public record StatusUpdateRequest(
        @NotNull ArticleStatus status,
        String note,
        Long operatorId
    ) {
    }

    public record ArticleCategoryView(
        Long id,
        String name,
        String slug,
        Long parentId
    ) {
    }

    public record ArticleTagView(
        Long id,
        String name,
        String slug,
        String color
    ) {
    }

    public record ArticleTimelineView(
        Long id,
        ArticleTimelineEventType eventType,
        String eventTitle,
        String eventNote,
        Long operatorId,
        LocalDateTime createdAt
    ) {
    }

    public record ArticleSummaryResponse(
        Long id,
        String title,
        String slug,
        String summary,
        ArticleStatus status,
        ArticleCategoryView category,
        List<ArticleTagView> tags,
        String coverImage,
        String bannerImage,
        String backgroundImage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime publishedAt,
        LocalDateTime draftSavedAt
    ) {
    }

    public record ArticleDetailResponse(
        Long id,
        String title,
        String slug,
        String summary,
        String contentMarkdown,
        String contentHtml,
        ArticleStatus status,
        ArticleCategoryView category,
        List<ArticleTagView> tags,
        String coverImage,
        String bannerImage,
        String backgroundImage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime publishedAt,
        LocalDateTime draftSavedAt,
        List<ArticleTimelineView> timeline,
        List<ArticleStatus> allowedTransitions
    ) {
    }
}
