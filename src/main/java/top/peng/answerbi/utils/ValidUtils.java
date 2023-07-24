/*
 * @(#)ValidUtils.java
 *
 * Copyright © 2023 YunPeng Corporation.
 */
package top.peng.answerbi.utils;

import cn.hutool.core.io.FileUtil;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import top.peng.answerbi.common.ErrorCode;
import top.peng.answerbi.exception.ThrowUtils;

/**
 * ValidUtils 校验工具类
 *
 * @author yunpeng
 * @version 1.0 2023/7/20
 */
public class ValidUtils {
    public static void validFile(MultipartFile multipartFile, int limitSizeMb, List<String> validFileSuffixList) {
        // 文件大小
        long fileSize = multipartFile.getSize();

        final long LIMIT_M = limitSizeMb * 1024 * 1024L;

        ThrowUtils.throwIf(fileSize > LIMIT_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 "+ limitSizeMb +" M");

        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());


        ThrowUtils.throwIf(!validFileSuffixList.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }
}
