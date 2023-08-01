/*
 * @(#)BiMessageProducer.java
 *
 * Copyright © 2023 YunPeng Corporation.
 */
package top.peng.answerbi.bizmq;

import com.rabbitmq.client.Channel;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import top.peng.answerbi.common.ErrorCode;
import top.peng.answerbi.constant.BiConstant;
import top.peng.answerbi.exception.BusinessException;
import top.peng.answerbi.manager.AiManager;
import top.peng.answerbi.model.entity.Chart;
import top.peng.answerbi.model.enums.BiTaskStatusEnum;
import top.peng.answerbi.model.vo.BiResponse;
import top.peng.answerbi.service.ChartService;
import top.peng.answerbi.utils.bizutils.BiUtils;

/**
 * BiMessageProducer
 *
 * @author yunpeng
 * @version 1.0 2023/7/24
 */
@Component
@Slf4j
public class BiMessageConsumer {

    @Resource
    private ChartService chartService;

    @Resource
    private AiManager aiManager;

    /**
     * 接收消息
     * @param message
     * @param channel
     * @param deliveryTag @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag 是一个方法参数注解，用于从消息头中获取投递标签
     *        在RabbitMQ中，每条消息者都会被分配一个唯一的投递标签，用于标识该消息在通道中的投递状态和顺序
     */
    @SneakyThrows
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        log.info("receiveMessage message = {}", message);
        if (StringUtils.isBlank(message)){
            //如果更新失败，未拿到chartId, 拒绝当前消息， 让消息重新进入队列
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);

        if (chart == null){
            //如果更新失败，未拿到chartId, 拒绝当前消息， 让消息重新进入队列
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图表为空");
        }

        //先修改图表任务状态为“执行中”;
        boolean updateRunningRes = chartService.updateChartStatus(chart.getId(), BiTaskStatusEnum.RUNNING.getValue(), null);
        if (!updateRunningRes){
            channel.basicNack(deliveryTag, false, false);
            chartService.updateChartStatus(chart.getId(), BiTaskStatusEnum.FAILED.getValue(), "更新图表执行中状态失败");
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图表为空");
        }
        BiResponse biResponse;
        try {
            //调用AI
            String aiResult = aiManager.doChat(BiConstant.BI_MODEL_ID, BiUtils.buildUserInputForAi(chart));
            biResponse = aiManager.aiAnsToBiResp(aiResult);
        } catch (BusinessException e) {
            channel.basicNack(deliveryTag, false, false);
            //执行失败，状态修改为“失败”,记录任务失败信息
            chartService.updateChartStatus(chart.getId(),BiTaskStatusEnum.FAILED.getValue(), e.getMessage());
            throw e;
        }
        //执行成功后，修改为“已完成”、保存执行结果
        biResponse.setChartId(chart.getId());
        boolean updateSucceedRes = chartService.updateChartSucceedResult(biResponse);
        if (!updateSucceedRes){
            channel.basicNack(deliveryTag, false, false);
            chartService.updateChartStatus(chart.getId(), BiTaskStatusEnum.FAILED.getValue(), "更新图表成功状态失败");
        }

        //收到确认消息的接收
        //投递标签 deliveryTag 是一个数字标识，它在消息消费者接收到消息之后用于向RabbitMq确认消息的处理状态
        //通过将投递标签传递给channel.basicAck(deliveryTag,false)方法，可以告知RabbitMQ该消息已经成功处理，可以进行确认和从队列中删除
        channel.basicAck(deliveryTag, false);
    }
}
