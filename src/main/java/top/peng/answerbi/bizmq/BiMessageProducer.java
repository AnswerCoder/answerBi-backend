/*
 * @(#)BiMessageProducer.java
 *
 * Copyright © 2023 YunPeng Corporation.
 */
package top.peng.answerbi.bizmq;

import javax.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * BiMessageProducer
 *
 * @author yunpeng
 * @version 1.0 2023/7/24
 */
@Component
public class BiMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String msg){
        rabbitTemplate.convertAndSend(BiMqConstant.BI_EXCHANGE_NAME, BiMqConstant.BI_ROUTING_KEY,
                msg, message -> {
                    //给消息设置延迟毫秒值,如果该消息30s未被消费，会被丢弃或进入死信队列(如果实现了的话)
                    message.getMessageProperties().setExpiration("30000");
                    return message;
                });
    }
}
