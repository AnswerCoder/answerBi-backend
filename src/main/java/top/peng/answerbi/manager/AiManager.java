/*
 * @(#)AiManager.java
 *
 * Copyright © 2023 YunPeng Corporation.
 */
package top.peng.answerbi.manager;

import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import top.peng.answerbi.common.ErrorCode;
import top.peng.answerbi.constant.BiConstant;
import top.peng.answerbi.exception.BusinessException;
import top.peng.answerbi.exception.ThrowUtils;
import top.peng.answerbi.model.vo.BiResponse;

/**
 * AiManager AI对话
 *
 * @author yunpeng
 * @version 1.0 2023/7/14
 */
@Service
public class AiManager {

    @Resource
    private YuCongMingClient yuCongMingClient;

    /**
     * AI 对话
     *
     * @param modelId 模型id
     * @param message 提问
     * @return 结果
     */
    public String doChat(long modelId, String message){
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);

        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);

        ThrowUtils.throwIf(response == null, ErrorCode.SYSTEM_ERROR,"AI响应错误");

        return response.getData().getContent();
    }

    /**
     * 将AI生成的结果转换为 BiResponse
     * @param aiAnswer  AI 对话 结果
     * @return BiResponse对象
     */
    public BiResponse aiAnsToBiResp(String aiAnswer) throws BusinessException {
        String[] aiResultSplit = aiAnswer.split(BiConstant.BI_RESULT_SEPARATOR);
        ThrowUtils.throwIf(aiResultSplit.length < 3,ErrorCode.SYSTEM_ERROR,"AI 生成错误");
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(aiResultSplit[1].trim());
        biResponse.setGenResult(aiResultSplit[2].trim());
        return biResponse;
    }
}
