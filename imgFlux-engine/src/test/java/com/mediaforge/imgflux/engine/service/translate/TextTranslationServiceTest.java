package com.mediaforge.imgflux.engine.service.translate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class TextTranslationServiceTest {

    private final TextTranslationService textTranslationService = new TextTranslationService();

    @Test
    void translateReturnsOriginalWhenLanguagesAreBlank() {
        byte[] imageBytes = "image".getBytes();

        byte[] result = textTranslationService.translate(imageBytes, "jpeg", "", "en");

        assertSame(imageBytes, result);
    }

    @Test
    void translateReturnsOriginalWhenLanguagesAreSame() {
        byte[] imageBytes = "image".getBytes();

        byte[] result = textTranslationService.translate(imageBytes, "jpeg", "zh-CN", "zh-cn");

        assertSame(imageBytes, result);
    }

    @Test
    void translateReturnsOriginalWhenLanguageIsUnsupported() {
        byte[] imageBytes = "image".getBytes();

        byte[] result = textTranslationService.translate(imageBytes, "jpeg", "ru", "en");

        assertSame(imageBytes, result);
    }
}
