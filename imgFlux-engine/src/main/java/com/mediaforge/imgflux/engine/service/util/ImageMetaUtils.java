package com.mediaforge.imgflux.engine.service.util;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.mediaforge.imgflux.engine.dataoject.ImageMeta;
import lombok.extern.slf4j.Slf4j;

/**
 * default javadoc.
 * <pre>
 * created by @author Richard Chen
 * on @date 2026-06-04 20:03:00
 * </pre>
 **/
@Slf4j
public class ImageMetaUtils {

    public static ImageMeta getMeta(File file) {
        ImageMeta meta = new ImageMeta();
        try {
            // 1. Getting Width, Height, and Type via BufferedImage
            BufferedImage bImage = ImageIO.read(file);
            int width = bImage.getWidth();
            int height = bImage.getHeight();

            // 2. Getting Color Space
            ColorSpace colorSpaceName = bImage.getColorModel().getColorSpace();
            meta.setWidth(width);
            meta.setHeight(height);
            meta.setColorSpace(colorSpaceName);

            // 3. Getting File Format (e.g., png, jpg, gif)
            try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
                if (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    String formatName = reader.getFormatName();
                    meta.setSrcFormat(formatName);
                }
            }

        } catch (Exception e) {
            log.error("Failed to extract image metadata", e);
        }
        return meta;
    }
}
