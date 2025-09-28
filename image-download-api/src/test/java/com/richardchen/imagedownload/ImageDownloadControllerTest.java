package com.richardchen.imagedownload;

import com.richardchen.imageprocessing.ImageProcessingService;
import com.richardchen.imageservice.cdn.CdnService;
import com.richardchen.imageservice.storage.ObjectStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ImageDownloadControllerTest {

    @Mock
    private ImageProcessingService imageProcessingService;

    @Mock
    private ObjectStorageService objectStorageService;

    @Mock
    private CdnService cdnService;

    @InjectMocks
    private ImageDownloadController imageDownloadController;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // 使用反射设置bucketName字段
        Field bucketNameField = ImageDownloadController.class.getDeclaredField("bucketName");
        bucketNameField.setAccessible(true);
        bucketNameField.set(imageDownloadController, "original-image");
    }

    @Test
    public void testViewImage_Success() throws Exception {
        // 准备测试数据
        String imagePath = "test/image.jpg";
        byte[] imageData = "test image data".getBytes();
        
        // 模拟ObjectStorageService的行为
        when(objectStorageService.downloadFile("original-image", imagePath)).thenReturn(imageData);
        
        // 创建模拟的HttpServletRequest
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/download/view/" + imagePath);
        
        // 调用被测试的方法
        ResponseEntity<byte[]> response = imageDownloadController.viewImage(request);
        
        // 验证结果
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getContentLength());
        assertEquals((Long)(long)imageData.length, response.getHeaders().getContentLength());
        assertNotNull(response.getHeaders().getContentType());
        assertEquals("image/jpeg", response.getHeaders().getContentType().toString());
        assertNotNull(response.getBody());
        assertArrayEquals(imageData, response.getBody());
        
        // 验证ObjectStorageService的方法被调用
        verify(objectStorageService).downloadFile("original-image", imagePath);
    }

    @Test
    public void testDownloadImage_Success() throws Exception {
        // 准备测试数据
        String imagePath = "test/image.jpg";
        byte[] originalImageData = "original image data".getBytes();
        byte[] processedImageData = "processed image data".getBytes();
        
        // 模拟ObjectStorageService的行为
        when(objectStorageService.downloadFile("original-image", imagePath)).thenReturn(originalImageData);
        
        // 模拟ImageProcessingService的行为
        when(imageProcessingService.processImage(originalImageData, 100, 100, 80, false, false, "JPG"))
            .thenReturn(processedImageData);
        
        // 创建模拟的HttpServletRequest
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/download/download/" + imagePath);
        
        // 调用被测试的方法
        ResponseEntity<byte[]> response = imageDownloadController.downloadImage(
            request, 100, 100, 80, false, false, "JPG");
        
        // 验证结果
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getContentLength());
        assertEquals((Long)(long)processedImageData.length, response.getHeaders().getContentLength());
        assertNotNull(response.getHeaders().getContentType());
        assertEquals("image/jpeg", response.getHeaders().getContentType().toString());
        assertNotNull(response.getBody());
        assertArrayEquals(processedImageData, response.getBody());
        
        // 验证ObjectStorageService的方法被调用
        verify(objectStorageService).downloadFile("original-image", imagePath);
        
        // 验证ImageProcessingService的方法被调用
        verify(imageProcessingService).processImage(originalImageData, 100, 100, 80, false, false, "JPG");
    }
    
    @Test
    public void testDownloadImage_OriginalImageReturnedWhenNoProcessingParams() throws Exception {
        // 准备测试数据
        String imagePath = "test/image.jpg";
        byte[] originalImageData = "original image data".getBytes();
        
        // 模拟ObjectStorageService的行为
        when(objectStorageService.downloadFile("original-image", imagePath)).thenReturn(originalImageData);
        
        // 创建模拟的HttpServletRequest
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/download/download/" + imagePath);
        
        // 调用被测试的方法，不指定处理参数（使用默认值）
        ResponseEntity<byte[]> response = imageDownloadController.downloadImage(
            request, 0, 0, 80, false, false, "JPG");
        
        // 验证结果
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getContentLength());
        assertEquals((Long)(long)originalImageData.length, response.getHeaders().getContentLength());
        assertNotNull(response.getBody());
        assertArrayEquals(originalImageData, response.getBody());
        
        // 验证ObjectStorageService的方法被调用
        verify(objectStorageService).downloadFile("original-image", imagePath);
        
        // 验证ImageProcessingService的方法没有被调用
        verify(imageProcessingService, never()).processImage(any(), anyInt(), anyInt(), anyInt(), anyBoolean(), anyBoolean(), anyString());
    }
    
    @Test
    public void testViewImage_ExceptionHandling() throws Exception {
        // 准备测试数据
        String imagePath = "test/image.jpg";
        
        // 模拟ObjectStorageService抛出异常
        when(objectStorageService.downloadFile("original-image", imagePath))
            .thenThrow(new RuntimeException("File not found"));
        
        // 创建模拟的HttpServletRequest
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/download/view/" + imagePath);
        
        // 调用被测试的方法
        ResponseEntity<byte[]> response = imageDownloadController.viewImage(request);
        
        // 验证结果
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        // 验证ObjectStorageService的方法被调用
        verify(objectStorageService).downloadFile("original-image", imagePath);
    }
}