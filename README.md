# SpaceX-Web-Distributed
web分布式开发框架；实现分布式锁，队列等；SpaceX-Web的分布式版本。

# zookeeper锁实现（curator实现）
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
函数 internalLockLoop，当发现自己不是第一个node时，调用Object.wait()。
watcher实现里有notify，接到现象后继续判断自己是不是得到锁。
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
# curator实现分布式锁问题
在 spring framework 定时任务下回报错：
```
2018-01-17 14:23:00,001 ERROR[org.springframework.scheduling.support.TaskUtils$L
oggingErrorHandler:95]- Unexpected error occurred in scheduled task.
java.lang.NoSuchMethodError: 
org.apache.curator.utils.PathUtils.validatePath(Ljava/lang/String;)Ljava/lang/String;
```
很奇怪在junit里不报错。还不知道why。

curator代码确实是有问题，如下：
```
InterProcessMutex(CuratorFramework client, String path, String lockName, int maxLeases, LockInternalsDriver driver)
    {
        basePath = PathUtils.validatePath(path);
        internals = new LockInternals(client, driver, path, lockName, maxLeases);
    }
    public static void validatePath(String path) throws IllegalArgumentException;
```
# Idea 调试jar运行的进程
## jar运行
```
java -Xdebug -Xrunjdwp:transport=dt_socket,address=5005,server=y,suspend=y -jar spacex_service_user.1.0.0.jar
```
## 运行结果
```
Listening for transport dt_socket at address: 5005
```
## 配置IDEA
run/debug configuration->remote->

transport:socket

debugger mode:attach

host: localhost port:5005
# curator分布式锁在spring定是任性下执行失败分析
定时任务的实质：实现runnable的线程ScheduledMethodRunnable的schedule执行。
在ScheduledMethodRunnable里通过反射执行有@Schedule注解的定时方法。
反射执行时，找不到curator里的validatePath方法。与普通方法执行不一样，
反射执行首先根据类文件里方法声明做判断方法是否存在。


