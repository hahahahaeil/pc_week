package com.shen;

import com.shen.config.AiConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Demo3ApplicationTests {

    @Test
    void contextLoads() {
    }
    @Autowired
    AiConfig.Assistant assistant;
    @Test
    void contextLoads1() throws InterruptedException {
        String chat1 = assistant.chat("我叫郭小芙");
        System.out.println(chat1);
        Thread.sleep(10000);
        System.out.println("****************************");
        chat1 = assistant.chat("我是谁");
        System.out.println(chat1);

    }
}
