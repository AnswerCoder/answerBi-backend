/*
 * @(#)GuavaRetryingConfig.java
 *
 * Copyright © 2023 YunPeng Corporation.
 */
package top.peng.answerbi.manager;

import com.github.rholder.retry.AttemptTimeLimiters;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Predicates;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;
import top.peng.answerbi.listener.RetryLogListener;
import top.peng.answerbi.utils.SpinBlockStrategy;

/**
 * BiRetryerBuilder Bi智能分析业务重试机制
 *
 * @author yunpeng
 * @version 1.0 2023/7/31
 */
@Component
public class AiRetryerBuilder {

    public Retryer<BaseResponse<DevChatResponse>> build(){
        return RetryerBuilder.<BaseResponse<DevChatResponse>>newBuilder()
                .retryIfResult(Predicates.isNull())
                //发生IO异常时重试
                .retryIfExceptionOfType(IOException.class)
                //发生运行时异常时重试
                .retryIfRuntimeException()
                //重试策略 递增等待时长策略(降频重试) 依次在失败后的第5s、15s进行降频重试。
                .withWaitStrategy(WaitStrategies.incrementingWait(5, TimeUnit.SECONDS,5,TimeUnit.SECONDS))
                //最多执行3次（首次执行+最多重试2次）
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                //超时限制 超时则中断执行，继续重试。
                .withAttemptTimeLimiter(AttemptTimeLimiters.fixedTimeLimit(90,TimeUnit.SECONDS))
                //自定义阻塞策略：自旋锁
                .withBlockStrategy(new SpinBlockStrategy())
                //重试监听器
                .withRetryListener(new RetryLogListener())
                .build();
    }
}
