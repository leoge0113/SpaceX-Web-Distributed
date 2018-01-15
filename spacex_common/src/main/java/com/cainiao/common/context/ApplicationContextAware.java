package com.cainiao.common.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

/*
@Lazy
默认情况下，Spring 容器在初始化过程中会创建和配置所有单例的bean。
这种提前实例化是可取的，因为配置环境错误会被立即发现而不需要过多的时间。
如果不采取这种行为，可以将单例的bean标记为延迟初始化。
一个延迟初始化的bean告诉Spring IoC容器去创建这个bean实例化对象当它第一次被调用时而不是在容器启动时立即创建。
为什么要实现applicationcontextaware？
在Web应用中，Spring容器通常采用声明式方式配置产生：开发者只要在web.xml中配置一个Listener，
该Listener将会负责初始化Spring容器，MVC框架可以直接调用Spring容器中的Bean，
无需访问Spring容器本身。在这种情况下，容器中的Bean处于容器管理下，无需主动访问容器，
只需接受容器的依赖注入即可。
但在某些特殊的情况下，Bean需要实现某个功能，
但该功能必须借助于Spring容器才能实现，此时就必须让该Bean先获取Spring容器，
然后借助于Spring容器实现该功能。为了让Bean获取它所在的Spring容器，
可以让该Bean实现ApplicationContextAware接口。
*/
@Lazy(value = false)
@Component
public class ApplicationContextAware implements org.springframework.context.ApplicationContextAware {
    private ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext var1) throws BeansException {
        this.ctx = var1;

    }

    public ApplicationContext getApplicationContext() {
        return ctx;
    }
}
