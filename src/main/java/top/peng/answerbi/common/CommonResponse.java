package top.peng.answerbi.common;

import java.io.Serializable;
import lombok.Data;

/**
 * 通用返回类
 *
 * @param <T>
 * @author yunpeng
 * @version 1.0 2023/5/16
 */
@Data
public class CommonResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    public CommonResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public CommonResponse(int code, T data) {
        this(code, data, "");
    }

    public CommonResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
