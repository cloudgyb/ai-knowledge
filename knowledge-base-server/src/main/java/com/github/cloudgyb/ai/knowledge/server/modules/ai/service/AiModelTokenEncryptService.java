package com.github.cloudgyb.ai.knowledge.server.modules.ai.service;

import com.github.cloudgyb.ai.knowledge.server.modules.commons.BusinessException;
import com.github.cloudgyb.ai.knowledge.server.modules.commons.util.RSAUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * AI 模型 token 加解密服务
 *
 * @author cloudgyb
 * @since 2026/4/28 10:41
 */
@Service
public class AiModelTokenEncryptService implements InitializingBean {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final static String privateKeyFilename = "private_key.pem";
    private final static String publicKeyFilename = "public_key.pem";
    private volatile KeyPair keyPair;


    public String encryptTokenByPrivateKey(String token) {
        try {
            return RSAUtil.encryptByPrivateKey(token, keyPair.getPrivate());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public String encryptTokenByPublicKey(String token) {
        try {
            return RSAUtil.encryptByPublicKey(token, keyPair.getPublic());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public String decryptTokenByPublicKey(String encryptedToken) {
        try {
            return RSAUtil.decryptByPublicKeyToString(encryptedToken, keyPair.getPublic());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String decryptTokenByPrivateKey(String encryptedToken) {
        try {
            return RSAUtil.decryptByPrivateKeyToString(encryptedToken, keyPair.getPrivate());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getPublicKey() {
        ClassPathResource publicKeyFile = new ClassPathResource(publicKeyFilename);
        try {
            return publicKeyFile.getContentAsString(StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new BusinessException("获取公有 key 失败！");
        }
    }

    private void loadKeyPair() throws IOException {
        if (keyPair != null) {
            return;
        }
        synchronized (this) {
            if (keyPair != null) {
                return;
            }
            ClassPathResource privateKeyFile = new ClassPathResource(privateKeyFilename);
            ClassPathResource publicKeyFile = new ClassPathResource(publicKeyFilename);
            try {
                String privateKeyBase64 = privateKeyFile.getContentAsString(StandardCharsets.UTF_8).trim();
                String publicKeyBase64 = publicKeyFile.getContentAsString(StandardCharsets.UTF_8).trim();
                PrivateKey privateKey = RSAUtil.loadPrivateKey(privateKeyBase64);
                PublicKey publicKey = RSAUtil.loadPublicKey(publicKeyBase64);
                keyPair = new KeyPair(publicKey, privateKey);
            } catch (Exception e) {
                throw new IOException("加载公私有key失败", e);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        loadKeyPair();
        if (log.isInfoEnabled()) {
            log.info("公私钥加载成功！");
        }
    }
}
