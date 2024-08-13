package com.mengxiang.base.datatask.upload.provider;

//import com.mengxiang.fileupload.dto.ByteArrayMultipartFile;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * 签名算法 sha256Hex(md5Hex(file) + appid + secret)
 */
public class Signature {

    /**
     * 使用密钥进行签名
     *
     * @param fileBytes   文件
     * @param appId  应用标识
     * @param secret 密钥
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static String sign(byte[] fileBytes,
                              String appId,
                              String secret) throws IOException, GeneralSecurityException {
        return DigestUtils.sha256Hex(DigestUtils.md5Hex(fileBytes) + appId + secret);
    }

    /**
     * 使用密钥进行签名
     *
     * @param file   文件
     * @param appId  应用标识
     * @param secret 密钥
     * @return
     * @throws IOException
     * @throws GeneralSecurityException

    public static String sign(MultipartFile file,
                              String appId,
                              String secret) throws IOException, GeneralSecurityException {
        return DigestUtils.sha256Hex(DigestUtils.md5Hex(file.getBytes()) + appId + secret);
    }
     */
    /**
     * 验证签名
     *
     * @param file      文件
     * @param appId     应用标识
     * @param secret    密钥
     * @param signature 签名数据
     * @return True 签名验证通过 False 签名验证失败

    public static boolean check(MultipartFile file, String appId, String secret, String signature) throws IOException, GeneralSecurityException {
        return sign(file, appId, secret).equals(signature);
    }
     */
    /*
    public static void main(String[] args) throws IOException, GeneralSecurityException {
        MultipartFile file = new ByteArrayMultipartFile("test.jpg", "hello!".getBytes());
        String appId = "appId";
        String secret = "secret";
        String signature = sign(file, appId, secret);
        System.out.println(signature);
        System.out.println(check(file, appId, secret, signature));
    }
    */
}
