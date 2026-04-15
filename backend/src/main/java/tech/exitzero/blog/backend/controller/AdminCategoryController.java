package tech.exitzero.blog.backend.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.exitzero.blog.backend.api.CategoryApi;
import tech.exitzero.blog.backend.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryApi.CategoryResponse> list() {
        return categoryService.listFlat();
    }

    @GetMapping("/tree")
    public List<CategoryApi.CategoryTreeNode> tree() {
        return categoryService.tree();
    }

    @PostMapping
    public CategoryApi.CategoryResponse create(@Valid @RequestBody CategoryApi.CategoryUpsertRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    public CategoryApi.CategoryResponse update(
        @PathVariable Long id,
        @Valid @RequestBody CategoryApi.CategoryUpsertRequest request
    ) {
        return categoryService.update(id, request);
    }
}
