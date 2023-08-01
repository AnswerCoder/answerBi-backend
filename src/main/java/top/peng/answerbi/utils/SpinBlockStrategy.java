/*
 * @(#)SpinBlockStrategy.java
 *
 * Copyright © 2023 YunPeng Corporation.
 */
package top.peng.answerbi.utils;


import com.github.rholder.retry.BlockStrategy;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SpinBlockStrategy 自旋锁的实现, 不响应线程中断
 *
 * @author yunpeng
 * @version 1.0 2023/7/31
 */
@Slf4j
@NoArgsConstructor
public class SpinBlockStrategy implements BlockStrategy {

    @Override
    public void block(long sleepTime) {
        LocalDateTime startTime = LocalDateTime.now();

        long start = System.currentTimeMillis();
        long end = start;
        log.info("[SpinBlockStrategy]...begin wait.");

        while (end - start <= sleepTime) {
            end = System.currentTimeMillis();
        }

        //使用Java8新增的Duration计算时间间隔
        Duration duration = Duration.between(startTime, LocalDateTime.now());

        log.info("[SpinBlockStrategy]...end wait.duration={}", duration.toMillis());
    }
}
