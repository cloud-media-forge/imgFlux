package com.example.imageservice.imageprocessing;

import org.gm4java.engine.GMConnection;
import org.gm4java.engine.GMException;
import org.gm4java.im4java.GMBatchCommand;
import org.im4java.core.IMOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class GMImageProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(GMImageProcessor.class);
    
    @Autowired
    private Gm4JavaBatchCommand gmService;
    
    /**
     * 使用GMConnection和GMBatchCommand处理图片
     * @param imageData 原始图片数据
     * @param width 目标宽度
     * @param height 目标高度
     * @param quality 图片质量
     * @return 处理后的图片数据
     */
    public byte[] processImageWithBatchCommand(byte[] imageData, int width, int height, int quality) throws Exception {
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
            op.resize(width, height);
            op.quality((double) quality);
            op.addImage(tempOutputFile.getAbsolutePath());
            
            // 执行命令
            cmd.run(op);
            
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
    
    /**
     * 使用GMConnection处理图片
     * @param imageData 原始图片数据
     * @param width 目标宽度
     * @param height 目标高度
     * @param quality 图片质量
     * @return 处理后的图片数据
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