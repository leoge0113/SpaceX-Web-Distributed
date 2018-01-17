package com.cainiao.core.user.quartz;

import com.cainiao.core.user.dao.UserDao;
import com.cainiao.spacex.distributed.locks.DistributedReentrantLock;
import com.cainiao.spacex.distributed.locks.ExecuteCallBack;
import com.cainiao.spacex.distributed.locks.impl.DistributedReentrantLockExeTempImpl;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/**
 * 业务相关的作业调度
 * <p>
 * 字段               允许值                           允许的特殊字符
 * 秒	 	0-59	 	, - * /
 * 分	 	0-59	 	, - * /
 * 小时	 	0-23	 	, - * /
 * 日期	 	1-31	 	, - * ? / L W C
 * 月份	 	1-12 或者 JAN-DEC	 	, - * /
 * 星期	 	1-7 或者 SUN-SAT	 	, - * ? / L C #
 * 年（可选）	 	留空, 1970-2099	 	, - * /
 * <p>
 * 字符代表所有可能的值
 * /  字符用来指定数值的增量
 * L  字符仅被用于天（月）和天（星期）两个子表达式，表示一个月的最后一天或者一个星期的最后一天
 * 6L 可以表示倒数第６天
 *

 */
@Component
public class UserQuartz {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserDao userDao;

    /**
     * 用户自动加积分
     * 每天9点到17点每过1分钟所有用户加一次积分
     * <p>
     * (这里要注意集群环境 可能出现重复触发的情况)
     * 这里采用分布式锁的方式解决重复触发的问题
     * <p>
     * TODO zookeeper地址采用配置文件的方式读入，并且为高可用。
     * TODO 多个线程下第一次同时调用new DistributedLock（）会冲突问题
     */
    @Scheduled(cron = "0 0/1 9-17 * * ? ")
    public void addUserScore() throws KeeperException, InterruptedException {
        DistributedReentrantLockExeTempImpl distributedReentrantLockExeTemp = new DistributedReentrantLockExeTempImpl();
        distributedReentrantLockExeTemp.execute("quartz", 5000, new ExecuteCallBack() {
            @Override
            public Object onGetLock() throws InterruptedException {
                userDao.addScore(10);
                return null;
            }

            @Override
            public Object onTimeout() throws InterruptedException {
                return null;
            }
        });

       // if (lock.tryLock()) {
          //  LOG.info("@Scheduled--------addUserScore()");
            //userDao.addScore(10);
       // }
        //lock.unlock();//释放锁
    }

}
