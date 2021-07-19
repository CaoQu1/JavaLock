package com.lock;

import org.openjdk.jol.info.ClassLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SynchronizedExample {


    public static void main(String[] args) throws InterruptedException {

        //synchronized锁的特性：
        //可重入/非公平/互斥锁
        Example example = new Example();//同一个锁对象
        example.run();

        //synchronized锁的升级过程
        Example1 example1 = new Example1();
        example1.run();
        //example1.batchRun();

        //使用场景：
        //多线程竞争环境下对共享资源的修改
        //也可以实现生产消费者模型
        Example2 example2 = new Example2();
        //example2.run();
        //example2.customer();

        //缺点：
        //当线程尝试获取锁的时候，如果获取不到锁会一直阻塞。
        //如果获取锁的线程进入休眠或者阻塞，除非当前线程异常，否则其他线程尝试获取锁必须一直等待。
    }

    public static class Example {

        public void run() {
            for (int i = 0; i < 10; i++) {
                //只有当前线程释放锁后，下一个线程才能获取锁，说明锁是独占锁（当前线程如果未执行完成，其他线程会一直阻塞）
                //获取锁的线程是没有先后顺序的，线程0获取后不一定是线程1获取到锁，说明锁是非公平的，
                //调用get后调用set都用的同一把锁，说明锁是可重入的
                new Thread(() -> get(), String.valueOf(i)).start();
            }
        }

        private synchronized void get() {
            System.out.println("线程-" + Thread.currentThread().getName() + "获得锁");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            set();
        }

        private synchronized void set() {
            System.out.println("线程-" + Thread.currentThread().getName() + "获得重入锁");
        }
    }

    public static class Example1 {

        public void run() throws InterruptedException {

            //由于偏向锁有延迟,关闭延迟-XX:BiasedLockingStartupDelay=0。（由于平时jvm的启动时间肯定超过了这个时间，所以已经是可偏向状态）
            //加锁之前是无锁（但是是可偏向状态）,启动线程加锁之后是偏向锁（偏向的线程id）,锁释放后还是偏向锁,
            //如果另一个线程（主线程）来获取这个锁,不管前面的线程是否释放，当前的锁都是轻量级锁

            //加锁之前是无锁（但是是可偏向状态）,启动两个线程获取的锁为重量级锁,锁释放后是无锁,
            //如果另一个线程（主线程）来获取这个锁,当前的锁是轻量级锁

            Object object = new Object();

            object.hashCode();//计算hashcode之后就成为不可偏向状态，直接变成轻量级或者重量级锁 //这里涉及一个大小端模式

            System.out.println("before lock");
            System.out.println(ClassLayout.parseInstance(object).toPrintable()); //可偏向状态

            startThread(object, 1); //数量设置为1，偏向锁 偏向当前线程 执行完成后还是偏向锁,再次获取锁，直接升级为轻量级锁

            //startThread(object, 2); //数量设置为2，因为有竞争变成重量级锁，执行完成后变成无锁，再次获取锁，因为是不可偏向状态,直接升级为轻量级锁

            System.out.println("thread after lock");
            System.out.println(ClassLayout.parseInstance(object).toPrintable());

            synchronized (object) {
                System.out.println("main lock ing");
                System.out.println(ClassLayout.parseInstance(object).toPrintable()); //轻量级锁
            }

            System.out.println("after lock");
            System.out.println(ClassLayout.parseInstance(object).toPrintable()); //无锁
        }

        private void startThread(Object object, int count) throws InterruptedException {
            for (int i = 0; i < count; i++) {
                Thread t1 = new Thread(() -> {
                    synchronized (object) {
                        System.out.println(Thread.currentThread().getName() + " lock ing");
                        System.out.println(ClassLayout.parseInstance(object).toPrintable()); //
                    }
                    //让当前线程等待
               /* try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                }, "t".concat(String.valueOf(i)));
                t1.start();
                //t1.join();//肯定已经执行完了
            }
            while (Thread.activeCount() > 1)
                Thread.yield(); //线程执行完成
        }

        public void batchRun() throws InterruptedException {

            List<Object> objects = new ArrayList<>();
            for (int i = 0; i < 60; i++) {
                Object object = new Object();
                synchronized (object) {
                    objects.add(object);
                }
            }

            System.out.println("随机取一个锁状态");
            System.out.println(ClassLayout.parseInstance(objects.get(20)).toPrintable());

            Thread t1 = new Thread(() -> {
                for (int i = 0; i < 60; i++) {
                    synchronized (objects.get(i)) {
                        if (i == 18) {
                            System.out.println("取19的锁状态");
                            System.out.println(ClassLayout.parseInstance(objects.get(i)).toPrintable());//轻量级锁
                        }

                        if (i == 19) {
                            System.out.println("取20的锁状态");
                            System.out.println(ClassLayout.parseInstance(objects.get(i)).toPrintable());//偏向锁 批量重偏向
                        }
                    }
                }
            });
            t1.start();
            t1.join();

            Thread t2 = new Thread(() -> {
                for (int i = 0; i < 60; i++) {
                    synchronized (objects.get(i)) {
                        if (i == 38) {
                            System.out.println("取39的锁状态");
                            System.out.println(ClassLayout.parseInstance(objects.get(i)).toPrintable());//轻量级锁
                        }

                        if (i == 39) {
                            System.out.println("取40的锁状态");
                            System.out.println(ClassLayout.parseInstance(objects.get(i)).toPrintable());//偏向锁 批量重偏向
                        }
                    }
                }
            });
            t2.start();

        }
    }

    public static class Example2 {

        static int sum = 0;

        static Object object = new Object();

        public void run() {

            Thread t1 = new Thread(() -> {
                synchronized (object) {
                    for (int i = 0; i < 1000; i++) {
                        sum++;
                    }
                }
            });

            Thread t2 = new Thread(() -> {
                synchronized (object) {
                    for (int i = 0; i < 1000; i++) {
                        sum++;
                    }
                }
            });

            t1.start();
            t2.start();

            while (Thread.activeCount() > 1)
                Thread.yield();

            System.out.println("预期值：" + 2000);
            System.out.println("实际值：" + sum);
        }

        public void customer() throws InterruptedException {

            List<Integer> list = new ArrayList<>();

            Thread t1 = new Thread(() -> {
                synchronized (object) {
                    try {
                        System.out.println("t1获取到锁");
                        while (true) {
                            while (!list.isEmpty()) {
                                object.wait();
                            }
                            int product = new Random().nextInt();
                            System.out.println("生产" + product);
                            list.add(product);
                            Thread.sleep(1000);
                            object.notify();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });


            Thread t2 = new Thread(() -> {
                synchronized (object) {
                    try {
                        System.out.println("t2获取到锁");
                        while (true) {
                            while (list.isEmpty()) {
                                object.wait();
                                System.out.println("等待消费数据");
                            }
                            System.out.println("消费" + list.remove(0));
                            object.notify();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            t2.start();
            Thread.sleep(2000);
            t1.start();
            Thread.sleep(10000000);
        }
    }
}
