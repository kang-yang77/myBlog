package tech.exitzero.blog.backend.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.exitzero.blog.backend.api.TagApi;
import tech.exitzero.blog.backend.service.TagService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tags")
public class AdminTagController {

    private final TagService tagService;

    public AdminTagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public List<TagApi.TagResponse> list() {
        return tagService.list();
    }

    @PostMapping
    public TagApi.TagResponse create(@Valid @RequestBody TagApi.TagUpsertRequest request) {
        return tagService.create(request);
    }

    @PutMapping("/{id}")
    public TagApi.TagResponse update(@PathVariable Long id, @Valid @RequestBody TagApi.TagUpsertRequest request) {
        return tagService.update(id, request);
    }
}
