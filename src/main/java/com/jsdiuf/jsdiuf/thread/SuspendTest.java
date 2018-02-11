package com.jsdiuf.jsdiuf.thread;

/**
 * @author weicc
 * @create 2018-01-19 15:55
 **/
public class SuspendTest {
    synchronized public void suspend(){
        System.out.println("begin");
        if(Thread.currentThread().getName().equals("a")){
           System.out.println("thread a into suspend");
           Thread.currentThread().suspend();
        }
        System.out.println("end");
    }
}
