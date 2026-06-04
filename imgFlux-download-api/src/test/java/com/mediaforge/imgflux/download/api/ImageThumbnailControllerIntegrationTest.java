package com.mediaforge.imgflux.download.api;

import com.mediaforge.imgflux.download.ImageDownloadApplication;
import com.mediaforge.imgflux.engine.gm.ImageProcessingService;
import com.mediaforge.imgflux.engine.gm.ThumbnailDefinition;
import com.mediaforge.imgflux.engine.service.storage.ObjectStorageService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ImageDownloadApplication.class)
@AutoConfigureMockMvc
public class ImageThumbnailControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageProcessingService imageProcessingService;

    @MockBean
    private ObjectStorageService objectStorageService;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    public void testResizeEndpointGeneratesThumbnail() throws Exception {
        byte[] originalImageData = "original image data".getBytes();
        byte[] processedImageData = "processed image data".getBytes();

        when(objectStorageService.downloadFile("original-image", "folder/image.png"))
                .thenReturn(originalImageData);
        when(imageProcessingService.processImage(any(ThumbnailDefinition.class)))
                .thenReturn(processedImageData);

        mockMvc.perform(get("/api/v1/thumbnail/resize/local/120x80q-70extrimrans:ja:en/folder/image.png.webp"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/webp"))
                .andExpect(content().bytes(processedImageData));

        ArgumentCaptor<ThumbnailDefinition> captor = ArgumentCaptor.forClass(ThumbnailDefinition.class);
        verify(imageProcessingService).processImage(captor.capture());
        ThumbnailDefinition definition = captor.getValue();
        assertArrayEquals(originalImageData, definition.getImageData());
        assertEquals(120, definition.getWidth());
        assertEquals(80, definition.getHeight());
        assertEquals(70, definition.getQuality());
        assertEquals(true, definition.isExtent());
        assertEquals(true, definition.isTrim());
        assertEquals("WEBP", definition.getToFormat());
        assertEquals("ja", definition.getSrcLang());
        assertEquals("en", definition.getToLang());
    }

    @Test
    public void testResizeEndpointRemoteMode() throws Exception {
        byte[] originalImageData = "remote image data".getBytes();
        byte[] processedImageData = "processed image data".getBytes();
        String remoteUrlInPath = "https:/example.com/image.jpg"; // Spring strips one slash
        String fixedRemoteUrl = "https://example.com/image.jpg"; // Controller fixes it

        when(restTemplate.getForObject(eq(fixedRemoteUrl), eq(byte[].class)))
                .thenReturn(originalImageData);
        when(imageProcessingService.processImage(any(ThumbnailDefinition.class)))
                .thenReturn(processedImageData);

        mockMvc.perform(get("/api/v1/thumbnail/resize/remote/120x80q-70/" + remoteUrlInPath))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(content().bytes(processedImageData));

        verify(restTemplate).getForObject(eq(fixedRemoteUrl), eq(byte[].class));
        verify(objectStorageService, never()).downloadFile(any(), any());

        ArgumentCaptor<ThumbnailDefinition> captor = ArgumentCaptor.forClass(ThumbnailDefinition.class);
        verify(imageProcessingService).processImage(captor.capture());
        ThumbnailDefinition definition = captor.getValue();
        assertArrayEquals(originalImageData, definition.getImageData());
        assertEquals(120, definition.getWidth());
        assertEquals(80, definition.getHeight());
        assertEquals(70, definition.getQuality());
    }

    @Test
    public void testForgeEndpointLocalMode() throws Exception {
        byte[] originalImageData = "original image data".getBytes();
        byte[] processedImageData = "processed image data".getBytes();

        when(objectStorageService.downloadFile("original-image", "folder/image.png"))
                .thenReturn(originalImageData);
        when(imageProcessingService.processImage(any(ThumbnailDefinition.class)))
                .thenReturn(processedImageData);

        mockMvc.perform(get("/api/v1/thumbnail/forge/local/folder/image.png")
                .param("width", "120")
                .param("height", "80")
                .param("quality", "70"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(content().bytes(processedImageData));

        verify(objectStorageService).downloadFile("original-image", "folder/image.png");
    }

    @Test
    public void testForgeEndpointRemoteMode() throws Exception {
        byte[] originalImageData = "remote image data".getBytes();
        byte[] processedImageData = "processed image data".getBytes();
        String remoteUrlInPath = "https:/example.com/image.jpg"; // Spring strips one slash
        String fixedRemoteUrl = "https://example.com/image.jpg"; // Controller fixes it

        when(restTemplate.getForObject(eq(fixedRemoteUrl), eq(byte[].class)))
                .thenReturn(originalImageData);
        when(imageProcessingService.processImage(any(ThumbnailDefinition.class)))
                .thenReturn(processedImageData);

        mockMvc.perform(get("/api/v1/thumbnail/forge/remote/" + remoteUrlInPath)
                .param("width", "120")
                .param("height", "80")
                .param("quality", "70"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(content().bytes(processedImageData));

        verify(restTemplate).getForObject(eq(fixedRemoteUrl), eq(byte[].class));
        verify(objectStorageService, never()).downloadFile(any(), any());
    }

    @Test
    public void testResizeEndpointInvalidMode() throws Exception {
        mockMvc.perform(get("/api/v1/thumbnail/resize/invalid/120x80/folder/image.png.webp"))
                .andExpect(status().isBadRequest());
    }
}
