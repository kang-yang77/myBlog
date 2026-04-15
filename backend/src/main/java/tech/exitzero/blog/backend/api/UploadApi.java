package tech.exitzero.blog.backend.api;

public final class UploadApi {

    private UploadApi() {
    }

    public record UploadResponse(
        String originalFilename,
        String storedFilename,
        String url,
        String contentType,
        long size
    ) {
    }
}
