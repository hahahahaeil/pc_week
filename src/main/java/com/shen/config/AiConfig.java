package com.shen.config;


import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    // 定义AI助手的公共接口
    public interface Assistant{
        // 普通聊天方法（阻塞式，返回完整响应）
        String chat(String message);

        // 流式响应方法（实时返回生成的token）
        TokenStream stream(String message);
    }

    // 定义Spring Bean来创建助手实例
    @Bean  // 标记为Spring管理的Bean
    public Assistant getAssistant(// 注入标准聊天语言模型（用于普通响应）
                                  ChatLanguageModel chatLanguageModel,
                                  // 注入流式聊天语言模型（用于流式响应）
                                  StreamingChatLanguageModel streamingChatLanguageModel){
        // 创建聊天记忆，保留最近10条消息的对话上下文
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        // 使用AI服务构建助手实现
        Assistant assistant = AiServices.builder(Assistant.class)
                // 设置标准聊天模型（用于chat()方法）
                .chatLanguageModel(chatLanguageModel)
                // 设置流式聊天模型（用于stream()方法）
                .streamingChatLanguageModel(streamingChatLanguageModel)
                // 配置聊天记忆保持对话上下文
                .chatMemory(chatMemory)
                // 完成构建
                .build();
        // 返回配置好的助手实例
        return assistant;
    }

}
