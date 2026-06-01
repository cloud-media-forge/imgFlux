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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    public void testResizeEndpointGeneratesThumbnail() throws Exception {
        byte[] originalImageData = "original image data".getBytes();
        byte[] processedImageData = "processed image data".getBytes();

        when(objectStorageService.downloadFile("original-image", "folder/image.png"))
                .thenReturn(originalImageData);
        when(imageProcessingService.processImage(any(ThumbnailDefinition.class)))
                .thenReturn(processedImageData);

        mockMvc.perform(get("/api/v1/thumbnail/resize/120x80q-70extrim/folder/image.png.webp"))
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
        assertEquals("WEBP", definition.getFormat());
    }
}
