package com.shen.controller;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@RestController
//@RequestMapping("/chat")
public class StreamingChatController {

    @Autowired
    private StreamingChatLanguageModel streamingChatLanguageModel;

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam String message) {

        return Flux.create(sink -> {
            streamingChatLanguageModel.chat(message, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    // 每次响应部分结果，就推送给浏览器
                    sink.next(partialResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    sink.complete(); // 推送结束
                }

                @Override
                public void onError(Throwable error) {
                    sink.error(error); // 异常处理
                }
            });
        }, FluxSink.OverflowStrategy.BUFFER);
    }
}
