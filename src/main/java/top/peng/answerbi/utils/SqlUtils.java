package top.peng.answerbi.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * SQL 工具
 *
 * @author yunpeng
 * @version 1.0 2023/5/16
 */
public class SqlUtils {

    /**
     * 校验排序字段是否合法（防止 SQL 注入）
     *
     * @param sortField
     * @return
     */
    public static boolean validSortField(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            return false;
        }
        return !StringUtils.containsAny(sortField, "=", "(", ")", " ");
    }
}
