package tech.exitzero.blog.backend.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tech.exitzero.blog.backend.api.UploadApi;
import tech.exitzero.blog.backend.service.FileStorageService;

@RestController
@RequestMapping("/api/admin/assets")
public class AssetUploadController {

    private final FileStorageService fileStorageService;

    public AssetUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadApi.UploadResponse upload(@RequestPart("file") MultipartFile file) {
        return fileStorageService.store(file);
    }
}
