package tech.exitzero.blog.backend.support;

import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.Locale;

public final class SlugUtils {

    private SlugUtils() {
    }

    public static String toSlug(String input) {
        if (!StringUtils.hasText(input)) {
            return "item";
        }

        String normalized = Normalizer.normalize(input.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFKC);
        String slug = normalized
            .replaceAll("[^\\p{L}\\p{Nd}]+", "-")
            .replaceAll("^-+|-+$", "")
            .replaceAll("-{2,}", "-");

        return StringUtils.hasText(slug) ? slug : "item";
    }

    public static String sanitizeFilename(String input) {
        if (!StringUtils.hasText(input)) {
            return "upload.bin";
        }

        String filename = input.replace("\\", "/");
        int slashIndex = filename.lastIndexOf('/');
        String baseName = slashIndex >= 0 ? filename.substring(slashIndex + 1) : filename;
        String sanitized = baseName
            .replaceAll("[^\\p{L}\\p{Nd}._-]+", "-")
            .replaceAll("-{2,}", "-");

        return StringUtils.hasText(sanitized) ? sanitized : "upload.bin";
    }
}
