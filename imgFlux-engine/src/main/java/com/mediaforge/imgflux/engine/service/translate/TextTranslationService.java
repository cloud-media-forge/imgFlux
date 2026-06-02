package com.mediaforge.imgflux.engine.service.translate;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Automatic text translation service for thumbnail images.
 * <p>
 * Detects non-English text on images using Tesseract OCR (via Tess4J),
 * translates it to English, removes the original text naturally by sampling
 * surrounding background colors, and renders the translated text at the
 * same position and size using Java2D.
 * </p>
 */
@Slf4j
@Service("textTranslationService")
public class TextTranslationService {

    private static final Map<String, String> TESS_LANGUAGES = Map.of(
            "ko", "kor",
            "ja", "jpn",
            "en", "eng",
            "es", "spa",
            "fr", "fra",
            "de", "deu",
            "zh-CN", "chi_sim",
            "zh-TW", "chi_tra");

    @Value("${thumbnail.translate.enabled:true}")
    private boolean enabled;

    @Value("${thumbnail.translate.tesseract.datapath:/usr/share/tesseract-ocr/5/tessdata}")
    private String tessDataPath;

    @Value("${thumbnail.translate.tesseract.language:kor+jpn+eng+spa+fra+deu+chi_sim+chi_tra}")
    private String tessLanguage;

    @Value("${thumbnail.translate.min-confidence:50}")
    private int minConfidence;

    @PostConstruct
    void init() {
        if (!enabled) {
            return;
        }
        // Filter tessLanguage to only include languages whose traineddata files exist.
        // This prevents native SIGSEGV at runtime and allows partial OCR with available packs.
        List<String> available = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String lang : tessLanguage.split("\\+")) {
            String trimmed = lang.trim();
            File trainedData = new File(tessDataPath, trimmed + ".traineddata");
            if (trainedData.isFile()) {
                available.add(trimmed);
            } else {
                missing.add(trimmed + " (" + trainedData.getAbsolutePath() + ")");
            }
        }
        if (!missing.isEmpty()) {
            log.error("Tesseract language data NOT found for: {} — these languages will be skipped", missing);
        }
        if (available.isEmpty()) {
            log.error("No Tesseract language data available — disabling text translation");
            enabled = false;
        } else {
            tessLanguage = String.join("+", available);
            log.info("Text translation enabled with languages: {}, datapath: {}", tessLanguage, tessDataPath);
        }
    }

    /**
     * Process the converted image bytes: detect non-English text, translate,
     * remove old text naturally, and render translated text.
     *
     * @param imageBytes the original image bytes
     * @param formatSubtype the image format subtype (e.g. "jpeg", "png")
     * @param srcLang the source text language code
     * @param toLang the target text language code
     * @return processed image bytes, or the original bytes if no translation is needed
     */
    public byte[] translate(byte[] imageBytes, String formatSubtype, String srcLang, String toLang) {
        String normalizedSrcLang = normalizeLanguage(srcLang);
        String normalizedToLang = normalizeLanguage(toLang);
        if (normalizedSrcLang == null || normalizedToLang == null || normalizedSrcLang.equals(normalizedToLang)) {
            return imageBytes;
        }
        if (!enabled) {
            return imageBytes;
        }


        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                log.warn("Could not decode image for text translation");
                return imageBytes;
            }

            // Step 1: OCR detect text regions
            String ocrLanguage = buildOcrLanguage(normalizedSrcLang, normalizedToLang);
            if (ocrLanguage.isBlank()) {
                return imageBytes;
            }
            List<DetectedText> detections = detectTextRegions(image, ocrLanguage);
            if (detections.isEmpty()) {
                return imageBytes;
            }

            // Step 2: Filter to text lines that are not already in the target language.
            List<TextLine> translatableLines = groupAndFilterNonTargetLanguage(detections, normalizedToLang);
            if (translatableLines.isEmpty()) {
                return imageBytes;
            }

            log.info("Found {} text line(s) to translate from {} to {}",
                    translatableLines.size(), normalizedSrcLang, normalizedToLang);

            // Step 3: For each line - translate, remove old text, render new text
            BufferedImage result = deepCopy(image);
            Graphics2D g2d = result.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            for (TextLine line : translatableLines) {
                try {
                    String translated = translateToLanguage(line.text, normalizedSrcLang, normalizedToLang);
                    if (translated == null || translated.equals(line.text)) {
                        continue;
                    }

                    log.debug("Translated '{}' -> '{}'", line.text, translated);

                    // Remove old text by painting background color
                    fillWithSurroundingColor(result, g2d, line.x, line.y, line.width, line.height);

                    // Render translated text
                    renderText(g2d, translated, line.x, line.y, line.width, line.height, result);
                } catch (Exception e) {
                    log.warn("Failed to translate text '{}': {}", line.text, e.getMessage());
                }
            }

            g2d.dispose();

            // Step 4: Encode back to bytes
            String outputFormat = getOutputFormat(formatSubtype);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(result, outputFormat, baos);
            byte[] resultBytes = baos.toByteArray();

            log.info("Text translation completed: {} bytes -> {} bytes", imageBytes.length, resultBytes.length);
            return resultBytes;

        } catch (Throwable e) {
            log.error("Text translation failed, returning original image", e);
            return imageBytes;
        }
    }

    // -----------------------------------------------------------------------
    // OCR Detection
    // -----------------------------------------------------------------------

    // Package-private for testability (spied in unit tests)
    List<DetectedText> detectTextRegions(BufferedImage image, String ocrLanguage) {
        // Try word-level detection with sparse text mode first
        List<DetectedText> results = runOcr(image, ocrLanguage, 11, net.sourceforge.tess4j.ITessAPI.TessPageIteratorLevel.RIL_WORD);
        if (!results.isEmpty()) {
            return results;
        }
        // Fallback: try line-level detection with sparse text mode
        results = runOcr(image, ocrLanguage, 11, net.sourceforge.tess4j.ITessAPI.TessPageIteratorLevel.RIL_TEXTLINE);
        if (!results.isEmpty()) {
            return results;
        }
        // Fallback: try auto mode with line-level
        return runOcr(image, ocrLanguage, 3, net.sourceforge.tess4j.ITessAPI.TessPageIteratorLevel.RIL_TEXTLINE);
    }

    private List<DetectedText> runOcr(BufferedImage image, String ocrLanguage, int pageSegMode, int iteratorLevel) {
        List<DetectedText> results = new ArrayList<>();
        try {
            // Pre-flight safety net: tessLanguage is filtered at init(), but if called
            // before init (e.g. tests), re-verify to prevent native SIGSEGV.
            for (String lang : ocrLanguage.split("\\+")) {
                File trainedData = new File(tessDataPath, lang.trim() + ".traineddata");
                if (!trainedData.isFile()) {
                    log.warn("Tesseract language data not found: {} — skipping OCR", trainedData.getAbsolutePath());
                    return results;
                }
            }

            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(tessDataPath);
            tesseract.setLanguage(ocrLanguage);
            tesseract.setPageSegMode(pageSegMode);

            List<Word> words = tesseract.getWords(image, iteratorLevel);
            for (Word word : words) {
                String text = word.getText().trim();
                float confidence = word.getConfidence();
                if (text.isEmpty() || confidence < minConfidence) {
                    continue;
                }
                java.awt.Rectangle rect = word.getBoundingBox();
                results.add(new DetectedText(text, rect.x, rect.y, rect.width, rect.height, confidence));
            }
            log.debug("OCR psm={} level={} found {} text regions", pageSegMode, iteratorLevel, results.size());
        } catch (Exception e) {
            log.error("Tesseract OCR failed (psm={}, level={}): {}", pageSegMode, iteratorLevel, e.getMessage());
        }
        return results;
    }

    // -----------------------------------------------------------------------
    // Group words into lines and filter non-English
    // -----------------------------------------------------------------------

    private List<TextLine> groupAndFilterNonTargetLanguage(List<DetectedText> detections, String toLang) {
        // Sort by y, then x
        detections.sort((a, b) -> {
            int cmp = Integer.compare(a.y, b.y);
            return cmp != 0 ? cmp : Integer.compare(a.x, b.x);
        });

        // Group into lines based on vertical overlap
        List<List<DetectedText>> lines = new ArrayList<>();
        List<DetectedText> currentLine = new ArrayList<>();
        currentLine.add(detections.get(0));

        for (int i = 1; i < detections.size(); i++) {
            DetectedText prev = currentLine.get(currentLine.size() - 1);
            DetectedText curr = detections.get(i);

            int overlap = Math.max(0, Math.min(prev.y + prev.height, curr.y + curr.height) - Math.max(prev.y, curr.y));
            int minH = Math.min(prev.height, curr.height);
            if (minH > 0 && (float) overlap / minH > 0.4f) {
                currentLine.add(curr);
            } else {
                lines.add(currentLine);
                currentLine = new ArrayList<>();
                currentLine.add(curr);
            }
        }
        lines.add(currentLine);

        // Merge each line and filter non-English
        List<TextLine> result = new ArrayList<>();
        for (List<DetectedText> words : lines) {
            StringBuilder sb = new StringBuilder();
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
            int maxX2 = 0, maxY2 = 0;
            for (DetectedText w : words) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(w.text);
                minX = Math.min(minX, w.x);
                minY = Math.min(minY, w.y);
                maxX2 = Math.max(maxX2, w.x + w.width);
                maxY2 = Math.max(maxY2, w.y + w.height);
            }
            String fullText = sb.toString();
            if (!isTargetLanguageText(fullText, toLang)) {
                result.add(new TextLine(fullText, minX, minY, maxX2 - minX, maxY2 - minY));
            }
        }
        return result;
    }

    // -----------------------------------------------------------------------
    // Non-English detection
    // -----------------------------------------------------------------------

    private boolean isTargetLanguageText(String text, String toLang) {
        if (text == null || text.isBlank()) {
            return true;
        }
        for (int i = 0; i < text.length(); i++) {
            int cp = text.codePointAt(i);
            if (isCodePointForLanguage(cp, toLang)) {
                return true;
            }
            if (Character.isSupplementaryCodePoint(cp)) {
                i++;
            }
        }
        return false;
    }

    private boolean isCodePointForLanguage(int cp, String lang) {
        switch (lang) {
            case "ko":
                return (cp >= 0xAC00 && cp <= 0xD7AF)
                        || (cp >= 0x1100 && cp <= 0x11FF)
                        || (cp >= 0x3130 && cp <= 0x318F);
            case "ja":
                return (cp >= 0x3040 && cp <= 0x309F)
                        || (cp >= 0x30A0 && cp <= 0x30FF);
            case "zh-CN":
            case "zh-TW":
                return (cp >= 0x4E00 && cp <= 0x9FFF)
                        || (cp >= 0x3400 && cp <= 0x4DBF);
            default:
                return false;
        }
    }

    // -----------------------------------------------------------------------
    // Translation via Google Translate (free endpoint, no API key)
    // -----------------------------------------------------------------------

    // Package-private for testability (spied in unit tests)
    String translateToLanguage(String text, String srcLang, String toLang) {
        try {
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
            String urlStr = String.format(
                "https://translate.googleapis.com/translate_a/single?client=gtx&sl=%s&tl=%s&dt=t&q=%s",
                srcLang, toLang, encoded
            );

            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();
            if (status != 200) {
                log.warn("Translation API returned status {}", status);
                return null;
            }

            try (InputStream is = conn.getInputStream()) {
                String response = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                // Response format: [[["translated","original",...],...],...] — extract first translated string
                return parseTranslationResponse(response);
            }
        } catch (Exception e) {
            log.warn("Translation failed for '{}': {}", text, e.getMessage());
            return null;
        }
    }

    private String parseTranslationResponse(String json) {
        // Simple parser for Google Translate JSON response: [[["translated text","original",...],...],...]
        // Extract the first quoted string after [[["
        int start = json.indexOf("\"");
        if (start < 0) return null;
        int end = json.indexOf("\"", start + 1);
        if (end < 0) return null;
        return json.substring(start + 1, end);
    }

    // -----------------------------------------------------------------------
    // Natural text removal - fill with surrounding background color
    // -----------------------------------------------------------------------

    private void fillWithSurroundingColor(BufferedImage image, Graphics2D g2d, int x, int y, int w, int h) {
        int imgW = image.getWidth();
        int imgH = image.getHeight();

        // Clamp bounding box to image bounds
        int x1 = Math.max(0, x);
        int y1 = Math.max(0, y);
        int x2 = Math.min(imgW - 1, x + w - 1);
        int y2 = Math.min(imgH - 1, y + h - 1);
        int bw = x2 - x1 + 1;
        int bh = y2 - y1 + 1;
        if (bw <= 0 || bh <= 0) return;

        // Edge-based bilinear interpolation for natural background fill.
        // For each pixel inside the bounding box, sample colors from the
        // left/right edges (same row) and top/bottom edges (same column)
        // just outside the box, then blend them based on relative position.
        for (int py = y1; py <= y2; py++) {
            // left & right edge colors for this row
            int leftRgb  = image.getRGB(Math.max(0, x1 - 1), py);
            int rightRgb = image.getRGB(Math.min(imgW - 1, x2 + 1), py);

            for (int px = x1; px <= x2; px++) {
                // top & bottom edge colors for this column
                int topRgb    = image.getRGB(px, Math.max(0, y1 - 1));
                int bottomRgb = image.getRGB(px, Math.min(imgH - 1, y2 + 1));

                float tx = bw > 1 ? (float)(px - x1) / (bw - 1) : 0.5f;
                float ty = bh > 1 ? (float)(py - y1) / (bh - 1) : 0.5f;

                // Horizontal interpolation (left ↔ right)
                int hR = Math.round(((leftRgb >> 16) & 0xFF) * (1 - tx) + ((rightRgb >> 16) & 0xFF) * tx);
                int hG = Math.round(((leftRgb >> 8) & 0xFF) * (1 - tx) + ((rightRgb >> 8) & 0xFF) * tx);
                int hB = Math.round((leftRgb & 0xFF) * (1 - tx) + (rightRgb & 0xFF) * tx);

                // Vertical interpolation (top ↔ bottom)
                int vR = Math.round(((topRgb >> 16) & 0xFF) * (1 - ty) + ((bottomRgb >> 16) & 0xFF) * ty);
                int vG = Math.round(((topRgb >> 8) & 0xFF) * (1 - ty) + ((bottomRgb >> 8) & 0xFF) * ty);
                int vB = Math.round((topRgb & 0xFF) * (1 - ty) + (bottomRgb & 0xFF) * ty);

                // Average horizontal and vertical
                int fR = clamp((hR + vR) / 2);
                int fG = clamp((hG + vG) / 2);
                int fB = clamp((hB + vB) / 2);

                image.setRGB(px, py, (fR << 16) | (fG << 8) | fB);
            }
        }
    }

    private static int clamp(int v) {
        return Math.min(255, Math.max(0, v));
    }

    // -----------------------------------------------------------------------
    // Text rendering via Java2D
    // -----------------------------------------------------------------------

    private void renderText(Graphics2D g2d, String text, int x, int y, int w, int h, BufferedImage image) {
        // Determine text color based on background luminance
        Color textColor = chooseTextColor(image, x, y, w, h);
        g2d.setColor(textColor);

        // Binary search for best font size that fits the bounding box
        int bestSize = 8;
        String fontName = Font.SANS_SERIF;
        for (int size = 8; size <= h * 2; size++) {
            Font testFont = new Font(fontName, Font.BOLD, size);
            g2d.setFont(testFont);
            FontMetrics fm = g2d.getFontMetrics();
            int tw = fm.stringWidth(text);
            int th = fm.getAscent();
            if (tw <= w && th <= h) {
                bestSize = size;
            } else {
                break;
            }
        }

        Font font = new Font(fontName, Font.BOLD, bestSize);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();

        // Center text in bounding box
        int tw = fm.stringWidth(text);
        int th = fm.getAscent();
        int tx = x + (w - tw) / 2;
        int ty = y + (h + th) / 2 - fm.getDescent() / 2;

        g2d.drawString(text, tx, ty);
    }

    private Color chooseTextColor(BufferedImage image, int x, int y, int w, int h) {
        // Sample center pixel of the filled area to determine contrast
        int cx = Math.min(x + w / 2, image.getWidth() - 1);
        int cy = Math.min(y + h / 2, image.getHeight() - 1);
        int rgb = image.getRGB(cx, cy);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        double luminance = 0.299 * r + 0.587 * g + 0.114 * b;
        return luminance > 128 ? Color.BLACK : Color.WHITE;
    }

    // -----------------------------------------------------------------------
    // Utility
    // -----------------------------------------------------------------------

    private String normalizeLanguage(String lang) {
        if (lang == null || lang.isBlank()) {
            return null;
        }
        String normalized = lang.trim();
        if ("zh-cn".equalsIgnoreCase(normalized)) {
            return "zh-CN";
        }
        if ("zh-tw".equalsIgnoreCase(normalized)) {
            return "zh-TW";
        }
        normalized = normalized.toLowerCase();
        return TESS_LANGUAGES.containsKey(normalized) ? normalized : null;
    }

    private String buildOcrLanguage(String srcLang, String toLang) {
        Set<String> languages = new LinkedHashSet<>();
        addAvailableTessLanguage(languages, TESS_LANGUAGES.get(srcLang));
        addAvailableTessLanguage(languages, TESS_LANGUAGES.get(toLang));
        return String.join("+", languages);
    }

    private void addAvailableTessLanguage(Set<String> languages, String tessLang) {
        if (tessLang == null || tessLang.isBlank()) {
            return;
        }
        File trainedData = new File(tessDataPath, tessLang + ".traineddata");
        if (trainedData.isFile()) {
            languages.add(tessLang);
        } else {
            log.warn("Tesseract language data not found: {} — language will be skipped", trainedData.getAbsolutePath());
        }
    }

    private String getOutputFormat(String formatSubtype) {
        if (formatSubtype == null) return "jpeg";
        switch (formatSubtype.toLowerCase()) {
            case "png": return "png";
            case "gif": return "gif";
            case "webp": return "webp";
            default: return "jpeg";
        }
    }

    private BufferedImage deepCopy(BufferedImage source) {
        int type = source.getType() == BufferedImage.TYPE_CUSTOM ? BufferedImage.TYPE_INT_ARGB : source.getType();
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), type);
        Graphics2D g = copy.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return copy;
    }

    // -----------------------------------------------------------------------
    // Internal data classes
    // -----------------------------------------------------------------------

    // Package-private for testability
    static class DetectedText {
        final String text;
        final int x, y, width, height;
        final float confidence;

        DetectedText(String text, int x, int y, int width, int height, float confidence) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.confidence = confidence;
        }
    }

    // Package-private for testability
    static class TextLine {
        final String text;
        final int x, y, width, height;

        TextLine(String text, int x, int y, int width, int height) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
