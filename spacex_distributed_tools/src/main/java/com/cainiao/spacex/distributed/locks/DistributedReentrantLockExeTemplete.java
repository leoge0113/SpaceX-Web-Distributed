package com.cainiao.spacex.distributed.locks;

//
public interface DistributedReentrantLockExeTemplete {
    public Object execute(String lockId, int timeout, ExecuteCallBack callBack);

}
