package com.jsdiuf.jsdiuf.thread;

import com.jsdiuf.jsdiuf.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author weicc
 */
public class ThreadTest {

    private static final transient Logger LOG = LoggerFactory.getLogger(ThreadTest.class);

    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public class Thread1 implements Runnable {
        String name;
        CountDownLatch latch;

        public Thread1(CountDownLatch latch, String name) {
            this.latch = latch;
            this.name = name;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " " + name + " start time is " + TimeUtils.getNowTimeMillions());

            try {
                Thread.sleep(650);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            latch.countDown();

        }
    }


    public static void main(String[] args) {
        int TaskNun = 1000;
        CountDownLatch latch = new CountDownLatch(TaskNun);
        ThreadPoolExecutor excu = new ThreadPoolExecutor(2, 5,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(12),
                new SyncFileThreadFactory("test"));


        for (int i = 0; i < TaskNun; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            excu.submit(new ThreadTest().new Thread1(latch, "name:" + i));
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        excu.shutdown();
        System.out.println("执行结束");




        /*ExecutorService exec = DynamicThreadPoolExecutor.of("test").keepAliveIn(30L, TimeUnit.SECONDS)
                .withCorePoolSize(10).withMaxPoolSize(20).build();

        for (int i = 0; i < TaskNun; i++) {
            exec.submit(new ThreadTest().new Thread1(latch,"name:"+i));
        }

        try {
            latch.await();
        } catch (Exception e) {
            LOG.error("latch.await() error" + e.getMessage(), e);
        }

        //此时所有线程已结束
        exec.shutdown();*/

        /**
         *
         */
       /* ExecutorService cachedThreadPool=Executors.newCachedThreadPool();
        ExecutorService singleThreadPool=Executors.newSingleThreadExecutor();
        ExecutorService fixedThreadPool=Executors.newFixedThreadPool(20);
        ExecutorService scheduledThreadPool=Executors.newScheduledThreadPool(10);

        latch=new CountDownLatch(TaskNun);
        for (int i = 0; i < TaskNun; i++) {
            fixedThreadPool.submit(new ThreadTest().new Thread1(latch,"name:"+i));
        }

        try {
            latch.await();
            fixedThreadPool.shutdown();
            System.out.println("cachedThreadPool end");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        Timer timer2=new Timer(true);
        timer2.scheduleAtFixedRate(null,100,100);
        timer2.schedule(null,100,100);
    }
}
