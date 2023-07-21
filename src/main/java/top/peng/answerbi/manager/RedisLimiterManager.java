/*
 * @(#)RedisLimiterManager.java
 *
 * Copyright © 2023 YunPeng Corporation.
 */
package top.peng.answerbi.manager;

import javax.annotation.Resource;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import top.peng.answerbi.common.ErrorCode;
import top.peng.answerbi.config.RedissonConfig;
import top.peng.answerbi.exception.ThrowUtils;

/**
 * RedisLimiterManager 提供 RedisLimiter 限流基础服务的
 *
 * @author yunpeng
 * @version 1.0 2023/7/21
 */
@Service
public class RedisLimiterManager {
    @Resource
    private RedissonClient redissonClient;

    /**
     * 限流操作
     * @param key 区分不同限流器，比如不同的用户应该分别统计
     */
    public void doRateLimit(String key, long qps){
        //创建一个每秒最低访问两次的限流器 key为限流器名称
        RRateLimiter rRateLimiter = redissonClient.getRateLimiter(key);
        //限流器的规则(每秒 qps 个请求(1秒中生产qps个令牌); 连续的请求,最多只能有1个请求被允许通过)
        //RateType.OVERALL 表示速率限制作用于整个令牌桶, 即所有实例共享
        rRateLimiter.trySetRate(RateType.OVERALL, qps, 1, RateIntervalUnit.SECONDS);
        //请求一个令牌
        boolean canOp = rRateLimiter.tryAcquire(1);
        //没有令牌可用，抛异常
        ThrowUtils.throwIf(!canOp, ErrorCode.TOO_MANY_REQUEST);
    }
}
