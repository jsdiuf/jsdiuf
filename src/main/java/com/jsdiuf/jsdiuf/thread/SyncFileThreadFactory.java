package com.jsdiuf.jsdiuf.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class SyncFileThreadFactory implements ThreadFactory {

    private final transient Logger LOG = LoggerFactory.getLogger(SyncFileThreadFactory.class);

    private final AtomicInteger count = new AtomicInteger(0);
    private String type;

    public SyncFileThreadFactory(final String type) {
        this.type = type;
    }

    @Override
    public Thread newThread(Runnable r) {

        final String threadName = type + "_" + count.getAndIncrement();
        final Thread thread = new Thread(r, threadName);
        //设置线程为非守护线程
        thread.setDaemon(false);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOG.error(e.getMessage(), e);
            }
        });
        return thread;
    }
}