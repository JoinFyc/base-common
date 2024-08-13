package com.mengxiang.base.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA分对称加解密工具类
 *
 * @author ice
 * @version 1.0
 * @date 2019/5/25 2:45 PM
 */
public final class RSAUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RSAUtils.class);

    private static final String RSA_ALGORITHM = "RSA";
    private static final int DEFAULT_KEY_SIZE = 1024;

    private RSAUtils() {
    }

    /**
     * 获取公私钥对
     *
     * @return 公私钥对
     */
    public static KeyPair getKeyPair() {
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("获取公私钥对失败", e);
        }
        if (generator == null) {
            return null;
        }
        generator.initialize(DEFAULT_KEY_SIZE);
        return generator.generateKeyPair();
    }

    /**
     * 公钥加密
     *
     * @param key     BASE64编码后的公钥
     * @param content 待加密内容
     * @return 加密后的内容
     */
    public static byte[] encryptByPublicKey(String key, String content) {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        RSAPublicKey publicKey = getRSAPublicKey(keyBytes);
        if (publicKey == null) {
            return null;
        }
        return encryptByKey(publicKey, content.getBytes());
    }

    /**
     * 公钥加密
     *
     * @param key     BASE64编码后的公钥
     * @param content 待加密内容
     * @param charset 待加密内容的字符编码
     * @return 加密后的内容
     * @throws UnsupportedEncodingException 字符编码不支持时抛出该异常
     */
    public static byte[] encryptByPublicKey(String key, String content, String charset)
            throws UnsupportedEncodingException {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        RSAPublicKey publicKey = getRSAPublicKey(keyBytes);
        if (publicKey == null) {
            return null;
        }
        return encryptByKey(publicKey, content.getBytes(charset));
    }

    /**
     * 私钥加密
     *
     * @param key     BASE64编码后的私钥
     * @param content 待加密内容
     * @return 加密后的内容
     */
    public static byte[] encryptByPrivateKey(String key, String content) {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        RSAPrivateKey privateKey = getRSAPrivateKey(keyBytes);
        if (privateKey == null) {
            return null;
        }
        return encryptByKey(privateKey, content.getBytes());
    }

    /**
     * 私钥加密
     *
     * @param key     BASE64编码后的私钥
     * @param content 待加密内容
     * @param charset 待加密内容的字符编码
     * @return 加密后的内容
     * @throws UnsupportedEncodingException 字符编码不支持时抛出该异常
     */
    public static byte[] encryptByPrivateKey(String key, String content, String charset)
            throws UnsupportedEncodingException {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        RSAPrivateKey privateKey = getRSAPrivateKey(keyBytes);
        if (privateKey == null) {
            return null;
        }
        return encryptByKey(privateKey, content.getBytes(charset));
    }

    /**
     * 公钥解密
     *
     * @param key     BASE64编码后的公钥
     * @param content 待解密内容
     * @return 解密后的内容
     */
    public static String decryptByPublicKey(String key, byte[] content) {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        RSAPublicKey publicKey = getRSAPublicKey(keyBytes);
        if (publicKey == null) {
            return null;
        }
        byte[] result = decryptByKey(publicKey, content);
        return new String(result);
    }

    /**
     * 公钥解密
     *
     * @param key     BASE64编码后的公钥
     * @param content 待解密内容
     * @param charset 结果字符串的字符编码
     * @return 解密后的内容
     * @throws UnsupportedEncodingException 字符编码不支持时抛出该异常
     */
    public static String decryptByPublicKey(String key, byte[] content, String charset)
            throws UnsupportedEncodingException {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        RSAPublicKey publicKey = getRSAPublicKey(keyBytes);
        if (publicKey == null) {
            return null;
        }
        byte[] result = decryptByKey(publicKey, content);
        return new String(result, charset);
    }

    /**
     * 私钥解密
     *
     * @param key     BASE64编码后的私钥
     * @param content 待解密内容
     * @return 解密后的内容
     */
    public static String decryptByPrivateKey(String key, byte[] content) {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        RSAPrivateKey privateKey = getRSAPrivateKey(keyBytes);
        if (privateKey == null) {
            return null;
        }
        byte[] result = decryptByKey(privateKey, content);
        return new String(result);
    }

    /**
     * 私钥解密
     *
     * @param key     BASE64编码后的私钥
     * @param content 待解密内容
     * @param charset 结果字符串的字符编码
     * @return 解密后的内容
     * @throws UnsupportedEncodingException 字符编码不支持时抛出该异常
     */
    public static String decryptByPrivateKey(String key, byte[] content, String charset)
            throws UnsupportedEncodingException {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        RSAPrivateKey privateKey = getRSAPrivateKey(keyBytes);
        if (privateKey == null) {
            return null;
        }
        byte[] result = decryptByKey(privateKey, content);
        return new String(result, charset);
    }

    private static byte[] encryptByKey(Key key, byte[] content) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(content);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error("加密失败", e);
            return null;
        }
    }

    private static byte[] decryptByKey(Key key, byte[] content) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(content);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error("解密失败", e);
            return null;
        }
    }

    private static RSAPublicKey getRSAPublicKey(byte[] key) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.error("生成公钥失败", e);
            return null;
        }
    }

    private static RSAPrivateKey getRSAPrivateKey(byte[] key) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.error("生成私钥失败", e);
            return null;
        }
    }
}
