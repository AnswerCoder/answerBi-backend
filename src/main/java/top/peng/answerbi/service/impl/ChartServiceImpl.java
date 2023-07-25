package top.peng.answerbi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.peng.answerbi.constant.CommonConstant;
import top.peng.answerbi.mapper.ChartMapper;
import top.peng.answerbi.model.dto.chart.ChartQueryRequest;
import top.peng.answerbi.model.entity.Chart;
import top.peng.answerbi.model.enums.BiTaskStatusEnum;
import top.peng.answerbi.model.vo.BiResponse;
import top.peng.answerbi.service.ChartService;
import top.peng.answerbi.utils.SqlUtils;

/**
* @author yunpeng.zhang
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2023-07-10 16:45:42
*/
@Service
@Slf4j
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{

    /**
     * 更新图表状态
     *
     * @param chartId
     * @param status
     * @param execMessage
     */
    @Override
    @Transactional(rollbackFor = { Exception.class })
    public boolean updateChartStatus(long chartId, String status, String execMessage) {
        LambdaUpdateWrapper<Chart> wrapper = new LambdaUpdateWrapper<Chart>()
                .set(Chart::getStatus, status)
                .set(Chart::getExecMessage,execMessage)
                .eq(Chart::getId, chartId);
        boolean updateResult = this.update(wrapper);
        if (!updateResult){
            log.error("更新图表[{}]状态失败", chartId);
        }
        return updateResult;
    }

    /**
     * 更新图表生成成功结果
     *
     * @param biResponse
     */
    @Override
    @Transactional(rollbackFor = { Exception.class })
    public boolean updateChartSucceedResult(BiResponse biResponse) {
        LambdaUpdateWrapper<Chart> wrapper = new LambdaUpdateWrapper<Chart>()
                .set(Chart::getStatus, BiTaskStatusEnum.SUCCEED.getValue())
                .set(Chart::getExecMessage,null)
                .set(Chart::getGenChart, biResponse.getGenChart())
                .set(Chart::getGenResult, biResponse.getGenResult())
                .eq(Chart::getId, biResponse.getChartId());
        boolean updateResult = this.update(wrapper);
        if (!updateResult){
            log.error("更新图表[{}]结果失败", biResponse.getChartId());
        }
        return updateResult;
    }

    /**
     * 获取查询条件
     *
     * @param chartQueryRequest@return
     */
    @Override
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chartQueryRequest.getId();
        String chartName = chartQueryRequest.getChartName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(chartName), "chart_name", chartName);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chart_type", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "user_id", userId);
        queryWrapper.eq("deleted_flag", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;

    }
}




