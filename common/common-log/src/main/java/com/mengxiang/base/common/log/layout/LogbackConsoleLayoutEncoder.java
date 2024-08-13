package com.mengxiang.base.common.log.layout;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.mengxiang.base.common.log.log.BaseLog;
import org.apache.skywalking.apm.toolkit.log.logback.v1.x.TraceIdPatternLogbackLayout;

/**
 * 自定义Encoder，替换LoggerName
 *
 * @author ice
 * @date 2019/8/12 9:13 PM
 */
public class LogbackConsoleLayoutEncoder extends PatternLayoutEncoder {

    @Override
    public void start() {
        TraceIdPatternLogbackLayout traceIdPatternLogbackLayout = new TraceIdPatternLogbackLayout();
        traceIdPatternLogbackLayout.setContext(context);
        traceIdPatternLogbackLayout.setPattern(getPattern());
        traceIdPatternLogbackLayout.setOutputPatternAsHeader(outputPatternAsHeader);
        traceIdPatternLogbackLayout.start();
        this.layout = traceIdPatternLogbackLayout;
        super.start();
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        LoggingEvent loggingEvent = (LoggingEvent) event;
        BaseLog log = (BaseLog) loggingEvent.getArgumentArray()[0];
        String cls = log.getSource().getCls();
        loggingEvent.setLoggerName(cls);
        return super.encode(loggingEvent);
    }
}
