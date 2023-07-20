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
import top.peng.answerbi.annotation.RateLimiterTag;
import top.peng.answerbi.common.ErrorCode;
import top.peng.answerbi.exception.ThrowUtils;
import top.peng.answerbi.model.entity.User;
import top.peng.answerbi.service.UserService;

/**
 * RateLimiterInterceptor 限流切面
 *
 * @author yunpeng
 * @version 1.0 2023/7/20
 */
@Slf4j
@Aspect
@Component
public class RateLimiterInterceptor {
    @Resource
    private UserService userService;

    private static final ConcurrentMap<String, RateLimiter> RATE_LIMITER_CACHE = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimiterTag)")
    public Object doInterceptor(ProceedingJoinPoint point, RateLimiterTag rateLimiterTag) throws Throwable {

        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 当前请求方法
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        String key = loginUser.getId() + "#" + method.getName();
        if (rateLimiterTag != null && rateLimiterTag.qps() > RateLimiterTag.NOT_LIMITED) {
            double qps = rateLimiterTag.qps();

            if (RATE_LIMITER_CACHE.get(key) == null) {
                // 初始化 QPS
                RATE_LIMITER_CACHE.put(key, RateLimiter.create(qps));
            }

            log.debug("【{}】每个用户的QPS设置为: {}", method.getName(), RATE_LIMITER_CACHE.get(key).getRate());
            // 尝试获取令牌
            if (RATE_LIMITER_CACHE.get(key) != null){
                RateLimiter limiter = RATE_LIMITER_CACHE.get(key);
                ThrowUtils.throwIf(
                        !limiter.tryAcquire(rateLimiterTag.timeout(), rateLimiterTag.timeUnit()),
                        ErrorCode.TOO_MANY_REQUEST);
            }
        }
        return point.proceed();
    }

}
