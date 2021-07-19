package com.lock;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantLockExample {

    public static void main(String[] args) throws InterruptedException {

        //ReentrantLock锁的特性：
        //可重入/（非）/公平/可响应锁
        Example example = new Example();
        //example.run();

        //原理AQS参见源码

        //使用场景：
        //1.某个线程在等待一个锁的控制权的这段时间需要中断
        //2.需要分开处理一些wait-notify，ReentrantLock里面的Condition，能够控制notify哪个线程
        //3.具有公平锁功能，每个到来的线程都将排队等候
        Example1 example1 = new Example1();
        example1.run();
        //example1.conditionRun();

        ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
        reentrantReadWriteLock.readLock();
    }

    public static class Example {

        ReentrantLock reentrantLock = new ReentrantLock();
        //ReentrantLock reentrantLock = new ReentrantLock(true);

        public void run() {
            Test test = new Test();
            for (int i = 0; i < 10; i++) {
                //只有当前线程释放锁后，下一个线程才能获取锁，说明锁是独占锁（当前线程如果未执行完成，其他线程会一直阻塞）
                //调用get后调用set都用的同一把锁，说明锁是可重入的
                //默认ReentrantLock构造的是非公平锁：获取锁的线程是没有先后顺序的，如果在申请锁的时候刚好有锁可用,直接跳过等待队列拿到锁。
                //初始化ReentrantLock时候构造函数传入true时就是公平锁：获取锁的线程是有先后顺序的，排在队列最前面的最先获取到锁。
                new Thread(test, String.valueOf(i)).start();
            }
        }

        public class Test implements Runnable {

            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("线程-" + Thread.currentThread().getName() + "开始运行");
                get();
            }

            private void get() {
                reentrantLock.lock();
                System.out.println("线程-" + Thread.currentThread().getName() + "获得锁");
                try {
                    Thread.sleep(1000);
                    set();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    reentrantLock.unlock();
                }
            }

            private void set() {
                reentrantLock.lock();
                try {
                    System.out.println("线程-" + Thread.currentThread().getName() + "获得重入锁");
                } finally {
                    reentrantLock.unlock();
                }
            }
        }
    }

    public static class Example1 {
        private ReentrantLock reentrantLock = new ReentrantLock();

        public void run() throws InterruptedException {

            //可中断 1、lockInterruptibly
          /*  Thread t1 = new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    System.out.println("线程t1开始执行");
                    reentrantLock.lockInterruptibly();
                    System.out.println("线程t1获取到reentrantLock锁" + reentrantLock.isLocked());
                    Thread.sleep(100000);
                } catch (InterruptedException e) {
                    System.out.println("线程t1被中断");
                } finally {
                    reentrantLock.unlock();
                    System.out.println("锁已经释放");
                }
            });
            t1.start();
            Thread.sleep(2000);
            t1.interrupt();
            Thread.sleep(100000);*/

            //2、tryLock
            Thread t2 = new Thread(() -> {
                try {
                    System.out.println("线程t2开始执行");
                    if (reentrantLock.tryLock(1, TimeUnit.SECONDS)) {
                        System.out.println("线程t2获取到锁");
                        Thread.sleep(1500000);
                    } else {
                        System.out.println("线程t2未获取到锁");
                    }
                } catch (InterruptedException ex) {
                    System.out.println("线程t2被中断");
                } finally {
                    if (reentrantLock.isHeldByCurrentThread()) {
                        reentrantLock.unlock();
                        System.out.println("线程t2释放锁");
                    }
                }
            }, "t2");
            Thread t3 = new Thread(() -> {
                try {
                    System.out.println("线程t3开始执行");
                    if (reentrantLock.tryLock(1, TimeUnit.SECONDS)) {
                        System.out.println("线程t3获取到锁");
                        Thread.sleep(15000000);
                    } else {
                        System.out.println("线程t3未获取到锁");
                    }
                } catch (InterruptedException ex) {
                    System.out.println("线程t3被中断");
                } finally {
                    if (reentrantLock.isHeldByCurrentThread()) {
                        reentrantLock.unlock();
                        System.out.println("线程t3释放锁");
                    }
                }
            }, "t3");
            t2.start();
            Thread.sleep(2000);
            t3.start();
            Thread.sleep(100000);
        }

        public void conditionRun() throws InterruptedException {

            Condition condition = reentrantLock.newCondition();
            Condition condition1 = reentrantLock.newCondition();
            Thread t1 = new Thread(() -> {
                reentrantLock.lock();
                try {
                    System.out.println("线程" + Thread.currentThread().getName() + "获取到锁");
                    System.out.println("线程" + Thread.currentThread().getName() + "被阻塞");
                    condition.await();
                    System.out.println("线程" + Thread.currentThread().getName() + "继续执行");
                } catch (InterruptedException e) {
                    System.out.println("线程" + Thread.currentThread().getName() + "被中断");
                } finally {
                    reentrantLock.unlock();
                    System.out.println("线程" + Thread.currentThread().getName() + "释放锁");
                }

            }, "t1");

            Thread t2 = new Thread(() -> {
                reentrantLock.lock();
                try {
                    System.out.println("线程" + Thread.currentThread().getName() + "获取到锁");
                    System.out.println("线程" + Thread.currentThread().getName() + "被阻塞");
                    condition1.await();
                    System.out.println("线程" + Thread.currentThread().getName() + "继续执行");
                } catch (InterruptedException e) {
                    System.out.println("线程" + Thread.currentThread().getName() + "被中断");
                } finally {
                    reentrantLock.unlock();
                    System.out.println("线程" + Thread.currentThread().getName() + "释放锁");
                }

            }, "t2");

            t1.start();
            t2.start();

            Thread.sleep(1000);
            condition.signal();//唤醒指定线程，condition1阻塞的是线程t1,所以也是唤醒t1

            Thread.sleep(1000);
            condition1.signal();//condition2阻塞的是线程t2,所以也是唤醒t2

            //总结：object wait/notify 随机唤醒，condition await/signal唤醒指定阻塞的线程
        }
    }
}
