/*
 * @(#)ExcelUtils.java
 *
 * Copyright © 2023 YunPeng Corporation.
 */
package top.peng.answerbi.utils;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import io.swagger.models.auth.In;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * ExcelUtils Excel相关工具类
 *
 * @author yunpeng
 * @version 1.0 2023/7/12
 */
@Slf4j
public class ExcelUtils {
    public static String excelToCsv(MultipartFile multipartFile){
        List<Map<Integer, String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("表格处理错误",e);
        }
        //如果数据为空
        if (CollUtil.isEmpty(list)){
            return "";
        }
        //转换为csv
        StringBuilder sb = new StringBuilder();
        //读取表头(第一行)
        Map<Integer, String> headerMap = list.get(0);
        List<String> headerList = headerMap.values().stream().filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toList());
        sb.append(StringUtils.join(headerList,",")).append("\n");
        //读取数据
        for (int i = 1; i < list.size();i++){
            Map<Integer, String> dataMap = list.get(i);
            List<String> dataList = dataMap.values().stream().filter(ObjectUtils::isNotEmpty)
                    .collect(Collectors.toList());
            sb.append(StringUtils.join(dataList,",")).append("\n");
        }
        return sb.toString();
    }
}
