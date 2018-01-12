# SpaceX-Web-Distributed
web分布式开发框架；实现分布式锁，队列等；SpaceX-Web的分布式版本。

# zookeeper锁实现
## 可重入锁
```
private static class LockData
    {
        final Thread owningThread;
        final String lockPath;
        final AtomicInteger lockCount = new AtomicInteger(1);

        private LockData(Thread owningThread, String lockPath)
        {
            this.owningThread = owningThread;
            this.lockPath = lockPath;
        }
    }
```
其中lockCount记录获取到锁的线程进入次数。
## 实现的是悲观锁，会阻塞
函数 internalLockLoop，如下，当发现自己不是第一个node时，调用Object.wait()。
watcher实现里有notify，接到现象后继续判断自己是不是得到锁。

```
 private boolean internalLockLoop(long startMillis, Long millisToWait, String ourPath) throws Exception
    {
        boolean     haveTheLock = false;
        boolean     doDelete = false;
        try
        {
            if ( revocable.get() != null )
            {
                client.getData().usingWatcher(revocableWatcher).forPath(ourPath);
            }

            while ( (client.getState() == CuratorFrameworkState.STARTED) && !haveTheLock )
            {
                List<String>        children = getSortedChildren();
                String              sequenceNodeName = ourPath.substring(basePath.length() + 1); // +1 to include the slash

                PredicateResults    predicateResults = driver.getsTheLock(client, children, sequenceNodeName, maxLeases);
                if ( predicateResults.getsTheLock() )
                {
                    haveTheLock = true;
                }
                else
                {
                    String  previousSequencePath = basePath + "/" + predicateResults.getPathToWatch();

                    synchronized(this)
                    {
                        try 
                        {
                            // use getData() instead of exists() to avoid leaving unneeded watchers which is a type of resource leak
                           
                           //watch里有nodify client.getData().usingWatcher(watcher).forPath(previousSequencePath);
                            if ( millisToWait != null )
                            {
                                millisToWait -= (System.currentTimeMillis() - startMillis);
                                startMillis = System.currentTimeMillis();
                                if ( millisToWait <= 0 )
                                {
                                    doDelete = true;    // timed out - delete our node
                                    break;
                                }

                                wait(millisToWait);
                            }
                            else
                            {
                                wait();
                            }
                        }
                        catch ( KeeperException.NoNodeException e ) 
                        {
                            // it has been deleted (i.e. lock released). Try to acquire again
                        }
                    }
                }
            }
        }
        catch ( Exception e )
        {
            doDelete = true;
            throw e;
        }
        finally
        {
            if ( doDelete )
            {
                deleteOurPath(ourPath);
            }
        }
        return haveTheLock;
    }
    
    private final Watcher watcher = new Watcher()
    {
        @Override
        public void process(WatchedEvent event)
        {
            notifyFromWatcher();
        }
    };
```
## curator与zookeeper版本
curator 4.0.0，zookeeper 3.5X 或者其他二者配套版本。
版本不匹配会出现诸如以下错误。

### xid out of order
zookeeper 与curator版本不匹配（curator2.9.1,zookeeper3.3.6时报错）
```
java.io.IOException: Xid out of order.
```
### doesn't support Container nodes
```
[org.apache.curator.utils.ZKPaths] - The version of ZooKeeper being used doesn't support Container nodes. CreateMode.PERSISTENT will be used instead.
```


