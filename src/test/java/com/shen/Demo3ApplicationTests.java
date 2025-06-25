package com.shen;

import com.shen.config.AiConfig;
import dev.langchain4j.service.TokenStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

@SpringBootTest
class Demo3ApplicationTests {
    @Autowired
    AiConfig.Assistant assistant;

    @Test
    void contextLoads1() throws InterruptedException {
        String chat1 = assistant.chat("我叫郭小芙芙");
        System.out.println(chat1);

    }



    @Test
    void contextLoads2() throws InterruptedException {
        String chat1 = assistant.chat("我是谁");
        System.out.println(chat1);

    }

}