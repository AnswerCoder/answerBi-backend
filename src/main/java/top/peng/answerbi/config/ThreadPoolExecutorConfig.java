/*
 * @(#)ThreeadPoolExecutorConfig.java
 *
 * Copyright © 2023 YunPeng Corporation.
 */
package top.peng.answerbi.config;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ThreadPoolExecutorConfig 线程池配置
 *
 * @author yunpeng
 * @version 1.0 2023/7/21
 */
@Configuration
public class ThreadPoolExecutorConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        //创建一个线程工厂
        ThreadFactory threadFactory = new ThreadFactory() {
            //初始化线程数为1
            private int count = 1;
            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("线程" + count);
                count++;
                return thread;
            }
        };
        //创建一个线程池，核心大小为2，最大线程数为4, 非核心线程空闲100秒即被释放
        //任务队列为阻塞队列，长度为4，使用上方创建的线程工厂 threadFactory 来创建线程
        return new ThreadPoolExecutor(2,4,100,
                TimeUnit.SECONDS,new ArrayBlockingQueue<>(4),threadFactory);
    }
}
