package com.mengxiang.base.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XssUtil {
    private XssUtil() {

    }

    private static Pattern[] patterns = new Pattern[]{
            // Script fragments
            Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
            // src='...'
            Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // lonely script tags
            Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // eval(...)
            Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // expression(...)
            Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // javascript:...
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            // vbscript:...
            Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
            // 空格英文单双引号
//            Pattern.compile("[\\s\'\"]+", Pattern.CASE_INSENSITIVE),
            // onload(...)=...
            Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // alert
            Pattern.compile("alert(.*?)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("<", Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile(">", Pattern.MULTILINE | Pattern.DOTALL),
            //Checks any html tags i.e. <script, <embed, <object etc.
            Pattern.compile("(<(script|iframe|embed|frame|frameset|object|img|applet|body|html|style|layer|link|ilayer|meta|bgsound))")
    };

    /**
     * xss替换函数
     *
     * @param value 需要替换的字符
     * @return 替换后的字符
     */
    public static String stripXSS(String value) {
        if (value != null) {
            value = value.replaceAll("\0", "");
            for (Pattern scriptPattern : patterns) {
                value = scriptPattern.matcher(value).replaceAll("");
            }
        }
        return value;
    }

    /**
     * xss校验函数
     *
     * @param value 需要校验的字符
     * @return 返回值：true 表示存在xss漏洞，false：不存在
     */
    public static boolean checkIsXSS(String value) {
        boolean isXss = false;
        if (value != null) {
            for (Pattern scriptPattern : patterns) {
                Matcher matcher = scriptPattern.matcher(value);
                if (matcher.find()) {
                    isXss = true;
                    break;
                }
            }
        }
        return isXss;
    }
}
