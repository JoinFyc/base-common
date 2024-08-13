package com.mengxiang.base.datatask.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StackTraceUtil {

    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            t.printStackTrace(pw);
            return sw.toString();
        } finally {
            pw.close();
        }
    }

    public static String getStackTrace(Throwable t,int len) {
        String msg = getStackTrace(t);
        return null != msg && msg.length() > len ? msg.substring(0,len) : msg;
    }

    public static void main(String[] args) {

        try {
            m1();
        } catch (Exception e) {
            System.out.println(StackTraceUtil.getStackTrace(e,300));
        }

    }

    public static void m1() {
        m2();
    }

    public static void m2() {
        m3();
    }

    public static void m3() {
        m4();
    }

    public static void m4() {
        m5();
    }

    public static void m5() {
        m6();
    }

    public static void m6() {
        throw new RuntimeException("error");
    }
}
