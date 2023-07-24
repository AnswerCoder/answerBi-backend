/*
 * @(#)BiUtils.java
 *
 * Copyright © 2023 YunPeng Corporation.
 */
package top.peng.answerbi.utils.bizutils;

import org.apache.commons.lang3.StringUtils;
import top.peng.answerbi.model.entity.Chart;

/**
 * BiUtils 业务工具
 *
 * @author yunpeng
 * @version 1.0 2023/7/24
 */
public class BiUtils {

    /**
     * 构建AI可以识别的用户输入
     */
    public static String buildUserInputForAi(Chart chart){
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String csvData = chart.getChartData();
        //用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        //拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)){
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        userInput.append(csvData).append("\n");
        return userInput.toString();
    }
}
