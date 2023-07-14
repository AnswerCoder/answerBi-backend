/*
 * @(#)BiResponse.java
 *
 * Copyright © 2023 YunPeng Corporation.
 */
package top.peng.answerbi.model.vo;

import lombok.Data;

/**
 * BiResponse Bi 的返回结果
 *
 * @author yunpeng
 * @version 1.0 2023/7/14
 */
@Data
public class BiResponse {

    /**
     * 生成的图表数据
     */
    private String genChart;

    /**
     * 生成的分析结论
     */
    private String genResult;

    /**
     * 新生成的图表Id
     */
    private Long chartId;
}
