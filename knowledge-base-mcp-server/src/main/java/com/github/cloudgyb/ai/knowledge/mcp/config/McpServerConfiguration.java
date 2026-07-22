package com.github.cloudgyb.ai.knowledge.mcp.config;

import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * MCP Server Configuration
 *
 * @author cloudgyb
 */
@Configuration(proxyBeanMethods = false)
public class McpServerConfiguration {

    @Bean
    public HttpServletStreamableServerTransportProvider httpServletStreamableServerTransportProvider() {
        return HttpServletStreamableServerTransportProvider.builder()
                .mcpEndpoint("/mcp")
                .build();
    }

    @Bean
    public ServletRegistrationBean<HttpServletStreamableServerTransportProvider> servletRegistrationBean(
            HttpServletStreamableServerTransportProvider httpServletStreamableServerTransportProvider) {
        return new ServletRegistrationBean<>(httpServletStreamableServerTransportProvider, "/mcp");
    }

    @Bean
    public McpAsyncServer mcpServer(HttpServletStreamableServerTransportProvider httpServletStreamableServerTransportProvider) {
        McpAsyncServer mcpAsyncServer = McpServer.async(httpServletStreamableServerTransportProvider)
                .serverInfo(McpSchema.Implementation.builder("MCP Server", "0.0.1").build())
                .capabilities(McpSchema
                        .ServerCapabilities
                        .builder()
                        .completions()
                        .logging()
                        .resources(true, true)
                        .tools(true)
                        .build())
                .build();
        McpSchema.Tool tool = McpSchema.Tool
                .builder("test", Map.of("type", "object", "properties", Map.of("test", Map.of("type", "string", "description", "test")))).description("test tool").build();
        McpServerFeatures.AsyncToolSpecification toolSpecification = McpServerFeatures.AsyncToolSpecification.builder()
                .tool(tool).callHandler((exchange, request) -> {
                    Map<String, Object> arguments = request.arguments();
                    System.out.println(arguments);
                    return Mono.just(McpSchema.CallToolResult.builder().isError(false).addTextContent("test success").build());
                }).build();
        mcpAsyncServer.addTool(toolSpecification).subscribe();
        var asyncResourceSpecification = new McpServerFeatures.AsyncResourceSpecification(
                McpSchema.Resource.builder("custom://resource", "name")
                        .description("description")
                        .mimeType("text/plain")
                        .build(),
                (exchange, request) -> {
                    // Resource read implementation
                    return Mono.just(McpSchema.ReadResourceResult.builder(List.of()).build());
                }
        );
        mcpAsyncServer.addResource(asyncResourceSpecification).subscribe();
        return mcpAsyncServer;
    }
}
