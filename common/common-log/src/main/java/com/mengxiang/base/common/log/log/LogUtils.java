package com.mengxiang.base.common.log.log;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * 日志格式工具类
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/7/3 8:54 PM
 */
public final class LogUtils {

    private LogUtils() {
    }

    /**
     * 格式化日志信息
     *
     * @param pattern 日志格式模版
     * @param params  参数列表
     * @return 格式化日志
     */
    public static String formatMessage(String pattern, Object... params) {
        if (params == null) {
            return pattern;
        } else {
            FormattingTuple tuple = MessageFormatter.arrayFormat(pattern, params);
            String message = tuple.getMessage();
            if (tuple.getThrowable() != null) {
                message = message + "\n" + getErrorMessage(tuple.getThrowable());
            }
            return message;
        }
    }

    /**
     * 打印的最大堆栈深度，超过部分会被省略
     */
    private static final Integer MAX_STACK_DEPTH = 200;

    /**
     * 输出异常信息和堆栈
     *
     * @param t 异常
     * @return 异常堆栈信息
     */
    public static String getErrorMessage(Throwable t) {
        StringBuilder appender = new StringBuilder(t.toString());
        StackTraceElement[] stack = t.getStackTrace();
        if (stack != null && stack.length > 0) {
            for (int i = 0; i < stack.length; i++) {
                if (i < MAX_STACK_DEPTH) {
                    StackTraceElement e = stack[i];
                    appender.append("\n\t").append(e.toString());
                } else {
                    appender.append("\n\t……");
                    break;
                }
            }
        }
        return appender.toString();
    }

    /**
     * 获取调用堆栈中的调用类名方法名等信息
     *
     * @return 堆栈元素
     */
    public static StackTraceElement getStackTrace(String fqcn) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        StackTraceElement last = null;
        for (StackTraceElement e : elements) {
            if (e.getClassName().equals(fqcn)) {
                last = e;
            } else if (last != null) {
                last = e;
                break;
            }
        }
        return last;
    }
}
