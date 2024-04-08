package top.peng.answerbi.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.peng.answerbi.annotation.AuthCheck;
import top.peng.answerbi.annotation.RedissonRateLimiter;
import top.peng.answerbi.bizmq.BiMessageProducer;
import top.peng.answerbi.common.CommonResponse;
import top.peng.answerbi.common.DeleteRequest;
import top.peng.answerbi.common.ErrorCode;
import top.peng.answerbi.common.ResultUtils;
import top.peng.answerbi.constant.BiConstant;
import top.peng.answerbi.constant.UserConstant;
import top.peng.answerbi.exception.BusinessException;
import top.peng.answerbi.exception.ThrowUtils;
import top.peng.answerbi.manager.AiManager;
import top.peng.answerbi.model.dto.chart.ChartAddRequest;
import top.peng.answerbi.model.dto.chart.ChartEditRequest;
import top.peng.answerbi.model.dto.chart.ChartQueryRequest;
import top.peng.answerbi.model.dto.chart.ChartUpdateRequest;
import top.peng.answerbi.model.dto.chart.GenChartByAiRequest;
import top.peng.answerbi.model.entity.Chart;
import top.peng.answerbi.model.entity.User;
import top.peng.answerbi.model.enums.BiTaskStatusEnum;
import top.peng.answerbi.model.vo.BiResponse;
import top.peng.answerbi.service.ChartService;
import top.peng.answerbi.service.UserService;
import top.peng.answerbi.utils.ExcelUtils;
import top.peng.answerbi.utils.ValidUtils;
import top.peng.answerbi.utils.bizutils.BiUtils;

/**
 * 图表接口
 *
 * @author yunpeng
 * @version 1.0 2023/5/16
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private AiManager aiManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private BiMessageProducer biMessageProducer;

    @Value("${upload.dir}")
    private String uploadDir;
    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public CommonResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public CommonResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public CommonResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public CommonResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public CommonResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public CommonResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public CommonResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 智能分析 (同步)
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen")
    @RedissonRateLimiter(qps = 1)
    public CommonResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
            GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        //生成参数
        Chart chart = preHandleGenChartRequest(genChartByAiRequest, multipartFile, request);
        String userInput = BiUtils.buildUserInputForAi(chart);

        //调用AI
        String aiResult = aiManager.doChat(BiConstant.BI_MODEL_ID, userInput);
        BiResponse biResponse = aiManager.aiAnsToBiResp(aiResult);

        //插入数据库
        BeanUtils.copyProperties(biResponse,chart);
        chart.setStatus(BiTaskStatusEnum.SUCCEED.getValue());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }


    /**
     * 智能分析 (异步)
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    @RedissonRateLimiter(qps = 1)
    public CommonResponse<BiResponse> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
            GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        //生成参数
        Chart chart = preHandleGenChartRequest(genChartByAiRequest, multipartFile, request);
        String userInput = BiUtils.buildUserInputForAi(chart);

        //先插入数据库, 状态为排队中
        chart.setStatus(BiTaskStatusEnum.WAIT.getValue());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");



        //创建线程任务
        CompletableFuture.runAsync(() -> {
            //先修改图表任务状态为“执行中”;
            chartService.updateChartStatus(chart.getId(),BiTaskStatusEnum.RUNNING.getValue(), null);

            //调用AI
            String aiResult = aiManager.doChat(BiConstant.BI_MODEL_ID, userInput);
            BiResponse biResponse;
            try {
                biResponse = aiManager.aiAnsToBiResp(aiResult);
            } catch (BusinessException e) {
                //执行失败，状态修改为“失败”,记录任务失败信息
                chartService.updateChartStatus(chart.getId(), BiTaskStatusEnum.FAILED.getValue(), "AI生成错误");
                throw e;
            }
            //执行成功后，修改为“已完成”、保存执行结果
            biResponse.setChartId(chart.getId());
            chartService.updateChartSucceedResult(biResponse);
        }, threadPoolExecutor).exceptionally((e) -> {
            log.error("AI生成错误 chartId = {} userId = {} error = {}", chart.getUserId(), chart.getUserId(), e.getMessage());
            chartService.updateChartStatus(chart.getId(), BiTaskStatusEnum.FAILED.getValue(), "AI生成错误");
            return null;
        });

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }

    /**
     * 智能分析 (异步消息队列)
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async/mq")
    @RedissonRateLimiter(qps = 1)
    public CommonResponse<BiResponse> genChartByAiAsyncMq(@RequestPart("file") MultipartFile multipartFile,
            GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        //生成参数
        Chart chart = preHandleGenChartRequest(genChartByAiRequest, multipartFile, request);

        //先插入数据库, 状态为排队中
        chart.setStatus(BiTaskStatusEnum.WAIT.getValue());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        biMessageProducer.sendMessage(String.valueOf(chart.getId()));

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }

    /**
     * 重新生成
     *
     * @param chartId
     * @return
     */
    @PostMapping("/regen")
    @RedissonRateLimiter(qps = 1)
    public CommonResponse<BiResponse> regenChartByAiAsyncMq(Long chartId) {
        ThrowUtils.throwIf(chartId == null, ErrorCode.PARAMS_ERROR, "数据不存在");
        //更新状态为等待中
        boolean update = chartService.updateChartStatus(chartId, BiTaskStatusEnum.WAIT.getValue(), null);
        ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR, "图表状态更新失败");

        biMessageProducer.sendMessage(String.valueOf(chartId));
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chartId);
        return ResultUtils.success(biResponse);
    }

    @ApiOperation(value = "上传文件到服务器指定路径")
    @PostMapping("/uploadtest")
    public String handleFileUpload(@RequestPart("file") MultipartFile file) throws IOException {
        if (!file.isEmpty()){
            String fileName = file.getOriginalFilename();
            File targetFile = new File(uploadDir, fileName);
            FileCopyUtils.copy(file.getBytes(),targetFile);
            return "redirect:/success";
        }
        return "redirect:/error";
    }


    /**
     * 预处理请求  根据用户输入构建 要存入数据库的 Chart 对象
     *
     * @param genChartByAiRequest
     * @param multipartFile
     * @param request
     * @return
     */
    private Chart preHandleGenChartRequest(GenChartByAiRequest genChartByAiRequest,MultipartFile multipartFile, HttpServletRequest request){

        //通过request对象拿到用户id(必须登录才能使用)
        User loginUser = userService.getLoginUser(request);

        String chartName = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        //校验
        //如果分析目标为空，就抛出请求参数错误异常，并给出提示
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"分析目标为空");
        //如果名称不为空，并且名称长度大于100，就抛出异常，并给出提示
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 100,ErrorCode.PARAMS_ERROR,"图表名称过长");
        ValidUtils.validFile(multipartFile, 1, Arrays.asList("xls","xlsx"));

        String csvData = ExcelUtils.excelToCsv(multipartFile);

        Chart chart = new Chart();
        chart.setChartName(chartName);
        chart.setGoal(goal);
        chart.setChartType(chartType);
        chart.setChartData(csvData);
        chart.setUserId(loginUser.getId());
        return chart;
    }

}
