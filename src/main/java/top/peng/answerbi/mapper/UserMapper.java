package top.peng.answerbi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import top.peng.answerbi.manager.MybatisRedisCacheManager;
import top.peng.answerbi.model.entity.User;

/**
* @author yunpeng.zhang
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2023-07-10 16:45:42
* @Entity top.peng.answerbi.model.entity.User
*/
@CacheNamespace(implementation = MybatisRedisCacheManager.class)
public interface UserMapper extends BaseMapper<User> {

}




