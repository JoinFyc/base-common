package com.mengxiang.base.common.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 摘要工具类，可以计算MD5、SHA等摘要
 *
 * @author ice
 * @version 1.0
 * @date 2019/4/15 4:12 PM
 */
public final class DigestUtils {

    private DigestUtils() {
    }

    /**
     * 取MD5摘要
     *
     * @param text 正文
     * @return 摘要结果
     */
    public static String md5(String text) {
        return builder().algorithm(Algorithm.MD5).append(text).digest();
    }

    /**
     * 取SHA-1摘要
     *
     * @param text 正文
     * @return 摘要结果
     */
    public static String sha1(String text) {
        return builder().algorithm(Algorithm.SHA1).append(text).digest();
    }

    /**
     * 取SHA-256摘要
     *
     * @param text 正文
     * @return 摘要结果
     */
    public static String sha256(String text) {
        return builder().algorithm(Algorithm.SHA256).append(text).digest();
    }

    /**
     * 取SHA-224摘要
     *
     * @param text 正文
     * @return 摘要结果
     */
    public static String sha224(String text) {
        return builder().algorithm(Algorithm.SHA224).append(text).digest();
    }

    /**
     * 取SHA-512摘要
     *
     * @param text 正文
     * @return 摘要结果
     */
    public static String sha512(String text) {
        return builder().algorithm(Algorithm.SHA512).append(text).digest();
    }

    /**
     * 取SHA-384摘要
     *
     * @param text 正文
     * @return 摘要结果
     */
    public static String sha384(String text) {
        return builder().algorithm(Algorithm.SHA384).append(text).digest();
    }

    /**
     * 用户自定义摘要算法
     *
     * @param alg  摘要算法
     * @param text 正文
     * @return 摘要结果
     * @throws NoSuchAlgorithmException 算法不存在，抛出该异常
     */
    public static String digest(String alg, String text) throws NoSuchAlgorithmException {
        return builder().algorithm(alg).append(text).digest();
    }

    /**
     * 获取摘要建造者对象
     *
     * @return 建造者对象
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * byte[]转换为16进制字符串
     *
     * @param bytes byte数组
     * @return 16进制字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            int temp = (bytes[i] & 0xff);
            String hex = Integer.toHexString(temp);
            sb.append(hex.length() < 2 ? "0" + hex : hex);
        }
        return sb.toString();
    }

    /**
     * 16进制字符串转换为byte数组
     *
     * @param hex 16进制字符串
     * @return byte数组
     */
    public static byte[] hexToBytes(String hex) {
        if ((hex.length() & 1) == 1) {
            throw new IllegalArgumentException("Invalid hex string.");
        }
        int len = hex.length() / 2;
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            String seg = String.valueOf(hex.charAt(i << 1)) + String.valueOf(hex.charAt((i << 1) + 1));
            bytes[i] = (byte) Integer.parseInt(seg, 16);
        }
        return bytes;
    }


    /**
     * 摘要Builder，可以方便的使用建造者模式计算指定文本的摘要
     */
    public static class Builder {

        private MessageDigest messageDigest;

        private StringBuilder sb;

        private String charset = "UTF-8";

        private Builder() {
            sb = new StringBuilder();
        }

        /**
         * 使用指定算法，如果指定算法不存在，则抛出异常
         *
         * @param alg 算法名
         * @return 建造者对象
         * @throws NoSuchAlgorithmException 没有指定算法时抛出异常
         */
        public Builder algorithm(String alg) throws NoSuchAlgorithmException {
            this.messageDigest = MessageDigest.getInstance(alg);
            return this;
        }

        /**
         * 使用指定算法，传入枚举
         * 如果算法不存在，不会抛出异常，但是{@link #digest()}方法返回{@code null}
         *
         * @param alg 算法枚举
         * @return 建造者对象
         */
        public Builder algorithm(Algorithm alg) {
            try {
                this.messageDigest = MessageDigest.getInstance(alg.getAlg());
            } catch (NoSuchAlgorithmException e) {
            }
            return this;
        }

        /**
         * 指定字符集，用于计算摘要时将传入的字符串转为byte[]
         *
         * @param charset 字符集
         * @return 建造者对象
         */
        public Builder charset(String charset) {
            this.charset = charset;
            return this;
        }

        /**
         * 添加文本，可以调用多次进行文本拼接
         *
         * @param text 输入文本
         * @return 建造者对象
         */
        public Builder append(String text) {
            sb.append(text);
            return this;
        }

        /**
         * 根据指定的算法、字符集和正文，计算摘要
         * 如果出现异常则返回{@code null}
         *
         * @return 摘要
         */
        public String digest() {
            if (messageDigest != null) {
                String content = sb.toString();
                byte[] input = null;
                try {
                    input = content.getBytes(charset);
                } catch (UnsupportedEncodingException e) {
                }
                if (input != null) {
                    byte[] bytes = messageDigest.digest(input);
                    return bytesToHex(bytes);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    /**
     * 摘要算法
     */
    public enum Algorithm {

        /**
         * md5
         */
        MD5("md5"),

        /**
         * sha-1
         */
        SHA1("sha-1"),

        /**
         * sha-256
         */
        SHA256("sha-256"),

        /**
         * sha-224
         */
        SHA224("sha-224"),

        /**
         * sha-512
         */
        SHA512("sha-512"),

        /**
         * sha-384
         */
        SHA384("sha-384"),
        ;

        private String alg;

        Algorithm(String alg) {
            this.alg = alg;
        }

        public String getAlg() {
            return alg;
        }
    }
}
