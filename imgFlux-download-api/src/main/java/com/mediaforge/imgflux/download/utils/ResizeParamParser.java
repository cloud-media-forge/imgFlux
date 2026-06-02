package com.mediaforge.imgflux.download.utils;

import com.mediaforge.imgflux.engine.gm.ThumbnailDefinition;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ResizeParamParser {

    public static final Pattern VALID_ID_PATTERN =
            Pattern.compile("^(([0-9]*)x?([0-9]*))?(q-?\\d+)?(ex)?(trim|shave)?(?:rans:([^:]*):([^:]*))?$");

    private ResizeParamParser() {
    }

    public static ThumbnailDefinition parseResizeParam(String resizeParam) {
        Matcher matcher = VALID_ID_PATTERN.matcher(resizeParam);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid resize param: " + resizeParam);
        }

        ThumbnailDefinition definition = new ThumbnailDefinition();
        definition.setWidth(parseIntOrDefault(matcher.group(2), 0));
        definition.setHeight(parseIntOrDefault(matcher.group(3), 0));
        definition.setQuality(parseQuality(matcher.group(4)));
        definition.setExtent(matcher.group(5) != null);
        definition.setTrim(matcher.group(6) != null);
        definition.setSrcLang(defaultString(matcher.group(7)));
        definition.setToLang(defaultString(matcher.group(8)));
        return definition;
    }

    private static int parseQuality(String qualityParam) {
        if (qualityParam == null) {
            return 80;
        }
        return Integer.parseInt(qualityParam.substring(1).replaceFirst("^-", ""));
    }

    private static int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    private static String defaultString(String value) {
        return value == null ? "" : value;
    }
}
