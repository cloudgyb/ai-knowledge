package com.github.cloudgyb.ai.knowledge.server.modules.commons.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 非对称加密 RSA 工具类
 *
 * @author cloudgyb
 * @since 2026/4/28 10:25
 */
public class RSAUtil {

    private static final String ALGORITHM = "RSA";
    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final int KEY_SIZE = 2048;

    static {
        // 添加 BouncyCastle 提供者
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 生成 RSA 密钥对
     *
     * @return KeyPair 包含公钥和私钥
     * @throws NoSuchAlgorithmException 算法不存在异常
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM, "BC");
        keyPairGenerator.initialize(KEY_SIZE);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 生成 Base64 编码的公钥和私钥字符串
     *
     * @return String[] 数组，[0]为公钥，[1]为私钥
     * @throws NoSuchAlgorithmException 算法不存在异常
     */
    public static String[] generateKeyPairStrings() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPair keyPair = generateKeyPair();
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        return new String[]{publicKey, privateKey};
    }

    /**
     * 从 Base64 字符串加载公钥
     *
     * @param publicKeyBase64 Base64 编码的公钥字符串
     * @return PublicKey 公钥对象
     * @throws Exception 异常
     */
    public static PublicKey loadPublicKey(String publicKeyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, "BC");
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 从 Base64 字符串加载私钥
     *
     * @param privateKeyBase64 Base64 编码的私钥字符串
     * @return PrivateKey 私钥对象
     * @throws Exception 异常
     */
    public static PrivateKey loadPrivateKey(String privateKeyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, "BC");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 使用公钥加密数据
     *
     * @param data      待加密的明文数据
     * @param publicKey 公钥
     * @return Base64 编码的密文
     * @throws Exception 异常
     */
    public static String encryptByPublicKey(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION, "BC");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(data);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * 使用公钥加密字符串
     *
     * @param plaintext 待加密的明文字符串
     * @param publicKey 公钥
     * @return Base64 编码的密文
     * @throws Exception 异常
     */
    public static String encryptByPublicKey(String plaintext, PublicKey publicKey) throws Exception {
        return encryptByPublicKey(plaintext.getBytes(StandardCharsets.UTF_8), publicKey);
    }

    /**
     * 使用公钥加密字符串（使用 Base64 公钥字符串）
     *
     * @param plaintext       待加密的明文字符串
     * @param publicKeyBase64 Base64 编码的公钥字符串
     * @return Base64 编码的密文
     * @throws Exception 异常
     */
    public static String encryptByPublicKey(String plaintext, String publicKeyBase64) throws Exception {
        PublicKey publicKey = loadPublicKey(publicKeyBase64);
        return encryptByPublicKey(plaintext, publicKey);
    }

    /**
     * 使用私钥解密数据
     *
     * @param encryptedData Base64 编码的密文数据
     * @param privateKey    私钥
     * @return 解密后的明文数据
     * @throws Exception 异常
     */
    public static byte[] decryptByPrivateKey(String encryptedData, PrivateKey privateKey) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION, "BC");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedBytes);
    }

    /**
     * 使用私钥解密数据并转换为字符串
     *
     * @param encryptedData Base64 编码的密文数据
     * @param privateKey    私钥
     * @return 解密后的明文字符串
     * @throws Exception 异常
     */
    public static String decryptByPrivateKeyToString(String encryptedData, PrivateKey privateKey) throws Exception {
        byte[] decryptedBytes = decryptByPrivateKey(encryptedData, privateKey);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * 使用私钥解密数据（使用 Base64 私钥字符串）
     *
     * @param encryptedData    Base64 编码的密文数据
     * @param privateKeyBase64 Base64 编码的私钥字符串
     * @return 解密后的明文字符串
     * @throws Exception 异常
     */
    public static String decryptByPrivateKeyToString(String encryptedData, String privateKeyBase64) throws Exception {
        PrivateKey privateKey = loadPrivateKey(privateKeyBase64);
        return decryptByPrivateKeyToString(encryptedData, privateKey);
    }

    /**
     * 使用私钥加密数据（数字签名场景）
     *
     * @param data       待加密的数据
     * @param privateKey 私钥
     * @return Base64 编码的密文
     * @throws Exception 异常
     */
    public static String encryptByPrivateKey(byte[] data, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION, "BC");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] encryptedBytes = cipher.doFinal(data);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * 使用私钥加密字符串（数字签名场景）
     *
     * @param plaintext  待加密的明文字符串
     * @param privateKey 私钥
     * @return Base64 编码的密文
     * @throws Exception 异常
     */
    public static String encryptByPrivateKey(String plaintext, PrivateKey privateKey) throws Exception {
        return encryptByPrivateKey(plaintext.getBytes(StandardCharsets.UTF_8), privateKey);
    }

    /**
     * 使用私钥加密字符串（使用 Base64 私钥字符串）
     *
     * @param plaintext        待加密的明文字符串
     * @param privateKeyBase64 Base64 编码的私钥字符串
     * @return Base64 编码的密文
     * @throws Exception 异常
     */
    public static String encryptByPrivateKey(String plaintext, String privateKeyBase64) throws Exception {
        PrivateKey privateKey = loadPrivateKey(privateKeyBase64);
        return encryptByPrivateKey(plaintext, privateKey);
    }

    /**
     * 使用公钥解密数据（验证签名场景）
     *
     * @param encryptedData Base64 编码的密文数据
     * @param publicKey     公钥
     * @return 解密后的明文数据
     * @throws Exception 异常
     */
    public static byte[] decryptByPublicKey(String encryptedData, PublicKey publicKey) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION, "BC");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        return cipher.doFinal(encryptedBytes);
    }

    /**
     * 使用公钥解密数据并转换为字符串（验证签名场景）
     *
     * @param encryptedData Base64 编码的密文数据
     * @param publicKey     公钥
     * @return 解密后的明文字符串
     * @throws Exception 异常
     */
    public static String decryptByPublicKeyToString(String encryptedData, PublicKey publicKey) throws Exception {
        byte[] decryptedBytes = decryptByPublicKey(encryptedData, publicKey);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * 使用公钥解密数据（使用 Base64 公钥字符串）
     *
     * @param encryptedData   Base64 编码的密文数据
     * @param publicKeyBase64 Base64 编码的公钥字符串
     * @return 解密后的明文字符串
     * @throws Exception 异常
     */
    public static String decryptByPublicKeyToString(String encryptedData, String publicKeyBase64) throws Exception {
        PublicKey publicKey = loadPublicKey(publicKeyBase64);
        return decryptByPublicKeyToString(encryptedData, publicKey);
    }
}
