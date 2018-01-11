package com.cainiao.spacex.distributed.locks;

import java.util.concurrent.TimeUnit;

public interface DistributedReentrantLock {
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException;
    public void unLock();
}
