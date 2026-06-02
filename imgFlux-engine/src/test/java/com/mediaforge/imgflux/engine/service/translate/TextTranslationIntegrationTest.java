package com.mediaforge.imgflux.engine.service.translate;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextTranslationIntegrationTest {

    private static final String RESOURCE_DIR = "src/test/resources/translate/src";
    private static final String OUT_DIR = "src/test/resources/translate/output";
    private static final String SRC_LANG = "ko";

    private TextTranslationService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new TextTranslationService();
        java.lang.reflect.Field enabledField = TextTranslationService.class.getDeclaredField("enabled");
        enabledField.setAccessible(true);
        enabledField.set(service, true);

        java.lang.reflect.Field tessDataPathField = TextTranslationService.class.getDeclaredField("tessDataPath");
        tessDataPathField.setAccessible(true);
        tessDataPathField.set(service, resolveTessDataPath());

        java.lang.reflect.Field tessLanguageField = TextTranslationService.class.getDeclaredField("tessLanguage");
        tessLanguageField.setAccessible(true);
        tessLanguageField.set(service, "kor+chi_sim+eng");

        java.lang.reflect.Field minConfidenceField = TextTranslationService.class.getDeclaredField("minConfidence");
        minConfidenceField.setAccessible(true);
        minConfidenceField.set(service, 50);

        service.init();
    }

    private String resolveTessDataPath() {
        String[] candidates = {
            "/usr/share/tesseract-ocr/5/tessdata",
            "/usr/local/share/tessdata",
            "/opt/homebrew/share/tessdata",
            System.getProperty("TESSDATA_PREFIX", "/usr/share/tessdata")
        };
        for (String path : candidates) {
            if (Files.isDirectory(Paths.get(path))) {
                return path;
            }
        }
        return candidates[0];
    }

    static boolean tesseractAvailable() {
        String[] candidates = {
            "/usr/share/tesseract-ocr/5/tessdata",
            "/usr/local/share/tessdata",
            "/opt/homebrew/share/tessdata",
            "/usr/share/tessdata"
        };
        for (String path : candidates) {
            Path kor = Paths.get(path, "kor.traineddata");
            Path chiSim = Paths.get(path, "chi_sim.traineddata");
            Path eng = Paths.get(path, "eng.traineddata");
            if (Files.isRegularFile(kor) && Files.isRegularFile(chiSim) && Files.isRegularFile(eng)) {
                return true;
            }
        }
        return false;
    }

    @Test
    @EnabledIf("tesseractAvailable")
    void translateKoreanToChineseForAllTestImages() throws IOException {
        Path resourceDir = Paths.get(RESOURCE_DIR).toAbsolutePath();
        Path outDir = Paths.get(OUT_DIR).toAbsolutePath();
        assertTrue(Files.isDirectory(resourceDir), "Resource directory not found: " + resourceDir);

        String[][] targets = {
            {"zh-CN", "-zh-CN-result"},
            {"en",    "-en-result"},
        };

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(resourceDir)) {
            for (Path file : stream) {
                String fileName = file.getFileName().toString();
                if (fileName.contains("-result.")) {
                    continue;
                }
                String lower = fileName.toLowerCase();
                if (!lower.endsWith(".jpg") && !lower.endsWith(".jpeg") && !lower.endsWith(".png")) {
                    continue;
                }

                String format = lower.endsWith(".png") ? "png" : "jpeg";
                byte[] imageBytes = Files.readAllBytes(file);
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                String extension = fileName.substring(fileName.lastIndexOf('.'));

                for (String[] target : targets) {
                    String toLang = target[0];
                    String suffix = target[1];

                    byte[] result = service.translate(imageBytes, format, SRC_LANG, toLang);
                    assertNotNull(result, "Translation returned null for " + fileName + " -> " + toLang);

                    BufferedImage resultImage = ImageIO.read(new ByteArrayInputStream(result));
                    assertNotNull(resultImage, "Result is not a valid image for " + fileName + " -> " + toLang);

                    String outputFileName = baseName + suffix + extension;
                    Path outputPath = outDir.resolve(outputFileName);
                    Files.write(outputPath, result);

                    assertTrue(result.length > 0, "Result image is empty for " + fileName + " -> " + toLang);
                    System.out.println("Translated " + fileName + " -> " + outputFileName
                            + " (" + imageBytes.length + " -> " + result.length + " bytes) [" + toLang + "]");
                }
            }
        }
    }
}
