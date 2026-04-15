package tech.exitzero.blog.backend.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.exitzero.blog.backend.api.VisualConfigApi;
import tech.exitzero.blog.backend.service.VisualConfigService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/visual-configs")
public class AdminVisualConfigController {

    private final VisualConfigService visualConfigService;

    public AdminVisualConfigController(VisualConfigService visualConfigService) {
        this.visualConfigService = visualConfigService;
    }

    @GetMapping
    public List<VisualConfigApi.VisualConfigResponse> list() {
        return visualConfigService.list();
    }

    @PostMapping
    public VisualConfigApi.VisualConfigResponse create(
        @Valid @RequestBody VisualConfigApi.VisualConfigUpsertRequest request
    ) {
        return visualConfigService.create(request);
    }

    @PutMapping("/{id}")
    public VisualConfigApi.VisualConfigResponse update(
        @PathVariable Long id,
        @Valid @RequestBody VisualConfigApi.VisualConfigUpsertRequest request
    ) {
        return visualConfigService.update(id, request);
    }

    @PutMapping("/{id}/activate")
    public VisualConfigApi.VisualConfigResponse activate(@PathVariable Long id) {
        return visualConfigService.activate(id);
    }
}
