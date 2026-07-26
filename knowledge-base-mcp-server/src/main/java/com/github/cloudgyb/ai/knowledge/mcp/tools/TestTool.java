package com.github.cloudgyb.ai.knowledge.mcp.tools;

import com.github.cloudgyb.ai.knowledge.mcp.config.McpTool;
import com.github.cloudgyb.ai.knowledge.mcp.config.McpToolsComponent;

@McpToolsComponent
public class TestTool {

    @McpTool(name = "add", description = "将两个数相加")
    public int add(int a, int b) {
        return a + b;
    }
}
