package top.peng.answerbi.manager;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.peng.answerbi.constant.BiConstant;

/**
 * AiManagerTest
 *
 * @author yunpeng
 * @version 1.0 2023/7/14
 */
@SpringBootTest
class AiManagerTest {

    @Resource
    private AiManager aiManager;

    @Test
    void doChat() {
        String answer = aiManager.doChat(1679729045626982402L, "分析需求：\n"
                + "分析网站用户的增长情况\n"
                + "原始数据：\n"
                + "日期,用户数\n"
                + "1号,10\n"
                + "2号,20\n"
                + "3号,30");
        String[] aiResultSplit = answer.split(BiConstant.BI_RESULT_SEPARATOR);
        System.out.println(aiResultSplit[0]);
        System.out.println(aiResultSplit[1]);
        System.out.println(aiResultSplit[2]);
    }
}