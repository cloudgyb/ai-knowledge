package com.github.cloudgyb.ai.knowledge.server.modules.sys.user.dto;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author cloudgyb
 * @since 2026/5/18 10:09
 */
@Getter
@Setter
public class CaptchaCode {
    private String captchaCodeImage;
    private String uuid;
}
