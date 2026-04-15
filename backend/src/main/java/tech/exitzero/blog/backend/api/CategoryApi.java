package tech.exitzero.blog.backend.api;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;

public final class CategoryApi {

    private CategoryApi() {
    }

    public record CategoryUpsertRequest(
        @NotBlank String name,
        String slug,
        Long parentId,
        String description,
        String bannerImage,
        String backgroundImage,
        Integer sortNum
    ) {
    }

    public record CategoryResponse(
        Long id,
        String name,
        String slug,
        Long parentId,
        String description,
        String bannerImage,
        String backgroundImage,
        Integer sortNum,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
    }

    public record CategoryTreeNode(
        Long id,
        String name,
        String slug,
        String description,
        String bannerImage,
        String backgroundImage,
        Integer sortNum,
        List<CategoryTreeNode> children
    ) {
    }
}
