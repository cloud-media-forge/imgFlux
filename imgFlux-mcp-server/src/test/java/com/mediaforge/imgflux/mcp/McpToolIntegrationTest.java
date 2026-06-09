package com.mediaforge.imgflux.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test demonstrating how an AI agent (MCP client) discovers and
 * calls image processing tools exposed by the ImgFlux MCP server.
 *
 * <p>Each test simulates a real AI agent workflow:
 * <ol>
 *   <li>Connect to the MCP server via SSE transport</li>
 *   <li>List available tools (the AI sees tool names + descriptions)</li>
 *   <li>Call a tool with JSON arguments (as the AI would)</li>
 *   <li>Verify the result</ * </ol>
 *
 * <p>Example AI prompts that would trigger these tool calls:
 * <ul>
 *   <li>"Resize this image to 200x150" → resizeImage tool</li>
 *   <li>"Remove the white borders from this image" → trimImage tool</li>
 *   <li>"Convert this PNG to JPEG" → convertImageFormat tool</li>
 *   <li>"What's the size of this image?" → getImageInfo tool</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class McpToolIntegrationTest {

    @LocalServerPort
    int port;

    private McpSyncClient mcpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        tempDir = Files.createTempDirectory("mcp-test-");

        String baseUrl = "http://localhost:" + port;
        var transport = new io.modelcontextprotocol.client.transport.HttpClientSseClientTransport(baseUrl);
        mcpClient = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(30))
                .build();
        mcpClient.initialize();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mcpClient != null) {
            mcpClient.close();
        }
        if (tempDir != null) {
            try (var stream = Files.walk(tempDir)) {
                stream.sorted(java.util.Comparator.reverseOrder()).map(Path::toFile).forEach(java.io.File::delete);
            }
        }
    }

    /**
     * AI prompt: "What image tools do you have?"
     *
     * <p>The AI agent connects to the MCP server and discovers all available tools
     * with their names, descriptions, and parameter schemas.
     */
    @Test
    @Order(1)
    void listTools_aiDiscoversAvailableImageTools() {
        List<McpSchema.Tool> tools = mcpClient.listTools().tools();

        assertFalse(tools.isEmpty(), "MCP server should expose tools");

        System.out.println("=== AI discovers these image tools ===");
        for (McpSchema.Tool tool : tools) {
            System.out.printf("  Tool: %-25s | %s%n", tool.name(),
                    tool.description() != null && tool.description().length() > 80
                            ? tool.description().substring(0, 80) + "..."
                            : tool.description());
        }

        List<String> names = tools.stream().map(McpSchema.Tool::name).toList();
        assertTrue(names.contains("resizeImage"), "Should have resizeImage tool");
        assertTrue(names.contains("trimImage"), "Should have trimImage tool");
        assertTrue(names.contains("convertImageFormat"), "Should have convertImageFormat tool");
        assertTrue(names.contains("translateImageText"), "Should have translateImageText tool");
        assertTrue(names.contains("getImageInfo"), "Should have getImageInfo tool");
        assertTrue(names.contains("processImage"), "Should have processImage tool");
    }

    /**
     * AI prompt: "Resize /tmp/test.png to 200x150 and save to /tmp/resized.png"
     */
    @Test
    @Order(2)
    void resizeImage_aiCallsResizeTool() throws Exception {
        Path input = createTestImage(400, 300, Color.BLUE, "input.png");
        Path output = tempDir.resolve("resized.png");

        Map<String, Object> args = Map.of(
                "inputPath", input.toString(),
                "outputPath", output.toString(),
                "width", 200,
                "height", 150,
                "quality", 80,
                "format", "PNG"
        );

        McpSchema.CallToolResult result = mcpClient.callTool(
                new McpSchema.CallToolRequest("resizeImage", args));

        assertNotNull(result);
        String content = extractTextContent(result);
        System.out.println("=== AI resize result: " + content + " ===");

        assertTrue(Files.exists(output), "Output file should exist");
        BufferedImage img = ImageIO.read(output.toFile());
        assertTrue(img.getWidth() <= 200, "Width should be <= 200");
        assertTrue(img.getHeight() <= 150, "Height should be <= 150");
    }

    /**
     * AI prompt: "Remove the white borders from /tmp/bordered.png and save to /tmp/trimmed.png"
     */
    @Test
    @Order(3)
    void trimImage_aiCallsTrimTool() throws Exception {
        Path input = createBorderedImage("bordered.png");
        Path output = tempDir.resolve("trimmed.png");

        Map<String, Object> args = Map.of(
                "inputPath", input.toString(),
                "outputPath", output.toString()
        );

        McpSchema.CallToolResult result = mcpClient.callTool(
                new McpSchema.CallToolRequest("trimImage", args));

        assertNotNull(result);
        String content = extractTextContent(result);
        System.out.println("=== AI trim result: " + content + " ===");

        assertTrue(Files.exists(output));
        BufferedImage img = ImageIO.read(output.toFile());
        assertTrue(img.getWidth() < 300, "Should trim horizontal borders");
        assertTrue(img.getHeight() < 300, "Should trim vertical borders");
    }

    /**
     * AI prompt: "Convert /tmp/test.png to JPEG format and save to /tmp/test.jpg"
     */
    @Test
    @Order(4)
    void convertFormat_aiCallsConvertTool() throws Exception {
        Path input = createTestImage(100, 100, Color.RED, "test.png");
        Path output = tempDir.resolve("test.jpg");

        Map<String, Object> args = Map.of(
                "inputPath", input.toString(),
                "outputPath", output.toString(),
                "format", "JPG",
                "quality", 85
        );

        McpSchema.CallToolResult result = mcpClient.callTool(
                new McpSchema.CallToolRequest("convertImageFormat", args));

        assertNotNull(result);
        String content = extractTextContent(result);
        System.out.println("=== AI convert result: " + content + " ===");

        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);
    }

    /**
     * AI prompt: "Tell me about this image: /tmp/test.png"
     */
    @Test
    @Order(5)
    void getImageInfo_aiCallsInfoTool() throws Exception {
        Path input = createTestImage(640, 480, Color.GREEN, "info.png");

        Map<String, Object> args = Map.of("inputPath", input.toString());

        McpSchema.CallToolResult result = mcpClient.callTool(
                new McpSchema.CallToolRequest("getImageInfo", args));

        assertNotNull(result);
        String content = extractTextContent(result);
        System.out.println("=== AI image info: " + content + " ===");

        assertTrue(content.contains("640x480"), "Should contain dimensions");
        assertTrue(content.contains("PNG"), "Should contain format");
    }

    /**
     * AI prompt: "Resize /tmp/bordered.png to 200x200, trim borders, and save to /tmp/processed.png"
     *
     * <p>This demonstrates the combined processImage tool.
     */
    @Test
    @Order(6)
    void processImage_aiCallsCombinedTool() throws Exception {
        Path input = createBorderedImage("combo.png");
        Path output = tempDir.resolve("processed.png");

        Map<String, Object> args = Map.of(
                "inputPath", input.toString(),
                "outputPath", output.toString(),
                "width", 200,
                "height", 200,
                "quality", 80,
                "format", "PNG",
                "trim", true,
                "extent", false,
                "srcLang", "",
                "toLang", ""
        );

        McpSchema.CallToolResult result = mcpClient.callTool(
                new McpSchema.CallToolRequest("processImage", args));

        assertNotNull(result);
        String content = extractTextContent(result);
        System.out.println("=== AI combined process: " + content + " ===");

        assertTrue(Files.exists(output));
        assertTrue(content.contains("resized") || content.contains("trimmed"), content);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Path createTestImage(int w, int h, Color color, String name) throws Exception {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, w, h);
        g.dispose();
        Path path = tempDir.resolve(name);
        ImageIO.write(img, "png", path.toFile());
        return path;
    }

    private Path createBorderedImage(String name) throws Exception {
        BufferedImage img = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 300, 300);
        g.setColor(Color.RED);
        g.fillRect(50, 50, 200, 200);
        g.dispose();
        Path path = tempDir.resolve(name);
        ImageIO.write(img, "png", path.toFile());
        return path;
    }

    private String extractTextContent(McpSchema.CallToolResult result) {
        for (Object content : result.content()) {
            if (content instanceof McpSchema.TextContent textContent) {
                return textContent.text();
            }
        }
        fail("No text content in MCP result");
        return null;
    }
}
