package com.jsdiuf.jsdiuf.thread;


/**
 * @author weicc
 */
public class Thread1 implements Runnable {

    Object lock;

    public Thread1(Object lock) {
        this.lock = lock;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
