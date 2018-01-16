package com.cainiao.spacex.distributed.locks.impl;

import com.cainiao.spacex.distributed.locks.DistributedReentrantLockExeTemplete;
import com.cainiao.spacex.distributed.locks.ExecuteCallBack;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;

import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
//内部类实现单例 clinet
public class DistributedReentrantLockExeTempImpl implements DistributedReentrantLockExeTemplete {
    private static final Logger LOG = LoggerFactory.getLogger(DistributedReentrantLockExeTempImpl.class);
    private static CuratorFramework client ;
    static {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient("192.168.253.131:2181", retryPolicy);
        client.start();
    }
    public DistributedReentrantLockExeTempImpl(){};

    @Override
    public Object execute(String lockId, int timeout, ExecuteCallBack callBack) {
        DistributedLockImpl distributedLock = new DistributedLockImpl(client, lockId);
        boolean getLock = false;
        try {
            if (distributedLock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
                getLock = true;
                return callBack.onGetLock();
            } else {
                return callBack.onTimeout();
            }
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
            // e.printStackTrace();
            Thread.currentThread().interrupt();//resume 中断
        }catch (Exception e){
            LOG.error(e.getMessage(),e);//如果不捕获处理trylock的异常，当异常发生时，execute不会退出，线程进入waiting状态
        }
        finally {
            if (getLock) {
                distributedLock.unLock();
            }
        }
        return null;
    }
}
