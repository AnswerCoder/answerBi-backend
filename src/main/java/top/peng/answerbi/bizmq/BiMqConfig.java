/*
 * @(#)BiMqConfig.java
 *
 * Copyright © 2023 YunPeng Corporation.
 */
package top.peng.answerbi.bizmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * BiMqConfig
 *
 * @author yunpeng
 * @version 1.0 2023/7/25
 */
@Configuration
public class BiMqConfig {

    //声明死信队列
    @Bean
    public Queue deadLetterQueue(){
        return new Queue(BiMqConstant.BI_DLX_QUEUE_NAME);
    }

    //声明死信交换机
    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder
                .directExchange(BiMqConstant.BI_DLX_EXCHANGE_NAME)
                .build();
    }


    //声明Bi分析业务队列
    @Bean
    public Queue biQueue(){
        return QueueBuilder
                .durable(BiMqConstant.BI_QUEUE_NAME)
                //绑定死信交换机
                .deadLetterExchange(BiMqConstant.BI_DLX_EXCHANGE_NAME)
                //绑定死信的路由key
                .deadLetterRoutingKey(BiMqConstant.BI_DLX_ROUTING_KEY)
                .build();
    }

    //声明Bi分析业务交换机
    @Bean
    public DirectExchange biExchange() {
        return ExchangeBuilder
                .directExchange(BiMqConstant.BI_EXCHANGE_NAME)
                .build();
    }

    //绑定Bi分析业务队列到Bi分析业务交换机
    @Bean
    public Binding biBinding(){
        return BindingBuilder
                .bind(biQueue())
                .to(biExchange())
                .with(BiMqConstant.BI_ROUTING_KEY);
    }
    //绑定Bi分析业务队列到Bi分析业务交换机
    @Bean
    public Binding DeadLetterBinding(){
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(BiMqConstant.BI_DLX_ROUTING_KEY);
    }
}
