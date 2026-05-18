package com.github.cloudgyb.ai.knowledge.server.modules.sys.user.dto;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author cloudgyb
 * @since 2026/5/18 10:00
 */
@Getter
@Setter
public class LoginResult {
    private String token;
    private String reason;
    private int code;
}
