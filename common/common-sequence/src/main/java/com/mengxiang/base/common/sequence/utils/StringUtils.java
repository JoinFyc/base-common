package com.mengxiang.base.common.sequence.utils;

/**
 * @author JoinFyc
 * @date 2020/12/14 9:26
 */
public class StringUtils {
    /**
     * 扩展并右对齐字符串，用指定字符串填充左边。
     * <pre>
     * StringUtil.alignRight(null, *, *)      = null
     * StringUtil.alignRight("", 3, "z")      = "zzz"
     * StringUtil.alignRight("bat", 3, "yz")  = "bat"
     * StringUtil.alignRight("bat", 5, "yz")  = "yzbat"
     * StringUtil.alignRight("bat", 8, "yz")  = "yzyzybat"
     * StringUtil.alignRight("bat", 1, "yz")  = "bat"
     * StringUtil.alignRight("bat", -1, "yz") = "bat"
     * StringUtil.alignRight("bat", 5, null)  = "  bat"
     * StringUtil.alignRight("bat", 5, "")    = "  bat"
     * </pre>
     *
     * @param str    要对齐的字符串
     * @param size   扩展字符串到指定宽度
     * @param padStr 填充字符串
     * @return 扩展后的字符串，如果字符串为<code>null</code>，则返回<code>null</code>
     */
    public static String alignRight(String str, int size, String padStr) {
        if (str == null) {
            return null;
        }

        if ((padStr == null) || (padStr.length() == 0)) {
            padStr = " ";
        }

        int padLen = padStr.length();
        int strLen = str.length();
        int pads = size - strLen;

        if (pads <= 0) {
            return str;
        }

        if (pads == padLen) {
            return padStr.concat(str);
        } else if (pads < padLen) {
            return padStr.substring(0, pads).concat(str);
        } else {
            char[] padding = new char[pads];
            char[] padChars = padStr.toCharArray();

            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }

            return new String(padding).concat(str);
        }
    }
}
