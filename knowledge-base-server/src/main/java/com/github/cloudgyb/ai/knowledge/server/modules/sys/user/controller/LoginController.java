package com.github.cloudgyb.ai.knowledge.server.modules.sys.user.controller;

import com.github.cloudgyb.ai.knowledge.server.modules.commons.ApiResponse;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.user.dto.CaptchaCode;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.user.dto.LoginForm;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.user.dto.LoginResult;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.user.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author cloudgyb
 * @since 2026/5/18 9:49
 */
@Slf4j
@RestController
@RequestMapping("")
public class LoginController {
    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/captcha")
    public ApiResponse<CaptchaCode> captchaCode() {
        CaptchaCode captchaCode = loginService.captchaCode();
        return ApiResponse.success(captchaCode);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResult> login(@Validated @RequestBody LoginForm loginForm) {
        LoginResult login = loginService.login(loginForm);
        return ApiResponse.success(login);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        loginService.logout();
        return ApiResponse.success();
    }
}
