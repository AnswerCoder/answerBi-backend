package top.peng.answerbi.common;

import java.io.Serializable;
import lombok.Data;

/**
 * 删除请求
 *
 * @author yunpeng
 * @version 1.0 2023/5/16
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}