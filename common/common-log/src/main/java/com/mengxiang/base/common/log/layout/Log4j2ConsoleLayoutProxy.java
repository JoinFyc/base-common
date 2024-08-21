package com.mengxiang.base.common.log.layout;

import com.mengxiang.base.common.log.log.BaseLog;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 控制台Layout的代理类，替换LoggerName
 *
 * @author JoinFyc
 * @date 2019/8/12 9:12 PM
 */
public class Log4j2ConsoleLayoutProxy implements InvocationHandler {

    private static final String PROXY_METHOD_NAME = "encode";

    private Layout patternLayout;

    private Object proxy;

    public Object getProxy() {
        return proxy;
    }

    public Log4j2ConsoleLayoutProxy(Layout patternLayout) {
        this.patternLayout = patternLayout;
        proxy = Proxy.newProxyInstance(patternLayout.getClass().getClassLoader(),
                new Class[]{Layout.class}, this);
    }

    @Override
    public Object invoke(Object object, Method method, Object[] args) throws Throwable {
        // 只代理encode方法
        if (PROXY_METHOD_NAME.equals(method.getName())) {
            LogEvent event = (LogEvent) args[0];

            Log4jLogEvent.Builder log4jLogEvent = new Log4jLogEvent.Builder(event);
            BaseLog log = (BaseLog) event.getMessage().getParameters()[0];
            log4jLogEvent.setLoggerName(log.getSource().getCls());

            args[0] = log4jLogEvent.build();
        }
        return method.invoke(patternLayout, args);
    }
}
