package com.github.cloudgyb.ai.knowledge.mcp.config;

import io.modelcontextprotocol.server.transport.ServerTransportSecurityException;
import io.modelcontextprotocol.server.transport.ServerTransportSecurityValidator;

import java.util.List;
import java.util.Map;

public class McpServerSecurityValidator implements ServerTransportSecurityValidator {
    @Override
    public void validateHeaders(Map<String, List<String>> headers) throws ServerTransportSecurityException {

        if (!headers.containsKey("Authorization")) {
            throw new ServerTransportSecurityException(401, "Authorization header is missing");
        }

        List<String> authorization = headers.get("Authorization");
        if (authorization == null || authorization.isEmpty() || !authorization.getFirst().equals("Bearer token")) {
            throw new ServerTransportSecurityException(401, "Invalid Authorization header");
        }

    }
}
