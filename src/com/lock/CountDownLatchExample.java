package com.lock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CountDownLatchExample {

    public static void main(String[] args) throws InterruptedException {

        //使用场景：
        //有一个任务想要往下执行，但必须要等到其他的任务执行完毕后才可以继续往下执行
        CountDownLatch countDownLatch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    System.out.println("线程" + Thread.currentThread().getName() + "正在执行");
                    Thread.sleep(3000);
                    countDownLatch.countDown();
                    System.out.println("线程" + Thread.currentThread().getName() + "执行完成");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "t" + i).start();
        }

        //countDownLatch.await();
        countDownLatch.await(1, TimeUnit.SECONDS);
        //System.out.println("所有线程执行完成");
        System.out.println("主线程开始执行");
    }
}
