/*
 * @(#)MyBatisRedisCache.java
 *
 * Copyright © 2023 YunPeng Corporation.
 */
package top.peng.answerbi.manager;

import cn.hutool.core.util.RandomUtil;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.Cache;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import top.peng.answerbi.utils.SpringContextUtils;

/**
 * MyBatisRedisCache mybaits 缓存工具类
 *
 * @author yunpeng
 * @version 1.0 2023/7/27
 */
@Slf4j
public class MybatisRedisCacheManager implements Cache {

    private RedisTemplate<Object,Object> redisTemplate;

    // 读写锁
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

    // cache instance id
    private final String id;

    private static final long EXPIRE_TIME_IN_MINUTES = 30; // redis过期时间
    
    public MybatisRedisCacheManager(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cache instances require an ID");
        }
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Put query result to redis
     *
     * @param key
     * @param value
     */
    @Override
    public void putObject(Object key, Object value) {
        getRedisTemplate().opsForValue().set(key.toString(), value, EXPIRE_TIME_IN_MINUTES + RandomUtil.randomInt(10), TimeUnit.MINUTES);
        log.info("Put query result to redis, key={}",key);
    }

    @Override
    public Object getObject(Object key) {
        try {
            log.info("Get cached query result from redis, key={}",key);
            return getRedisTemplate().opsForValue().get(key.toString());
        } catch (Exception e) {
            log.error("Get cached query result from redis failed , key={}",key);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object removeObject(Object key) {
        if (key != null){
            getRedisTemplate().delete(key.toString());
            log.info("Remove cached query result from redis, key={}",key);
        }
        return null;
    }

    @Override
    public void clear() {
        Set<Object> keys = getRedisTemplate().keys("*:" + this.id + "*");
        if (!CollectionUtils.isEmpty(keys)) {
            getRedisTemplate().delete(keys);
        }
        log.info("Clear all the cached query result from redis");
    }

    @Override
    public int getSize() {
        Long size = getRedisTemplate().execute(RedisServerCommands::dbSize);
        if (size == null) return 0;
        return size.intValue();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    private RedisTemplate<Object,Object> getRedisTemplate(){
        //通过SpringContextUtils工具类获取RedisTemplate
        if (redisTemplate == null) {
            redisTemplate = (RedisTemplate<Object,Object>) SpringContextUtils.getBean("redisTemplate");
        }
        return redisTemplate;
    }
}
