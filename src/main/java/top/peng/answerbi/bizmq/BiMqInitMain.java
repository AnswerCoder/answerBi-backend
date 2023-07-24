/*
 * @(#)BiMqInitMain.java
 *
 * Copyright © 2023 YunPeng Corporation.
 */
package top.peng.answerbi.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * BiMqInitMain 用于创建测试程序用到的交换机和队列（只用在程序启动前执行一次）
 *
 * @author yunpeng
 * @version 1.0 2023/7/24
 */
public class BiMqInitMain {

    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            String exchangeName = BiMqConstant.BI_EXCHANGE_NAME;
            channel.exchangeDeclare(exchangeName, "direct");

            //创建队列
            String queueName = BiMqConstant.BI_QUEUE_NAME;
            channel.queueDeclare(queueName,true,false,false,null);
            channel.queueBind(queueName, exchangeName, BiMqConstant.BI_ROUTING_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
