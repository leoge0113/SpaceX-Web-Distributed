import com.cainiao.spacex.distributed.locks.ExecuteCallBack;
import com.cainiao.spacex.distributed.locks.impl.DistributedReentrantLockExeTempImpl;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public class DistributedLockTest {
    @Test
    public void testTry() throws InterruptedException {
//        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
//        CuratorFramework client = CuratorFrameworkFactory.newClient("192.168.253.131:2181", retryPolicy);
//        client.start();
        final DistributedReentrantLockExeTempImpl template = new DistributedReentrantLockExeTempImpl();
        int size = 2;
        final CountDownLatch startCountDownLatch = new CountDownLatch(1);
        final CountDownLatch endDownLatch = new CountDownLatch(size);
        for (int i = 0; i < size; i++) {
            new Thread() {
                public void run() {
                    try {
                        startCountDownLatch.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    final int sleepTime = ThreadLocalRandom.current().nextInt(5) * 1000;
                    template.execute("test1233", 5000, new ExecuteCallBack() {
                        public Object onGetLock() throws InterruptedException {
                            System.out.println(Thread.currentThread().getName() + ":getLock");
                            Thread.currentThread().sleep(sleepTime);
                            System.out.println(Thread.currentThread().getName() + ":sleeped:" + sleepTime);
                            endDownLatch.countDown();
                            return null;
                        }

                        public Object onTimeout() throws InterruptedException {
                            System.out.println(Thread.currentThread().getName() + ":timeout");
                            endDownLatch.countDown();
                            return null;
                        }
                    });
                }
            }.start();
        }
        startCountDownLatch.countDown();
        endDownLatch.await();
    }
/*
    public static void main(String[] args) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", retryPolicy);
        client.start();

        final DistributedReentrantLockExeTempImpl template = new DistributedReentrantLockExeTempImpl(client);//本类多线程安全,可通过spring注入
        template.execute("订单流水号", 5000, new ExecuteCallBack() {
            @Override
            public Object onGetLock() throws InterruptedException {
                return null;
            }

            @Override
            public Object onTimeout() throws InterruptedException {
                return null;
            }
        });
    }
    */
}
