package com.mediaforge.imgflux.mcp.tools;

import com.mediaforge.imgflux.engine.config.GMConfigProperties;
import com.mediaforge.imgflux.engine.gm.Gm4JavaBatchCommand;
import com.mediaforge.imgflux.engine.gm.ImageProcessingService;
import com.mediaforge.imgflux.engine.gm.ThumbnailDefinition;
import com.mediaforge.imgflux.engine.service.translate.TextTranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ImageTools {

    private static final Logger logger = LoggerFactory.getLogger(ImageTools.class);

    @Autowired(required = false)
    private ImageProcessingService imageProcessingService;

    @Autowired(required = false)
    private TextTranslationService textTranslationService;

    @Autowired(required = false)
    private Gm4JavaBatchCommand gmBatchCommand;

    private boolean isGmAvailable() {
        return gmBatchCommand != null && gmBatchCommand.isAvailable();
    }

    @Tool(description = "Resize an image to specified width and height. "
            + "Supports quality control (1-100) and optional format conversion. "
            + "If only width or height is provided, the other dimension is auto-calculated to maintain aspect ratio. "
            + "Supported formats: JPG, PNG, GIF, WEBP.")
    public String resizeImage(
            @ToolParam(description = "Absolute path to the input image file") String inputPath,
            @ToolParam(description = "Absolute path to save the output image") String outputPath,
            @ToolParam(description = "Target width in pixels, 0 means auto-calculate from height") int width,
            @ToolParam(description = "Target height in pixels, 0 means auto-calculate from width") int height,
            @ToolParam(description = "Compression quality 1-100, default 80") int quality,
            @ToolParam(description = "Output format: JPG, PNG, WEBP, GIF. Empty string keeps original format.") String format) {
        try {
            byte[] imageData = Files.readAllBytes(Path.of(inputPath));
            byte[] result;

            if (isGmAvailable()) {
                ThumbnailDefinition def = new ThumbnailDefinition();
                def.setImageData(imageData);
                def.setWidth(width);
                def.setHeight(height);
                def.setQuality(quality > 0 ? quality : 80);
                def.setToFormat(resolveFormat(format, inputPath));
                result = imageProcessingService.processImage(def);
            } else {
                result = javaResize(imageData, width, height);
            }

            Files.write(Path.of(outputPath), result);
            return String.format("Image resized to %dx%d and saved to %s (%d bytes)",
                    width, height, outputPath, result.length);
        } catch (Exception e) {
            logger.error("resizeImage failed", e);
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Remove white or blank borders/margins from an image (trim operation). "
            + "This is useful for cleaning up scanned documents, screenshots with extra whitespace, "
            + "or product photos with uneven borders.")
    public String trimImage(
            @ToolParam(description = "Absolute path to the input image file") String inputPath,
            @ToolParam(description = "Absolute path to save the trimmed output image") String outputPath) {
        try {
            byte[] imageData = Files.readAllBytes(Path.of(inputPath));
            byte[] result;

            if (isGmAvailable()) {
                ThumbnailDefinition def = new ThumbnailDefinition();
                def.setImageData(imageData);
                def.setTrim(true);
                def.setQuality(90);
                def.setToFormat(detectFormat(inputPath));
                result = imageProcessingService.processImage(def);
            } else {
                result = javaTrim(imageData);
            }

            Files.write(Path.of(outputPath), result);
            return String.format("Image trimmed and saved to %s (%d bytes)", outputPath, result.length);
        } catch (Exception e) {
            logger.error("trimImage failed", e);
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Convert an image from one format to another. "
            + "Supported formats: JPG, PNG, GIF, WEBP. "
            + "Useful for converting images for web optimization (e.g., PNG to WEBP) "
            + "or compatibility requirements.")
    public String convertImageFormat(
            @ToolParam(description = "Absolute path to the input image file") String inputPath,
            @ToolParam(description = "Absolute path to save the converted image") String outputPath,
            @ToolParam(description = "Target format: JPG, PNG, WEBP, GIF") String format,
            @ToolParam(description = "Compression quality 1-100, default 85") int quality) {
        try {
            byte[] imageData = Files.readAllBytes(Path.of(inputPath));
            byte[] result;

            if (isGmAvailable()) {
                ThumbnailDefinition def = new ThumbnailDefinition();
                def.setImageData(imageData);
                def.setQuality(quality > 0 ? quality : 85);
                def.setToFormat(format.toUpperCase());
                result = imageProcessingService.processImage(def);
            } else {
                result = javaConvert(imageData, format);
            }

            Files.write(Path.of(outputPath), result);
            return String.format("Image converted to %s format and saved to %s (%d bytes)",
                    format.toUpperCase(), outputPath, result.length);
        } catch (Exception e) {
            logger.error("convertImageFormat failed", e);
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Detect and translate text on an image using OCR (Tesseract). "
            + "Automatically detects text regions, translates to the target language, "
            + "removes the original text naturally, and renders translated text at the same position. "
            + "Supported languages: ko (Korean), ja (Japanese), en (English), es (Spanish), "
            + "fr (French), de (German), zh-CN (Simplified Chinese), zh-TW (Traditional Chinese).")
    public String translateImageText(
            @ToolParam(description = "Absolute path to the input image file") String inputPath,
            @ToolParam(description = "Absolute path to save the translated output image") String outputPath,
            @ToolParam(description = "Source language code: ko, ja, en, es, fr, de, zh-CN, zh-TW") String srcLang,
            @ToolParam(description = "Target language code: ko, ja, en, es, fr, de, zh-CN, zh-TW") String toLang) {
        try {
            if (textTranslationService == null) {
                return "Error: OCR/translation service not available (Tesseract not configured)";
            }
            byte[] imageData = Files.readAllBytes(Path.of(inputPath));
            String format = detectFormat(inputPath);
            byte[] result = textTranslationService.translate(imageData, format, srcLang, toLang);
            Files.write(Path.of(outputPath), result);
            return String.format("Image text translated from %s to %s and saved to %s (%d bytes)",
                    srcLang, toLang, outputPath, result.length);
        } catch (Exception e) {
            logger.error("translateImageText failed", e);
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Process an image with multiple combined operations: resize, trim, extent, format conversion, and text translation. "
            + "This is the most powerful tool — it can perform all operations in a single pass. "
            + "Leave parameters empty or 0 to skip specific operations.")
    public String processImage(
            @ToolParam(description = "Absolute path to the input image file") String inputPath,
            @ToolParam(description = "Absolute path to save the output image") String outputPath,
            @ToolParam(description = "Target width in pixels, 0 to skip resize") int width,
            @ToolParam(description = "Target height in pixels, 0 to skip resize") int height,
            @ToolParam(description = "Compression quality 1-100, default 80") int quality,
            @ToolParam(description = "Output format: JPG, PNG, WEBP, GIF. Empty keeps original.") String format,
            @ToolParam(description = "Whether to trim white borders") boolean trim,
            @ToolParam(description = "Whether to extent (pad) to exact dimensions after resize") boolean extent,
            @ToolParam(description = "Source language for text translation, empty to skip") String srcLang,
            @ToolParam(description = "Target language for text translation, empty to skip") String toLang) {
        try {
            if (imageProcessingService == null) {
                byte[] imageData = Files.readAllBytes(Path.of(inputPath));
                byte[] result = imageData;
                if (width > 0 || height > 0) {
                    result = javaResize(result, width, height);
                }
                if (trim) {
                    result = javaTrim(result);
                }
                if (format != null && !format.isBlank()) {
                    result = javaConvert(result, format);
                }
                Files.write(Path.of(outputPath), result);
                StringBuilder desc = new StringBuilder("Image processed (Java2D fallback):");
                if (width > 0 || height > 0) desc.append(String.format(" resized %dx%d", width, height));
                if (trim) desc.append(" trimmed");
                if (format != null && !format.isBlank()) desc.append(" converted to ").append(format.toUpperCase());
                desc.append(String.format(" -> %s (%d bytes)", outputPath, result.length));
                return desc.toString();
            }

            byte[] imageData = Files.readAllBytes(Path.of(inputPath));
            ThumbnailDefinition def = new ThumbnailDefinition();
            def.setImageData(imageData);
            def.setWidth(width);
            def.setHeight(height);
            def.setQuality(quality > 0 ? quality : 80);
            def.setTrim(trim);
            def.setExtent(extent);
            def.setToFormat(resolveFormat(format, inputPath));
            def.setSrcLang(srcLang != null ? srcLang : "");
            def.setToLang(toLang != null ? toLang : "");

            byte[] result = imageProcessingService.processImage(def);
            Files.write(Path.of(outputPath), result);

            StringBuilder desc = new StringBuilder("Image processed:");
            if (width > 0 || height > 0) desc.append(String.format(" resized %dx%d", width, height));
            if (trim) desc.append(" trimmed");
            if (extent) desc.append(" extended");
            if (format != null && !format.isBlank()) desc.append(" converted to ").append(format.toUpperCase());
            if (srcLang != null && !srcLang.isBlank() && toLang != null && !toLang.isBlank())
                desc.append(String.format(" translated %s->%s", srcLang, toLang));
            desc.append(String.format(" -> %s (%d bytes)", outputPath, result.length));
            return desc.toString();
        } catch (Exception e) {
            logger.error("processImage failed", e);
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Get basic information about an image file: dimensions (width x height), "
            + "file format, and file size. Does not modify the image.")
    public String getImageInfo(
            @ToolParam(description = "Absolute path to the image file") String inputPath) {
        try {
            Path path = Path.of(inputPath);
            byte[] imageData = Files.readAllBytes(path);
            long fileSize = imageData.length;

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            if (image == null) {
                return "Error: Could not decode image at " + inputPath;
            }

            String format = detectFormat(inputPath);
            return String.format("Image: %s | Format: %s | Dimensions: %dx%d | File size: %s (%d bytes)",
                    inputPath, format, image.getWidth(), image.getHeight(),
                    humanReadableSize(fileSize), fileSize);
        } catch (Exception e) {
            logger.error("getImageInfo failed", e);
            return "Error: " + e.getMessage();
        }
    }

    // -----------------------------------------------------------------------
    // Pure Java (Java2D) fallbacks when GraphicsMagick is not available
    // -----------------------------------------------------------------------

    private byte[] javaResize(byte[] imageData, int width, int height) throws IOException {
        BufferedImage src = ImageIO.read(new ByteArrayInputStream(imageData));
        if (src == null) throw new IOException("Unsupported image format");
        if (width <= 0 && height > 0) {
            width = (int) ((double) src.getWidth() / src.getHeight() * height);
        } else if (height <= 0 && width > 0) {
            height = (int) ((double) src.getHeight() / src.getWidth() * width);
        } else if (width <= 0 && height <= 0) {
            return imageData;
        }
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(src, 0, 0, width, height, null);
        g.dispose();
        return encodePng(resized);
    }

    private byte[] javaTrim(byte[] imageData) throws IOException {
        BufferedImage src = ImageIO.read(new ByteArrayInputStream(imageData));
        if (src == null) throw new IOException("Unsupported image format");
        int w = src.getWidth(), h = src.getHeight();
        int bgColor = src.getRGB(0, 0);
        int threshold = 30;
        int top = 0, bottom = h - 1, left = 0, right = w - 1;

        outer: for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                if (!colorMatch(src.getRGB(x, y), bgColor, threshold)) { top = y; break outer; }
        outer: for (int y = h - 1; y >= top; y--)
            for (int x = 0; x < w; x++)
                if (!colorMatch(src.getRGB(x, y), bgColor, threshold)) { bottom = y; break outer; }
        outer: for (int x = 0; x < w; x++)
            for (int y = top; y <= bottom; y++)
                if (!colorMatch(src.getRGB(x, y), bgColor, threshold)) { left = x; break outer; }
        outer: for (int x = w - 1; x >= left; x--)
            for (int y = top; y <= bottom; y++)
                if (!colorMatch(src.getRGB(x, y), bgColor, threshold)) { right = x; break outer; }

        int cropW = right - left + 1, cropH = bottom - top + 1;
        if (cropW <= 0 || cropH <= 0) return imageData;
        BufferedImage cropped = src.getSubimage(left, top, cropW, cropH);
        BufferedImage result = new BufferedImage(cropW, cropH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.drawImage(cropped, 0, 0, null);
        g.dispose();
        return encodePng(result);
    }

    private byte[] javaConvert(byte[] imageData, String format) throws IOException {
        BufferedImage src = ImageIO.read(new ByteArrayInputStream(imageData));
        if (src == null) throw new IOException("Unsupported image format");
        String fmt = format.toLowerCase();
        if ("jpg".equals(fmt) || "jpeg".equals(fmt)) {
            BufferedImage rgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgb.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, src.getWidth(), src.getHeight());
            g.drawImage(src, 0, 0, null);
            g.dispose();
            src = rgb;
            fmt = "jpeg";
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(src, fmt, baos);
        return baos.toByteArray();
    }

    private boolean colorMatch(int rgb1, int rgb2, int threshold) {
        int dr = Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF));
        int dg = Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF));
        int db = Math.abs((rgb1 & 0xFF) - (rgb2 & 0xFF));
        return (dr + dg + db) / 3 <= threshold;
    }

    private byte[] encodePng(BufferedImage img) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }

    // -----------------------------------------------------------------------
    // Utility
    // -----------------------------------------------------------------------

    private String resolveFormat(String format, String path) {
        if (format != null && !format.isBlank()) return format.toUpperCase();
        return detectFormat(path);
    }

    private String detectFormat(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".png")) return "PNG";
        if (lower.endsWith(".gif")) return "GIF";
        if (lower.endsWith(".webp")) return "WEBP";
        if (lower.endsWith(".avif")) return "AVIF";
        if (lower.endsWith(".bmp")) return "BMP";
        return "JPG";
    }

    private String humanReadableSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}
