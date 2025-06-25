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

    //    assistant 变量会通过 Spring 自动注入得到 getAssistant() 方法返回的 Assistant 实例。
    @Autowired
    AiConfig.Assistant assistant;
    @Autowired
    AiConfig.Assistant1 assistant1;

    @Test
    void contextLoads1() throws InterruptedException {
        String chat1 = assistant.chat("我叫郭小芙");
        System.out.println(chat1);
        Thread.sleep(10000);
        System.out.println("****************************");
        chat1 = assistant.chat("我是谁");
        System.out.println(chat1);

    }

    @Test
    void contextLoads3() throws InterruptedException {
        String chat1 = assistant1.chat("1", "我叫郭小芙芙");
        System.out.println(chat1);
        Thread.sleep(10000);
        System.out.println("****************************");
        chat1 = assistant1.chat("3", "我是谁");
        System.out.println(chat1);
        Thread.sleep(10000);
        System.out.println("########################");
        chat1 = assistant1.chat("1", "我是谁");
        System.out.println(chat1);

    }
}