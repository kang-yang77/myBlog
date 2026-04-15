package tech.exitzero.blog.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import tech.exitzero.blog.backend.api.UploadApi;
import tech.exitzero.blog.backend.config.StorageProperties;
import tech.exitzero.blog.backend.support.ApiException;
import tech.exitzero.blog.backend.support.SlugUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final StorageProperties storageProperties;

    public FileStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    public UploadApi.UploadResponse store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String sanitizedFilename = SlugUtils.sanitizeFilename(originalFilename);
        String storedFilename = UUID.randomUUID().toString().replace("-", "") + "-" + sanitizedFilename;

        try {
            Path storageDir = storageProperties.resolveLocation();
            Files.createDirectories(storageDir);
            Path target = storageDir.resolve(storedFilename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String publicPath = normalizePublicPath(storageProperties.getPublicPath());
            String url = publicPath + "/" + storedFilename;

            return new UploadApi.UploadResponse(
                originalFilename,
                storedFilename,
                url,
                StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream",
                file.getSize()
            );
        } catch (IOException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "文件保存失败: " + ex.getMessage());
        }
    }

    private String normalizePublicPath(String publicPath) {
        if (!StringUtils.hasText(publicPath)) {
            return "/uploads";
        }
        String normalized = publicPath.startsWith("/") ? publicPath : "/" + publicPath;
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }
}
