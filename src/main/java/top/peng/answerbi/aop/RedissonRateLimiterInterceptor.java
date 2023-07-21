/*
 * @(#)RateLimiterInterceptor.java
 *
 * Copyright © 2023 YunPeng Corporation.
 */
package top.peng.answerbi.aop;

import com.google.common.util.concurrent.RateLimiter;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.peng.answerbi.annotation.GuavaRateLimiter;
import top.peng.answerbi.annotation.RedissonRateLimiter;
import top.peng.answerbi.common.ErrorCode;
import top.peng.answerbi.exception.ThrowUtils;
import top.peng.answerbi.manager.RedisLimiterManager;
import top.peng.answerbi.model.entity.User;
import top.peng.answerbi.service.UserService;

/**
 * RedissonRateLimiterInterceptor 分布式限流切面
 *
 * @author yunpeng
 * @version 1.0 2023/7/20
 */
@Slf4j
@Aspect
@Component
public class RedissonRateLimiterInterceptor {
    @Resource
    private UserService userService;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    private static final ConcurrentMap<String, RateLimiter> RATE_LIMITER_CACHE = new ConcurrentHashMap<>();

    @Around("@annotation(rRateLimiter)")
    public Object doInterceptor(ProceedingJoinPoint point, RedissonRateLimiter rRateLimiter) throws Throwable {

        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 当前请求方法
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        String key = loginUser.getId() + "#" + method.getName();
        if (rRateLimiter != null && rRateLimiter.qps() > RedissonRateLimiter.NOT_LIMITED) {
            redisLimiterManager.doRateLimit(key, rRateLimiter.qps());
        }
        return point.proceed();
    }

}
