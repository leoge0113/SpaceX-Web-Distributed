package com.cainiao.spacex.distributed.locks;

public interface ExecuteCallBack {
    public Object onGetLock() throws InterruptedException;

    public Object onTimeout() throws InterruptedException;
}
