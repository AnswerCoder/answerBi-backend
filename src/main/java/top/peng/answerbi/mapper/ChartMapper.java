package top.peng.answerbi.mapper;

import org.apache.ibatis.annotations.CacheNamespace;
import top.peng.answerbi.manager.MybatisRedisCacheManager;
import top.peng.answerbi.model.entity.Chart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author yunpeng.zhang
* @description 针对表【chart(图表信息表)】的数据库操作Mapper
* @createDate 2023-07-10 16:45:42
* @Entity top.peng.answerbi.model.entity.Chart
*/
@CacheNamespace(implementation = MybatisRedisCacheManager.class)
public interface ChartMapper extends BaseMapper<Chart> {

}




