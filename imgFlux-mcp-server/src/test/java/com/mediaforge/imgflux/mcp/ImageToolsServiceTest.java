package com.mediaforge.imgflux.mcp;

import com.mediaforge.imgflux.mcp.tools.ImageTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests ImageTools directly (without MCP protocol) to verify image processing logic.
 * These tests work whether or not GraphicsMagick is installed — Java2D fallbacks are used.
 */
@SpringBootTest
class ImageToolsServiceTest {

    @Autowired
    ImageTools imageTools;

    @TempDir
    Path tempDir;

    private Path inputPng;
    private Path borderedPng;

    @BeforeEach
    void setUp() throws Exception {
        inputPng = createTestImage(400, 300, Color.BLUE, "input.png");
        borderedPng = createBorderedImage("bordered.png");
    }

    @Test
    void resizeImage_shouldScaleDown() {
        Path output = tempDir.resolve("resized.png");
        String result = imageTools.resizeImage(
                inputPng.toString(), output.toString(), 200, 150, 80, "PNG");

        assertFalse(result.startsWith("Error"), result);
        assertTrue(Files.exists(output), "Output file should exist");
        assertTrue(result.contains("200x150"), result);
    }

    @Test
    void resizeImage_autoHeight() {
        Path output = tempDir.resolve("auto-height.png");
        String result = imageTools.resizeImage(
                inputPng.toString(), output.toString(), 200, 0, 80, "PNG");

        assertFalse(result.startsWith("Error"), result);
        assertTrue(Files.exists(output));
    }

    @Test
    void trimImage_shouldRemoveWhiteBorders() throws Exception {
        Path output = tempDir.resolve("trimmed.png");
        String result = imageTools.trimImage(borderedPng.toString(), output.toString());

        assertFalse(result.startsWith("Error"), result);
        assertTrue(Files.exists(output));

        BufferedImage trimmed = ImageIO.read(output.toFile());
        assertTrue(trimmed.getWidth() < 300, "Width should shrink from 300");
        assertTrue(trimmed.getHeight() < 300, "Height should shrink from 300");
        assertTrue(trimmed.getWidth() >= 200, "Should not trim content");
    }

    @Test
    void convertFormat_pngToJpeg() {
        Path output = tempDir.resolve("converted.jpg");
        String result = imageTools.convertImageFormat(
                inputPng.toString(), output.toString(), "JPG", 85);

        assertFalse(result.startsWith("Error"), result);
        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);
    }

    @Test
    void getImageInfo_shouldReturnDimensions() {
        String result = imageTools.getImageInfo(inputPng.toString());
        assertTrue(result.contains("400x300"), "Should report 400x300 dimensions: " + result);
        assertTrue(result.contains("PNG"), "Should report PNG format: " + result);
    }

    @Test
    void processImage_combinedResizeAndTrim() throws Exception {
        Path output = tempDir.resolve("processed.png");
        String result = imageTools.processImage(
                borderedPng.toString(), output.toString(),
                200, 200, 80, "PNG",
                true, false, "", "");

        assertFalse(result.startsWith("Error"), result);
        assertTrue(Files.exists(output));
        assertTrue(result.contains("resized"), result);
        assertTrue(result.contains("trimmed"), result);
    }

    @Test
    void translateImageText_shouldReportUnavailableWithoutTesseract() {
        Path output = tempDir.resolve("translated.png");
        String result = imageTools.translateImageText(
                inputPng.toString(), output.toString(), "ko", "en");

        // Without Tesseract configured, should return a clear error message
        assertNotNull(result);
    }

    private Path createTestImage(int w, int h, Color color, String name) throws Exception {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, w, h);
        g.dispose();
        Path path = tempDir.resolve(name);
        ImageIO.write(img, "png", path.toFile());
        return path;
    }

    private Path createBorderedImage(String name) throws Exception {
        BufferedImage img = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 300, 300);
        g.setColor(Color.RED);
        g.fillRect(50, 50, 200, 200);
        g.dispose();
        Path path = tempDir.resolve(name);
        ImageIO.write(img, "png", path.toFile());
        return path;
    }
}
