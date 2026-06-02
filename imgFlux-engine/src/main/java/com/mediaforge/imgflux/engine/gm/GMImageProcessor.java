package com.mediaforge.imgflux.engine.gm;

import com.mediaforge.imgflux.engine.service.translate.TextTranslationService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.gm4java.engine.GMConnection;
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
    private Gm4JavaBatchCommand gmService;

    @Autowired
    private TextTranslationService textTranslationService;
    
    /**
     * Process image using GMConnection and GMBatchCommand
     * @param imageData Original image data
     * @param width Target width
     * @param height Target height
     * @param quality Image quality
     * @return Processed image data
     */
    public byte[] processImageWithBatchCommand(
            byte[] imageData, int width, int height, int quality, String format, String srcLang, String toLang) throws Exception {
        GMConnection connection = null;
        File tempInputFile = null;
        File tempOutputFile = null;
        
        try {
            // 获取GM连接
            connection = gmService.getConnection();
            
            // 创建临时文件
            tempInputFile = File.createTempFile("gm_input_", ".tmp");
            tempOutputFile = File.createTempFile("gm_output_", ".tmp");
            
            // 将输入数据写入临时文件
            try (FileOutputStream fos = new FileOutputStream(tempInputFile)) {
                fos.write(imageData);
            }
            
            // 使用GMBatchCommand执行命令
            GMBatchCommand cmd = gmService.getGMBatchCommand();
            
            // 创建操作
            IMOperation op = new IMOperation();
            op.addImage(tempInputFile.getAbsolutePath());
            if (width > 0 || height > 0) {
                op.resize(width, height);
            }
            op.quality((double) quality);
            op.addImage(tempOutputFile.getAbsolutePath());
            
            // 执行命令
            cmd.run(op);
            
            // 读取输出文件
            byte[] result;
            try (FileInputStream fis = new FileInputStream(tempOutputFile)) {
                result = fis.readAllBytes();
            }
            
            return textTranslationService.translate(result, format, srcLang, toLang);
        } finally {
            // 关闭连接
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    logger.warn("Failed to close GM connection", e);
                }
            }
            
            // 清理临时文件
            if (tempInputFile != null) tempInputFile.delete();
            if (tempOutputFile != null) tempOutputFile.delete();
        }
    }
    
    /**
     * Process image using GMConnection
     * @param imageData Original image data
     * @param width Target width
     * @param height Target height
     * @param quality Image quality
     * @return Processed image data
     */
    public byte[] processImageWithConnection(byte[] imageData, int width, int height, int quality) throws Exception {
        GMConnection connection = null;
        File tempInputFile = null;
        File tempOutputFile = null;
        
        try {
            // 获取GM连接
            connection = gmService.getConnection();
            
            // 创建临时文件
            tempInputFile = File.createTempFile("gm_input_", ".tmp");
            tempOutputFile = File.createTempFile("gm_output_", ".tmp");
            
            // 将输入数据写入临时文件
            try (FileOutputStream fos = new FileOutputStream(tempInputFile)) {
                fos.write(imageData);
            }
            
            // 执行多个GM命令
            connection.execute("convert", 
                tempInputFile.getAbsolutePath(),
                "-resize", width + "x" + height,
                "-quality", String.valueOf(quality),
                tempOutputFile.getAbsolutePath());
            
            // 读取输出文件
            byte[] result;
            try (FileInputStream fis = new FileInputStream(tempOutputFile)) {
                result = fis.readAllBytes();
            }
            
            return result;
        } finally {
            // 关闭连接
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    logger.warn("Failed to close GM connection", e);
                }
            }
            
            // 清理临时文件
            if (tempInputFile != null) tempInputFile.delete();
            if (tempOutputFile != null) tempOutputFile.delete();
        }
    }
}
