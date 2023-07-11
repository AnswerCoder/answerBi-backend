package top.peng.answerbi.service;

import top.peng.answerbi.model.entity.PostThumb;
import com.baomidou.mybatisplus.extension.service.IService;
import top.peng.answerbi.model.entity.User;

/**
 * 帖子点赞服务
 *
 * @author yunpeng
 * @version 1.0 2023/5/16
 */
public interface PostThumbService extends IService<PostThumb> {

    /**
     * 点赞
     *
     * @param postId
     * @param loginUser
     * @return
     */
    int doPostThumb(long postId, User loginUser);

    /**
     * 帖子点赞（内部服务）
     *
     * @param userId
     * @param postId
     * @return
     */
    int doPostThumbInner(long userId, long postId);
}
