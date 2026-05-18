package com.github.cloudgyb.ai.knowledge.server.modules.sys.user.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.user.dto.CaptchaCode;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.user.dto.LoginForm;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.user.dto.LoginResult;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.user.entity.SysUserEntity;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.user.mapper.SysUserEntityMapper;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author cloudgyb
 * @since 2026/5/18 9:53
 */
@Service
public class LoginService {
    private final static String CAPTCHA_CODE_KEY = "captcha:code:";
    private final StringRedisTemplate redisTemplate;
    private final SysUserEntityMapper userEntityMapper;

    public LoginService(StringRedisTemplate redisTemplate,
                        SysUserEntityMapper userEntityMapper) {
        this.redisTemplate = redisTemplate;
        this.userEntityMapper = userEntityMapper;
    }

    public CaptchaCode captchaCode() {
        Captcha captcha = new SpecCaptcha();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String code = captcha.text();
        String base64 = captcha.toBase64();
        CaptchaCode captchaCode = new CaptchaCode();
        captchaCode.setCaptchaCodeImage(base64);
        captchaCode.setUuid(uuid);
        redisTemplate.opsForValue().set(CAPTCHA_CODE_KEY + uuid, code,2, TimeUnit.MINUTES);
        return captchaCode;
    }

    public LoginResult login(LoginForm form) {
        LoginResult loginResult = new LoginResult();
        String uuid = form.getUuid();
        String captchaCode = form.getCaptchaCode();
        String capCode = redisTemplate.opsForValue().getAndDelete(CAPTCHA_CODE_KEY + uuid);
        if (capCode == null || !capCode.equalsIgnoreCase(captchaCode)) {
            loginResult.setCode(-2);
            loginResult.setReason("验证码过期或验证码错误！");
            return loginResult;
        }
        String username = form.getUsername();
        String password = form.getPassword();
        SysUserEntity user = userEntityMapper.selectByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            loginResult.setCode(-1);
            loginResult.setReason("用户名或密码错误！");
            return loginResult;
        }
        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        String tokenValue = tokenInfo.tokenValue;
        loginResult.setToken(tokenValue);
        loginResult.setReason("成功！");
        loginResult.setCode(0);
        return loginResult;
    }

    public void logout() {
        StpUtil.logout();
    }
}
