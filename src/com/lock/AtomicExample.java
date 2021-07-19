package com.lock;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicExample {

    static AtomicInteger sum = new AtomicInteger();

    public static void main(String[] args) {

        //非阻塞同步 这个就解决了 前面volatile进行自增的不能保证原子操作 原理(cas操作)我们可以参见源码

        for (int i = 0; i < 20; i++) {
            new Thread(() -> {
                for (int j = 0; j < 10000; j++) {
                    sum.incrementAndGet();
                }
            }).start();
        }

        while (Thread.activeCount() > 1)
            Thread.yield();

        System.out.println("sum预期值等于:" + 200000);
        System.out.println("sum实际值等于:" + sum);
    }
}
