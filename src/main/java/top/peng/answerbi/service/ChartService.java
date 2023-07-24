package top.peng.answerbi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import top.peng.answerbi.model.dto.chart.ChartQueryRequest;
import top.peng.answerbi.model.entity.Chart;
import top.peng.answerbi.model.vo.BiResponse;

/**
* @author yunpeng.zhang
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2023-07-10 16:45:42
*/
public interface ChartService extends IService<Chart> {

    /**
     * 更新图表状态
     * @param chartId
     * @param status
     * @param execMessage
     */
    boolean updateChartStatus(long chartId,String status,String execMessage);

    /**
     * 更新图表生成成功结果
     * @param biResponse
     */
    boolean updateChartSucceedResult(BiResponse biResponse);


    /**
     * 获取查询条件
     *
     * @param chartQueryRequest
     * @return
     */
    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);
}
