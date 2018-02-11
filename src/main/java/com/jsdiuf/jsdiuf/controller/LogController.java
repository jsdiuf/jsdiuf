package com.jsdiuf.jsdiuf.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author
 */
@RestController
@RequestMapping("log")
public class LogController {

    private final Logger LOG = LoggerFactory.getLogger(LogController.class);

    @RequestMapping(value="writelog",method = RequestMethod.GET)
    public Object writeLog()
    {
        LOG.trace("This is a trace message");
        LOG.debug("This is a debug message");
        LOG.info("This is an info message");
        LOG.warn("This is a warn message");
        LOG.error("This is an error message");
        new LogHelper().helpMethod();
        return "OK";
    }
}
