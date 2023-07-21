/*
 * @(#)RateLimiter.java
 *
 * Copyright © 2023 YunPeng Corporation.
 */
package top.peng.answerbi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * RedissonRateLimiter 分布式限流注解   与  GuavaRateLimiter 二选一使用即可
 *
 * @author yunpeng
 * @version 1.0 2023/7/20
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedissonRateLimiter {
    int NOT_LIMITED = 0;
    /**
     * 用户qps， 每个用户每秒的请求限制
     */
    long qps() default NOT_LIMITED;
}
