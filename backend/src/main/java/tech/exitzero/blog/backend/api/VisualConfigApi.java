package tech.exitzero.blog.backend.api;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public final class VisualConfigApi {

    private VisualConfigApi() {
    }

    public record VisualConfigUpsertRequest(
        @NotBlank String configKey,
        @NotBlank String configName,
        String imageUrl,
        String overlayCss,
        String accentColor,
        String motionClass,
        Boolean enabled
    ) {
    }

    public record VisualConfigResponse(
        Long id,
        String configKey,
        String configName,
        String imageUrl,
        String overlayCss,
        String accentColor,
        String motionClass,
        boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
    }
}
