/*
 * @(#)BiTaskStatus.java
 *
 * Copyright © 2023 YunPeng Corporation.
 */
package top.peng.answerbi.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;

/**
 * BiTaskStatus 智能分析任务状态枚举
 *
 * @author yunpeng
 * @version 1.0 2023/7/21
 */
public enum BiTaskStatusEnum {

    WAIT("排队中","wait"),
    RUNNING("生成中","running"),
    SUCCEED("成功","succeed"),
    FAILED("失败","failed"),
    ;

    private final String text;

    private final String value;

    BiTaskStatusEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static BiTaskStatusEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (BiTaskStatusEnum anEnum : BiTaskStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
