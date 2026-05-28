package com.github.cloudgyb.ai.knowledge.server.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * @author cloudgyb
 * @since 2026/5/18 14:37
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验。
        registry.addInterceptor(new SaInterceptor(handle -> {
                    // 获取当前请求的 DispatcherType
                    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                    if (requestAttributes != null) {
                        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                        if (DispatcherType.ASYNC.equals(request.getDispatcherType())) {
                            // 异步派发请求跳过认证，避免出现获取不到 SaToken 上下文导致的异常
                            return;
                        }
                    }
                    StpUtil.checkLogin();
                }))
                .addPathPatterns("/**")
                .excludePathPatterns("/captcha")
                .excludePathPatterns("/login")
                .excludePathPatterns("/error");
    }
}