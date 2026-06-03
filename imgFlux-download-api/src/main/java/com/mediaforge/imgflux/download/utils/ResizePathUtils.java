package com.mediaforge.imgflux.download.utils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class ResizePathUtils {

    private ResizePathUtils() {
    }

    /**
     * Normalize a decoded image path coming from Spring's catch-all path variable
     * ({@code {*name}}). Spring delivers the matched remainder with a leading slash
     * (e.g. "/folder/image.png" or "/https://example.com/a.png"); this method strips
     * that leading slash and validates the result is non-empty.
     */
    public static String normalizeDecodedImagePath(String decodedImagePath) {
        if (decodedImagePath == null || decodedImagePath.isBlank()) {
            throw new IllegalArgumentException("Image path is required");
        }
        if (decodedImagePath.startsWith("/")) {
            decodedImagePath = decodedImagePath.substring(1);
        }
        if (decodedImagePath.isBlank()) {
            throw new IllegalArgumentException("Image path is required");
        }
        return decodedImagePath;
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
