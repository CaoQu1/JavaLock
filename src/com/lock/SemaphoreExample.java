package com.lock;

import java.util.concurrent.Semaphore;

public class SemaphoreExample {
    public static void main(String[] args) {

        //使用场景：
        //同一时间能访问共享资源的线程数量
        Semaphore semaphore = new Semaphore(3);
        for (int i = 0; i < 15; i++) {
            new Thread(() -> {
                try {
                    semaphore.acquire();
                    System.out.println("线程" + Thread.currentThread().getName() + "正在执行");
                    Thread.sleep(2000);
                    semaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "t" + i).start();
        }
    }
}
