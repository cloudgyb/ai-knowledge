package com.github.cloudgyb.ai.knowledge.server.config;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * MCP提供者配置类
 *
 * @author cloudgyb
 * @since 2026/08/11
 **/
@Configuration(proxyBeanMethods = false)
public class McpProviderConfiguration {
    @Getter
    @ConfigurationProperties(prefix = "mcp.server")
    @Configuration
    public static class McpServerConfig {
        String url = "http://localhost:9000/mcp";
        Duration timeout = Duration.ofSeconds(10); // Default timeout
        String key = "mcp-inner"; // Default key
        Duration pingTimeout = Duration.ofSeconds(8); // Default ping timeout
        Boolean logRequests = false; // Default log requests
        Boolean logResponses = false; // Default log responses
    }

    /**
     * 创建HTTP MCP传输
     *
     * @return StreamableHttpMcpTransport
     */
    @Bean
    public StreamableHttpMcpTransport streamableHttpMcpTransport(McpServerConfig mcpServerConfig) {
        return StreamableHttpMcpTransport.builder()
                .logRequests(mcpServerConfig.getLogRequests())
                .logResponses(mcpServerConfig.getLogResponses())
                .setHttpVersion1_1()
                .url(mcpServerConfig.getUrl())
                //.subsidiaryChannel(true)
                .timeout(mcpServerConfig.getTimeout())
                .build();
    }

    /**
     * 创建MCP客户端
     *
     * @return McpClient
     */
    @Bean
    public McpClient mcpClient(StreamableHttpMcpTransport streamableHttpMcpTransport, McpServerConfig mcpServerConfig) {
        return DefaultMcpClient.builder()
                .transport(streamableHttpMcpTransport)
                .key(mcpServerConfig.getKey())
                .pingTimeout(mcpServerConfig.getPingTimeout())
                .build();
    }

    /**
     * 创建MCP工具提供者
     *
     * @return McpToolProvider
     */
    @Bean
    public McpToolProvider mcpToolProvider(McpClient mcpClient) {
        return McpToolProvider.builder()
                .mcpClients(mcpClient)
                .toolNameMapper((client, toolSpecification) ->
                        client.key() + "_" + toolSpecification.name()
                )
                .build();
    }
}
