package com.mediaforge.imgflux.mcp;

import com.mediaforge.imgflux.mcp.tools.ImageTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.mediaforge.imgflux")
public class ImgFluxMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImgFluxMcpApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider imageToolsProvider(ImageTools imageTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(imageTools)
                .build();
    }
}
