package com.telsoft.cbs.designer.utils;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

import javax.swing.*;

public class MyAppender extends AppenderSkeleton {
    private JTextArea txt;

    public MyAppender(JTextArea txt) {
        this.txt = txt;
        setLayout(new PatternLayout("%p - %d{HH:mm:ss,SSS} - [%.30t] %.20c{1}: %m%n"));
    }

    protected void append(LoggingEvent loggingEvent) {
        txt.append(getLayout().format(loggingEvent));
    }

    public void close() {
    }

    public boolean requiresLayout() {
        return false;
    }
}
