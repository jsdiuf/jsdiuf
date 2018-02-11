package com.jsdiuf.jsdiuf.thread;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author weicc
 * @create 2018-01-23 11:07
 **/
public class MyService {

    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void method() {

        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
