package com.cainiao.spacex.distributed.locks;

import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DistributedLock implements DistributedReentrantLock {
    private static final Logger LOG = LoggerFactory.getLogger(DistributedLock.class);
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);
    private static final String ROOT_PATH = "/ROOT_LOCK";
    private String path;
    private static long delayCleanTime = 1000;
    //Curator 共享锁实现
    private InterProcessMutex interProcessMutex;
    private CuratorFramework client;

    public DistributedLock(CuratorFramework client, String lockId) {
        init(client, lockId);
    }

    private void init(CuratorFramework client, String lockId) {
        this.client = client;
        this.path = ROOT_PATH + lockId;
        this.interProcessMutex = new InterProcessMutex(client, path);
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        try {
            return interProcessMutex.acquire(timeout, unit);
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void unLock() {
        try {
            interProcessMutex.release();
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
        } finally {
            executorService.schedule(new Cleaner(client, path), delayCleanTime, TimeUnit.MILLISECONDS);
        }

    }

    static class Cleaner implements Runnable {
        CuratorFramework client;
        String path;

        public Cleaner(CuratorFramework client, String path) {
            this.client = client;
            this.path = path;
        }

        public void run() {
            try {
                List list = client.getChildren().forPath(path);
                if (list == null || list.isEmpty()) {
                    client.delete().forPath(path);
                }
            } catch (KeeperException.NoNodeException e1) {
                //nothing
            } catch (KeeperException.NotEmptyException e2) {
                //nothing
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);//准备删除时,正好有线程创建锁
            }
        }
    }
}
