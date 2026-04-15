package tech.exitzero.blog.backend.api;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public final class TagApi {

    private TagApi() {
    }

    public record TagUpsertRequest(
        @NotBlank String name,
        String slug,
        String color
    ) {
    }

    public record TagResponse(
        Long id,
        String name,
        String slug,
        String color,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
    }
}
