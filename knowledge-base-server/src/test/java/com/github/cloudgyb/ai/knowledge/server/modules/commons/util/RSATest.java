package com.github.cloudgyb.ai.knowledge.server.modules.commons.util;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * RSA 工具类测试
 *
 * @author cloudgyb
 * @since 2026/4/28
 */
public class RSATest {

    public static void main(String[] args) {
        try {
            System.out.println("========== RSA 工具类测试 ==========\n");

            // 1. 生成密钥对
            System.out.println("1. 生成 RSA 密钥对...");
            String[] keyPairStrings = RSAUtil.generateKeyPairStrings();
            String publicKeyStr = keyPairStrings[0];
            String privateKeyStr = keyPairStrings[1];

            System.out.println("公钥 (Base64): " + publicKeyStr);
            System.out.println("私钥 (Base64): " + privateKeyStr);

            // 2. 测试公钥加密，私钥解密
            System.out.println("2. 测试公钥加密，私钥解密...");
            String originalText = "Hello, RSA Encryption! 你好，RSA加密！";
            System.out.println("原始明文: " + originalText);

            String encryptedByPublic = RSAUtil.encryptByPublicKey(originalText, publicKeyStr);
            System.out.println("公钥加密后 (Base64): " + encryptedByPublic.substring(0, 50) + "...");

            String decryptedByPrivate = RSAUtil.decryptByPrivateKeyToString(encryptedByPublic, privateKeyStr);
            System.out.println("私钥解密后: " + decryptedByPrivate);
            System.out.println("解密是否成功: " + originalText.equals(decryptedByPrivate) + "\n");

            // 3. 测试私钥加密，公钥解密（数字签名场景）
            System.out.println("3. 测试私钥加密，公钥解密（数字签名场景）...");
            String signText = "This is a digital signature test. 这是数字签名测试。";
            System.out.println("原始数据: " + signText);

            String encryptedByPrivate = RSAUtil.encryptByPrivateKey(signText, privateKeyStr);
            System.out.println("私钥加密后 (Base64): " + encryptedByPrivate.substring(0, 50) + "...");

            String decryptedByPublic = RSAUtil.decryptByPublicKeyToString(encryptedByPrivate, publicKeyStr);
            System.out.println("公钥解密后: " + decryptedByPublic);
            System.out.println("解密是否成功: " + signText.equals(decryptedByPublic) + "\n");

            // 4. 测试 KeyPair 对象方式
            System.out.println("4. 测试使用 KeyPair 对象...");
            KeyPair keyPair = RSAUtil.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            String testData = "Test with KeyPair object. 使用密钥对对象测试。";
            System.out.println("原始数据: " + testData);

            String encrypted = RSAUtil.encryptByPublicKey(testData, publicKey);
            System.out.println("公钥加密后: " + encrypted.substring(0, 50) + "...");

            String decrypted = RSAUtil.decryptByPrivateKeyToString(encrypted, privateKey);
            System.out.println("私钥解密后: " + decrypted);
            System.out.println("解密是否成功: " + testData.equals(decrypted) + "\n");

            System.out.println("========== 所有测试完成 ==========");

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
