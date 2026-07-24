package com.github.cloudgyb.ai.knowledge.mcp.config;

import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Assembles MCP server tools from annotated classes.
 *
 * @author cloudgyb
 */
@Component
public class McpServerToolAssembler implements ApplicationContextAware, BeanPostProcessor {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(McpServerToolAssembler.class);
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Nullable
    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        McpToolsComponent annotation = AnnotationUtils.findAnnotation(bean.getClass(), McpToolsComponent.class);
        if (annotation != null) {
            addToolToMcpServer(bean);
        }
        return bean;
    }

    private void addToolToMcpServer(Object bean) {
        Class<?> aClass = bean.getClass();
        Method[] methods = aClass.getMethods();
        for (Method method : methods) {
            McpTool mcpTool = AnnotationUtils.findAnnotation(method, McpTool.class);
            if (mcpTool != null) {
                String name = mcpTool.name();
                String description = mcpTool.description();
                Parameter[] parameters = method.getParameters();
                HashMap<String, Map<String, String>> methodParametersSchema = new HashMap<>();
                Map<String, Class<?>> methodParameterNameToTypeMap = new HashMap<>();
                for (Parameter parameter : parameters) {
                    methodParameterNameToTypeMap.put(parameter.getName(), parameter.getType());
                    methodParametersSchema.put(parameter.getName(), Map.of("type", classTypeToMcpType(parameter.getType())));
                }
                Map<String, Object> argumentsSchema = Map.of("type", "object",
                        "properties", methodParametersSchema, "required", methodParametersSchema.keySet());
                McpSchema.Tool tool = McpSchema.Tool
                        .builder(name, argumentsSchema).description(description).build();
                McpServerFeatures.AsyncToolSpecification toolSpecification = McpServerFeatures.AsyncToolSpecification.builder()
                        .tool(tool).callHandler((exchange, request) -> {
                            Map<String, Object> arguments = request.arguments();
                            if (logger.isDebugEnabled()) {
                                logger.debug("Calling tool [{}] with arguments [{}]", name, arguments);
                            }
                            List<Object> argumentsList = new ArrayList<>(methodParameterNameToTypeMap.size());
                            for (Parameter parameter : parameters) {
                                argumentsList.add(arguments.get(parameter.getName()));
                            }
                            try {
                                Object res = ReflectionUtils.invokeMethod(method, bean, argumentsList.toArray(new Object[0]));
                                if (res == null) {
                                    return Mono.just(McpSchema.CallToolResult.builder().isError(false).build());
                                }
                                return Mono.just(McpSchema.CallToolResult.builder().isError(false).addTextContent(res.toString()).build());
                            } catch (Exception e) {
                                logger.error("Error calling tool [{}] with arguments [{}]", name, arguments, e);
                                return Mono.just(McpSchema.CallToolResult.builder().isError(true).addTextContent(e.getMessage()).build());
                            }
                        }).build();
                if (logger.isDebugEnabled()) {
                    logger.debug("Adding tool [{}] to MCP server", name);
                }
                McpAsyncServer mcpAsyncServer = applicationContext.getBean(McpAsyncServer.class);
                mcpAsyncServer.addTool(toolSpecification).subscribe();
            }
        }
    }

    // ["array", "boolean", "integer", "null", "number", "object", "string"]
    private String classTypeToMcpType(Class<?> type) {
        if (type.isArray()) {
            return "array";
        }
        if (type == boolean.class || type == Boolean.class) return "boolean";
        if (type == int.class || type == Integer.class || type == byte.class || type == Byte.class) return "integer";
        if (type == double.class || type == Double.class || type == float.class || type == Float.class ||
                type.isAssignableFrom(Number.class)) return "number";
        if (type == String.class) return "string";
        return "object";
    }

}
