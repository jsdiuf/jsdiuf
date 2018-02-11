package com.jsdiuf.jsdiuf.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author weicc
 */
public class LogHelper {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    public void helpMethod(){
        LOG.trace("This is a trace message");
        LOG.debug("This is a debug message");
        LOG.info("This is an info message");
        LOG.warn("This is a warn message");
        LOG.error("This is an error message");
    }
}
