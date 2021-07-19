package com.lock;

public class VolatileExample {

    static volatile int sum = 0;
    static volatile boolean flag = true;

    public static void main(String[] args) throws InterruptedException {

        //volatile变量的特性：
        // 1、保证了可见性(这里有个误区，很多人以为变量加了volatile关键字就能保证结果是正确的，举个例子：),但是不保证原子性
        // 2、防止指令重排序导致的bug（还解决了一个单例模式双重检查加锁的问题：当new对象时，发生指令重排序，建立连接比对象初始化先发生，下一个线程进入后发现单例对象不为空返回）
      /*  for (int i = 0; i < 20; i++) {
            new Thread(() -> {
                for (int j = 0; j < 10000; j++) {
                    sum++;//这里并不是原子操作 ，翻译成字节码指令并不是一个操作
                }
            }).start();
        }

        while (Thread.activeCount() > 1)
            Thread.yield();

        System.out.println("sum预期值等于:" + 200000);
        System.out.println("sum实际值等于:" + sum);
*/
        //使用场景：
        //1、运算结果并不依赖变量的当前值，或者能够确保只有单一的线程修改变量的值。
        //2、变量不需要与其他的状态变量共同参与不变约束。
        Thread t1 = new Thread(() -> {
            while (flag) {
                System.out.println(Thread.currentThread().getName() + "执行中");
            }
            System.out.println(Thread.currentThread().getName() + "循环退出");
        }, "线程t1");
        t1.start();

        Thread t2 = new Thread(() -> {
            while (flag) {
                System.out.println(Thread.currentThread().getName() + "执行中");
            }
            System.out.println(Thread.currentThread().getName() + "循环退出");
        }, "线程t2");
        t2.start();
        Thread.sleep(5000);
        flag = false;//只有主线程对标识进行修改
    }
}
