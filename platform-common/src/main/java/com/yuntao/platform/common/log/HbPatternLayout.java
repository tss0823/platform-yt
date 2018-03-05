package com.yuntao.platform.common.log;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Created by shengshan.tang on 9/14/2015 at 8:41 PM
 */
public class HbPatternLayout extends PatternLayout {

    @Override
    public String doLayout(ILoggingEvent event) {
        return event.getMessage();
    }
}
