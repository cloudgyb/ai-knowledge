package com.github.cloudgyb.ai.knowledge.mcp.config;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MCP工具注解
 * 标记一个方法为MCP工具
 *
 * @author cloudgyb
 * @since 2023/5/27
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface McpTool {
    /**
     * 工具的名称
     */
    @AliasFor("name")
    String value() default "";

    /**
     * 工具的描述
     */
    String description() default "";

    /**
     * 工具的名称
     */
    @AliasFor("value")
    String name() default "";
}
