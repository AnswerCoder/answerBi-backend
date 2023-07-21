/*
 * @(#)RedissonConfig.java
 *
 * Copyright © 2023 YunPeng Corporation.
 */
package top.peng.answerbi.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RedissonConfig
 *
 * @author yunpeng
 * @version 1.0 2023/7/20
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private Integer database;

    private String host;

    private Integer port;

    private String password;

    @Bean
    public RedissonClient getRedissonClient(){
        Config config = new Config();
        //添加单机redisson配置
        config.useSingleServer()
                .setDatabase(database)
                .setAddress("redis://" + host + ":" + port)
                .setPassword(password);
        return Redisson.create(config);
    }
}
