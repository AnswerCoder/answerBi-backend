package top.peng.answerbi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.peng.answerbi.model.entity.Post;
import java.util.Date;
import java.util.List;

/**
 * 帖子数据库操作
 *
 * @author yunpeng
 * @version 1.0 2023/5/16
 */
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 查询帖子列表（包括已被删除的数据）
     */
    List<Post> listPostWithDelete(Date minUpdateTime);

}




