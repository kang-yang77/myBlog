package tech.exitzero.blog.backend.api;

import java.time.LocalDateTime;

public final class TimelineApi {

    private TimelineApi() {
    }

    public record TimelineEntryResponse(
        String sourceType,
        String eventType,
        String title,
        String description,
        LocalDateTime createdAt,
        Long articleId,
        String articleTitle,
        String articleStatus,
        Long categoryId,
        String categoryName
    ) {
    }
}
