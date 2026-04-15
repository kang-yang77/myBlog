package tech.exitzero.blog.backend.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.exitzero.blog.backend.api.ArticleApi;
import tech.exitzero.blog.backend.domain.ArticleStatus;
import tech.exitzero.blog.backend.service.ArticleService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/articles")
public class AdminArticleController {

    private final ArticleService articleService;

    public AdminArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public List<ArticleApi.ArticleSummaryResponse> list(@RequestParam(required = false) ArticleStatus status) {
        return articleService.list(status);
    }

    @GetMapping("/{id}")
    public ArticleApi.ArticleDetailResponse get(@PathVariable Long id) {
        return articleService.get(id);
    }

    @PostMapping("/drafts")
    public ArticleApi.ArticleDetailResponse createDraft(@RequestBody ArticleApi.DraftCreateRequest request) {
        return articleService.createDraft(request);
    }

    @PutMapping("/{id}/draft")
    public ArticleApi.ArticleDetailResponse saveDraft(
        @PathVariable Long id,
        @RequestBody ArticleApi.DraftSaveRequest request
    ) {
        return articleService.saveDraft(id, request);
    }

    @PutMapping("/{id}/status")
    public ArticleApi.ArticleDetailResponse updateStatus(
        @PathVariable Long id,
        @Valid @RequestBody ArticleApi.StatusUpdateRequest request
    ) {
        return articleService.changeStatus(id, request);
    }
}
