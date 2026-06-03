package com.mediaforge.imgflux.download.api;

import java.lang.reflect.Field;

import com.mediaforge.imgflux.engine.gm.ImageProcessingService;
import com.mediaforge.imgflux.engine.gm.ThumbnailDefinition;
import com.mediaforge.imgflux.engine.service.cdn.CdnService;
import com.mediaforge.imgflux.engine.service.storage.ObjectStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ImageDownloadControllerTest {

    @Mock
    private ImageProcessingService imageProcessingService;

    @Mock
    private ObjectStorageService objectStorageService;

    @Mock
    private CdnService cdnService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ImageThumbnailController imageDownloadController;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Use reflection to set bucketName field
        Field bucketNameField = ImageThumbnailController.class.getDeclaredField("bucketName");
        bucketNameField.setAccessible(true);
        bucketNameField.set(imageDownloadController, "original-image");

        Field supportedFormatsField = ImageThumbnailController.class.getDeclaredField("supportedFormats");
        supportedFormatsField.setAccessible(true);
        supportedFormatsField.set(imageDownloadController, "JPG,JPEG,PNG,GIF,AVIF,WEBP");
    }

    @Test
    public void testViewImage_Success() throws Exception {
        // Prepare test data
        String imagePath = "test/image.jpg";
        byte[] imageData = "test image data".getBytes();

        // Mock ObjectStorageService behavior
        when(objectStorageService.downloadFile("original-image", imagePath)).thenReturn(imageData);

        // Create mock HttpServletRequest
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/thumbnail/view/" + imagePath);

        // Call the method under test
        ResponseEntity<byte[]> response = imageDownloadController.viewImage(request);

        // Verify results
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getContentLength());
        assertEquals((Long)(long)imageData.length, response.getHeaders().getContentLength());
        assertNotNull(response.getHeaders().getContentType());
        assertEquals("image/jpeg", response.getHeaders().getContentType().toString());
        assertNotNull(response.getBody());
        assertArrayEquals(imageData, response.getBody());

        // Verify ObjectStorageService method was called
        verify(objectStorageService).downloadFile("original-image", imagePath);
    }

    @Test
    public void testDownloadImage_Success() throws Exception {
        // Prepare test data
        String imagePath = "test/image.jpg";
        byte[] originalImageData = "original image data".getBytes();
        byte[] processedImageData = "processed image data".getBytes();

        // Mock ObjectStorageService behavior
        when(objectStorageService.downloadFile("original-image", imagePath)).thenReturn(originalImageData);

        // Mock ImageProcessingService behavior
        when(imageProcessingService.processImage(any(ThumbnailDefinition.class)))
            .thenReturn(processedImageData);

        // Create mock HttpServletRequest
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/thumbnail/forge/local/" + imagePath);

        // Call the method under test with mode parameter
        ResponseEntity<byte[]> response = imageDownloadController.resize(
            request, "local", 100, 100, 80, false, false, "JPG", "", "");

        // Verify results
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getContentLength());
        assertEquals((Long)(long)processedImageData.length, response.getHeaders().getContentLength());
        assertNotNull(response.getHeaders().getContentType());
        assertEquals("image/jpeg", response.getHeaders().getContentType().toString());
        assertNotNull(response.getBody());
        assertArrayEquals(processedImageData, response.getBody());

        // Verify ObjectStorageService method was called
        verify(objectStorageService).downloadFile("original-image", imagePath);

        // Verify ImageProcessingService method was called
        ArgumentCaptor<ThumbnailDefinition> captor = ArgumentCaptor.forClass(ThumbnailDefinition.class);
        verify(imageProcessingService).processImage(captor.capture());
        ThumbnailDefinition def = captor.getValue();
        assertArrayEquals(originalImageData, def.getImageData());
        assertEquals(100, def.getWidth());
        assertEquals(100, def.getHeight());
        assertEquals(80, def.getQuality());
        assertEquals("JPG", def.getFormat());
    }
    
    @Test
    public void testDownloadImage_OriginalImageReturnedWhenNoProcessingParams() throws Exception {
        // Prepare test data
        String imagePath = "test/image.jpg";
        byte[] originalImageData = "original image data".getBytes();

        // Mock ObjectStorageService behavior
        when(objectStorageService.downloadFile("original-image", imagePath)).thenReturn(originalImageData);

        // Create mock HttpServletRequest
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/thumbnail/forge/local/" + imagePath);

        // Call the method under test without specifying processing parameters (use default values)
        ResponseEntity<byte[]> response = imageDownloadController.resize(
            request, "local", 0, 0, 80, false, false, "JPG", "", "");

        // Verify results
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getContentLength());
        assertEquals((Long)(long)originalImageData.length, response.getHeaders().getContentLength());
        assertNotNull(response.getBody());
        assertArrayEquals(originalImageData, response.getBody());

        // Verify ObjectStorageService method was called
        verify(objectStorageService).downloadFile("original-image", imagePath);

        // Verify ImageProcessingService method was not called
        verify(imageProcessingService, never()).processImage(any(ThumbnailDefinition.class));
    }

    @Test
    public void testForgePassesTranslationLanguages() throws Exception {
        String imagePath = "test/image.jpg";
        byte[] originalImageData = "original image data".getBytes();
        byte[] processedImageData = "translated image data".getBytes();

        when(objectStorageService.downloadFile("original-image", imagePath)).thenReturn(originalImageData);
        when(imageProcessingService.processImage(any(ThumbnailDefinition.class)))
            .thenReturn(processedImageData);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/thumbnail/forge/local/" + imagePath);

        ResponseEntity<byte[]> response = imageDownloadController.resize(
            request, "local", 0, 0, 80, false, false, "JPG", "ko", "zh-CN");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(processedImageData, response.getBody());

        ArgumentCaptor<ThumbnailDefinition> captor = ArgumentCaptor.forClass(ThumbnailDefinition.class);
        verify(imageProcessingService).processImage(captor.capture());
        ThumbnailDefinition def = captor.getValue();
        assertEquals("ko", def.getSrcLang());
        assertEquals("zh-CN", def.getToLang());
    }

    @Test
    public void testResizeImage_SuccessWithPathParamsAndFormatConversion() throws Exception {
        String imagePath = "test/image.jpg.webp";
        String sourcePath = "test/image.jpg";
        byte[] originalImageData = "original image data".getBytes();
        byte[] processedImageData = "processed image data".getBytes();

        when(objectStorageService.downloadFile("original-image", sourcePath)).thenReturn(originalImageData);
        when(imageProcessingService.processImage(any(ThumbnailDefinition.class))).thenReturn(processedImageData);

        ResponseEntity<byte[]> response = imageDownloadController.resizeImage("local", "100x200q-75extrimrans:ko:zh-CN", imagePath);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals((Long)(long)processedImageData.length, response.getHeaders().getContentLength());
        assertEquals("image/webp", response.getHeaders().getContentType().toString());
        assertArrayEquals(processedImageData, response.getBody());

        verify(objectStorageService).downloadFile("original-image", sourcePath);
        ArgumentCaptor<ThumbnailDefinition> captor = ArgumentCaptor.forClass(ThumbnailDefinition.class);
        verify(imageProcessingService).processImage(captor.capture());
        ThumbnailDefinition definition = captor.getValue();
        assertArrayEquals(originalImageData, definition.getImageData());
        assertEquals(100, definition.getWidth());
        assertEquals(200, definition.getHeight());
        assertEquals(75, definition.getQuality());
        assertEquals(true, definition.isExtent());
        assertEquals(true, definition.isTrim());
        assertEquals("WEBP", definition.getFormat());
        assertEquals("ko", definition.getSrcLang());
        assertEquals("zh-CN", definition.getToLang());
    }
    
    @Test
    public void testViewImage_ExceptionHandling() throws Exception {
        // Prepare test data
        String imagePath = "test/image.jpg";

        // Mock ObjectStorageService to throw exception
        when(objectStorageService.downloadFile("original-image", imagePath))
            .thenThrow(new RuntimeException("File not found"));

        // Create mock HttpServletRequest
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/thumbnail/view/" + imagePath);

        // Call the method under test
        ResponseEntity<byte[]> response = imageDownloadController.viewImage(request);

        // Verify results
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        // Verify ObjectStorageService method was called
        verify(objectStorageService).downloadFile("original-image", imagePath);
    }

    @Test
    public void testResize_RemoteMode_DownloadsFromRemote() throws Exception {
        String remoteUrlInPath = "https:/example.com/image.jpg"; // Spring strips one slash
        String fixedRemoteUrl = "https://example.com/image.jpg"; // Controller fixes it
        byte[] originalImageData = "remote image data".getBytes();
        byte[] processedImageData = "processed image data".getBytes();

        when(restTemplate.getForObject(fixedRemoteUrl, byte[].class)).thenReturn(originalImageData);
        when(imageProcessingService.processImage(any(ThumbnailDefinition.class)))
            .thenReturn(processedImageData);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/thumbnail/forge/remote/" + remoteUrlInPath);

        ResponseEntity<byte[]> response = imageDownloadController.resize(
            request, "remote", 100, 100, 80, false, false, "JPG", "", "");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(processedImageData, response.getBody());

        verify(restTemplate).getForObject(fixedRemoteUrl, byte[].class);
        verify(objectStorageService, never()).downloadFile(any(), any());

        ArgumentCaptor<ThumbnailDefinition> captor = ArgumentCaptor.forClass(ThumbnailDefinition.class);
        verify(imageProcessingService).processImage(captor.capture());
        ThumbnailDefinition def = captor.getValue();
        assertArrayEquals(originalImageData, def.getImageData());
    }

    @Test
    public void testResizeImage_RemoteMode_DownloadsFromRemote() throws Exception {
        String remoteUrlInPath = "https:/example.com/image.jpg"; // Spring strips one slash
        String fixedRemoteUrl = "https://example.com/image.jpg"; // Controller fixes it
        byte[] originalImageData = "remote image data".getBytes();
        byte[] processedImageData = "processed image data".getBytes();

        when(restTemplate.getForObject(fixedRemoteUrl, byte[].class)).thenReturn(originalImageData);
        when(imageProcessingService.processImage(any(ThumbnailDefinition.class))).thenReturn(processedImageData);

        ResponseEntity<byte[]> response = imageDownloadController.resizeImage("remote", "100x200q-75", remoteUrlInPath);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(processedImageData, response.getBody());

        verify(restTemplate).getForObject(fixedRemoteUrl, byte[].class);
        verify(objectStorageService, never()).downloadFile(any(), any());

        ArgumentCaptor<ThumbnailDefinition> captor = ArgumentCaptor.forClass(ThumbnailDefinition.class);
        verify(imageProcessingService).processImage(captor.capture());
        ThumbnailDefinition definition = captor.getValue();
        assertArrayEquals(originalImageData, definition.getImageData());
        assertEquals(100, definition.getWidth());
        assertEquals(200, definition.getHeight());
        assertEquals(75, definition.getQuality());
    }

    @Test
    public void testResize_InvalidMode_ReturnsBadRequest() throws Exception {
        String imagePath = "test/image.jpg";
        byte[] originalImageData = "original image data".getBytes();

        when(objectStorageService.downloadFile("original-image", imagePath)).thenReturn(originalImageData);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/thumbnail/forge/invalid/" + imagePath);

        ResponseEntity<byte[]> response = imageDownloadController.resize(
            request, "invalid", 100, 100, 80, false, false, "JPG", "", "");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testResizeImage_InvalidMode_ReturnsBadRequest() throws Exception {
        String imagePath = "test/image.jpg";
        byte[] originalImageData = "original image data".getBytes();

        when(objectStorageService.downloadFile("original-image", imagePath)).thenReturn(originalImageData);

        ResponseEntity<byte[]> response = imageDownloadController.resizeImage("invalid", "100x200", imagePath);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
