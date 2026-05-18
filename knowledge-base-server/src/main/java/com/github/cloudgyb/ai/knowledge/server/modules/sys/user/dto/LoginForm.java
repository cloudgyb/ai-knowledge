package com.github.cloudgyb.ai.knowledge.server.modules.sys.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author cloudgyb
 * @since 2026/5/18 9:51
 */
@Getter
@Setter
public class LoginForm {
    @NotEmpty
    private String username;
    @NotEmpty
    private String password;
    @NotEmpty
    private String captchaCode;
    @NotEmpty
    private String uuid;
}
