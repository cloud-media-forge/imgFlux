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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
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
        when(imageProcessingService.processImage(originalImageData, 100, 100, 80, false, false, "JPG"))
            .thenReturn(processedImageData);

        // Create mock HttpServletRequest
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/thumbnail/forge/" + imagePath);

        // Call the method under test
        ResponseEntity<byte[]> response = imageDownloadController.downloadImage(
            request, 100, 100, 80, false, false, "JPG");

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
        verify(imageProcessingService).processImage(originalImageData, 100, 100, 80, false, false, "JPG");
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
        when(request.getRequestURI()).thenReturn("/api/v1/thumbnail/forge/" + imagePath);

        // Call the method under test without specifying processing parameters (use default values)
        ResponseEntity<byte[]> response = imageDownloadController.downloadImage(
            request, 0, 0, 80, false, false, "JPG");

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
        verify(imageProcessingService, never()).processImage(any(), anyInt(), anyInt(), anyInt(), anyBoolean(), anyBoolean(), anyString());
    }

    @Test
    public void testResizeImage_SuccessWithPathParamsAndFormatConversion() throws Exception {
        String imagePath = "test/image.jpg.webp";
        String sourcePath = "test/image.jpg";
        byte[] originalImageData = "original image data".getBytes();
        byte[] processedImageData = "processed image data".getBytes();

        when(objectStorageService.downloadFile("original-image", sourcePath)).thenReturn(originalImageData);
        when(imageProcessingService.processImage(any(ThumbnailDefinition.class))).thenReturn(processedImageData);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/thumbnail/resize/100x200q-75extrim/" + imagePath);

        ResponseEntity<byte[]> response = imageDownloadController.resizeImage(request, "100x200q-75extrim");

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
}
