package com.richardchen.imageprocessing;

/**
 * 图片处理服务接口
 */
public interface ImageProcessingService {
    
    /**
     * 处理图片
     * @param imageData 原始图片二进制数据
     * @param width 目标宽度
     * @param height 目标高度
     * @param quality 图片质量 (1-100)
     * @param extent 是否扩展图片
     * @param trim 是否裁剪图片
     * @param format 目标格式
     * @return 处理后的图片二进制数据
     */
    byte[] processImage(byte[] imageData, int width, int height, int quality, 
                       boolean extent, boolean trim, String format);
}