package com.github.cloudgyb.ai.knowledge.server.modules.ai.dto;

import com.github.cloudgyb.ai.knowledge.server.modules.ai.AiModelType;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModelConfig;
import com.github.cloudgyb.ai.knowledge.server.modules.commons.validation.Group;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.ai.domain.SysAiModelProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * AI 模型数据传输对象
 *
 * @author cloudgyb
 * @since 2026/3/13 10:41
 */
@Getter
@Setter
public class AiModelDTO {
    @NotNull(message = "id不能为空", groups = {Group.Update.class})
    private Integer id;

    /**
     * 自定义名称
     */
    @NotBlank(message = "模型名称不能为空", groups = {Group.Add.class, Group.Update.class})
    private String customName;

    /**
     * 模型名称
     */
    @NotBlank(message = "模型不能为空", groups = {Group.Add.class, Group.Update.class})
    private String modelName;

    /**
     * 模型类型
     */
    @NotNull(message = "模型类型不能为空", groups = {Group.Add.class, Group.Update.class})
    private AiModelType modelType;

    /**
     * 模型API URL
     */
    @NotBlank(message = "模型API URL不能为空", groups = {Group.Add.class, Group.Update.class})
    private String modelUrl;

    /**
     * 模型API Key
     */
    @NotBlank(message = "模型API Key不能为空", groups = {Group.Add.class, Group.Update.class})
    private String modelApiKey;

    /**
     * 模型API密钥
     */
    private String modelApiSecret;

    /**
     * 模型提供商 id
     */
    @NotNull(message = "模型提供商不能为空", groups = {Group.Add.class, Group.Update.class})
    private Integer providerId;

    /**
     * 模型状态，0未激活1已激活
     */
    @NotNull(message = "模型状态不能为空", groups = {Group.Add.class, Group.Update.class})
    private Integer status;

    private SysAiModelProvider provider;

    @NotNull(message = "模型配置不能为空", groups = {Group.Add.class, Group.Update.class})
    private AiModelConfig config;
}
