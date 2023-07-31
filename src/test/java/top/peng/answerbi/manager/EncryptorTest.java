package top.peng.answerbi.manager;

import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * AiManagerTest
 *
 * @author yunpeng
 * @version 1.0 2023/7/14
 */
@SpringBootTest
class EncryptorTest {

    @Autowired
    private StringEncryptor stringEncryptor;

    @Test
    void encode() {
        String password = "123456";

        System.out.println( "加密密文：" + stringEncryptor.encrypt(password));
        System.out.println("解密密文：" + stringEncryptor.decrypt(stringEncryptor.encrypt(password)));
    }
}