package com.shen.controller;

import com.shen.config.AiConfig;
import dev.langchain4j.service.TokenStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class RAGController {

    @Autowired
    AiConfig.Assistant assistant;

    @RequestMapping(value = "/test",produces = "text/stream;charset=utf-8")
//    从 URL 请求参数中获取 message，返回类型是一个字符串流
    public Flux<String> test(@RequestParam String message) {

        final TokenStream stream = assistant.stream(message);
        // 启动流处理
//        这行作用是打印 TokenStream 对象的引用地址或描述，主要用于调试，不是必要的逻辑。

        System.out.println(stream);
//      一边生成，一边输出给前端
        return Flux.create(sink -> {
//            一段段发给前端
            stream.onPartialResponse(sink::next)
//                    告诉前端结束
                    .onCompleteResponse(s -> sink.complete())
//                    报错处理
                    .onError(sink::error)
                    .start(); // 启动流处理
        });

    }


}
