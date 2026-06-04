package com.mediaforge.imgflux.engine.dataoject;

import java.awt.color.ColorSpace;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;

/**
 * default javadoc.
 * <pre>
 * created by @author Richard Chen
 * on @date 2026-06-04 20:05:00
 * </pre>
 **/
@Data
public class ImageMeta {
    int width;
    int height;
    String srcFormat;
    ColorSpace colorSpace;
}
