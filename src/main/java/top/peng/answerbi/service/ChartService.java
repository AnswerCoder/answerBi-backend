package top.peng.answerbi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import top.peng.answerbi.model.dto.chart.ChartQueryRequest;
import top.peng.answerbi.model.dto.post.PostQueryRequest;
import top.peng.answerbi.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import top.peng.answerbi.model.entity.Post;

/**
* @author yunpeng.zhang
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2023-07-10 16:45:42
*/
public interface ChartService extends IService<Chart> {

    /**
     * 获取查询条件
     *
     * @param postQueryRequest
     * @return
     */
    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);
}
