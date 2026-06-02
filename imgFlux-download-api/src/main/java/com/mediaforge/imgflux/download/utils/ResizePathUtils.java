package com.mediaforge.imgflux.download.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class ResizePathUtils {

    private ResizePathUtils() {
    }

    public static String extractResizeImagePath(HttpServletRequest request, String mode, String resizeParam) {
        String prefix = "/api/v1/thumbnail/resize/" + mode + "/" + resizeParam + "/";
        String requestUri = request.getRequestURI();
        if (!requestUri.startsWith(prefix)) {
            throw new IllegalArgumentException("Invalid resize path");
        }
        String imagePath = requestUri.substring(prefix.length());
        if (imagePath.isBlank()) {
            throw new IllegalArgumentException("Image path is required");
        }
        return imagePath;
    }

    public static ResizePath parseResizePath(String imagePath, String supportedFormats) {
        String[] parts = imagePath.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Image suffix is required");
        }

        Set<String> formats = supportedFormatSet(supportedFormats);
        String lastSuffix = normalizeFormat(parts[parts.length - 1]);
        if (!formats.contains(lastSuffix)) {
            throw new IllegalArgumentException("Unsupported image format: " + lastSuffix);
        }

        if (parts.length >= 3) {
            String sourceSuffix = normalizeFormat(parts[parts.length - 2]);
            if (formats.contains(sourceSuffix)) {
                String targetFormat = lastSuffix;
                String sourcePath = imagePath.substring(0, imagePath.length() - lastSuffix.length() - 1);
                return new ResizePath(sourcePath, targetFormat);
            }
        }

        return new ResizePath(imagePath, lastSuffix);
    }

    private static Set<String> supportedFormatSet(String supportedFormats) {
        return Arrays.stream(supportedFormats.split(","))
                .map(ResizePathUtils::normalizeFormat)
                .collect(Collectors.toSet());
    }

    private static String normalizeFormat(String format) {
        return format.trim().toUpperCase();
    }

    public record ResizePath(String sourcePath, String targetFormat) {
    }
}
