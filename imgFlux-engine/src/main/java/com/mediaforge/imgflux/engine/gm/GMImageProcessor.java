package com.mediaforge.imgflux.engine.gm;

import com.mediaforge.imgflux.engine.dataoject.ImageMeta;
import com.mediaforge.imgflux.engine.service.translate.TextTranslationService;

import java.awt.color.ColorSpace;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.security.MessageDigest;

import com.mediaforge.imgflux.engine.service.util.ImageMetaUtils;
import org.gm4java.im4java.GMBatchCommand;
import org.im4java.core.IMOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GMImageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(GMImageProcessor.class);

    @Autowired
    private GMBatchCommand gmBatchCommand;

    @Autowired
    private TextTranslationService textTranslationService;
    
    /**
     * Process image using GMConnection and GMBatchCommand
     * @param def Thumbnail definition containing image data and processing parameters
     * @return Processed image data
     */
    public byte[] processImageWithBatchCommand(ThumbnailDefinition def) throws Exception {
        byte[] imageData = def.getImageData();
        int width = def.getWidth();
        int height = def.getHeight();
        int quality = def.getQuality();
        String srcLang = def.getSrcLang();
        String toLang = def.getToLang();
        String toFormat = def.getToFormat();

        String hash = String.format("%08x",
            new java.math.BigInteger(1, MessageDigest.getInstance("MD5").digest(imageData)).intValue());
        long ts = System.currentTimeMillis();
        Path tmpDir = Path.of(System.getProperty("java.io.tmpdir"));
        File tempInputFile = tmpDir.resolve(hash + "_" + ts + ".tmp").toFile();
        File tempOutputFile = tmpDir.resolve(hash + "_" + ts + ".tmp.out" + (def.getToFormat() != null ? "." + def.getToFormat() : "")).toFile();

        ImageMeta meta = ImageMetaUtils.getMeta(tempInputFile);
        try {
            try (FileOutputStream fos = new FileOutputStream(tempInputFile)) {
                fos.write(imageData);
            }

            IMOperation op = new IMOperation();
            op.addImage(tempInputFile.getAbsolutePath());
            if (width > 0 || height > 0) {
                op.resize(width, height);
            }
            op.quality((double) quality);
            if (def.isExtent()){
                if (meta.getColorSpace()!= null && ColorSpace.TYPE_CMYK == meta.getColorSpace().getType()) {
                    op.background("black");
                } else {
                    op.background("white");
                }
                if("gif".equals(meta.getSrcFormat()) || "gif".equals(toFormat)){
                    op.background("transparent");
                }
                op.gravity("center");
                op.extent(width, height);
            }
            if (def.isTrim()){
                op.trim();
            }
            if (def.getToFormat() != null){
                op.format(def.getToFormat());
            }

            op.addImage(tempOutputFile.getAbsolutePath());
            gmBatchCommand.run(op);

            byte[] result;
            try (FileInputStream fis = new FileInputStream(tempOutputFile)) {
                result = fis.readAllBytes();
            }

            return textTranslationService.translate(result, toFormat, srcLang, toLang);
        } catch (Exception e) {
            logger.error("processImage error", e);
            return def.getImageData();
        } finally {
            tempInputFile.delete();
            tempOutputFile.delete();
        }
    }
    
}
