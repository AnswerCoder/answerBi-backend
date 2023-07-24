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

    public void sendMessage(String message){
        rabbitTemplate.convertAndSend(BiMqConstant.BI_EXCHANGE_NAME,BiMqConstant.BI_ROUTING_KEY,message);
    }
}
