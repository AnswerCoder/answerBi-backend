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
 * RateLimiter 限流注解
 *
 * @author yunpeng
 * @version 1.0 2023/7/20
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiterTag {
    int NOT_LIMITED = 0;
    /**
     * 用户qps， 每个用户每秒的请求限制
     */
    double qps() default NOT_LIMITED;

    /**
     * 超时时长
     */
    int timeout() default 0;

    /**
     * 超时时间单位
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
