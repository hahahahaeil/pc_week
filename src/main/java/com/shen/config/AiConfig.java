package com.shen.config;


import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
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
    // 方法，获得一个Assistant
    @Bean  // 标记为Spring管理的Bean
    public Assistant getAssistant(// 注入标准聊天语言模型（用于普通响应）
                                  ChatLanguageModel chatLanguageModel,
                                  // 注入流式聊天语言模型（用于流式响应）
                                  StreamingChatLanguageModel streamingChatLanguageModel){
        // 创建聊天记忆，保留最近10条消息的对话上下文
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        // 使用AI服务构建助手实现，动态代理实现技术生成了Assistant接口的实现，
//        三个配置，两个聊天模型，一个上下文记忆
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
// 记忆分离

    // 定义AI助手的公共接口
    public interface Assistant1{
        // 普通聊天方法（阻塞式，返回完整响应）
        String chat(String message);
        // 流式响应方法（实时返回生成的token）
        TokenStream stream(String message);

        // 普通聊天方法（阻塞式，返回完整响应）带聊天记忆功能
        String chat(@MemoryId String memoryId, @UserMessage String message);
        // 流式响应方法（实时返回生成的token）带聊天记忆功能
        TokenStream stream(@MemoryId String memoryId, @UserMessage String message);
    }

    // 定义Spring Bean来创建助手实例
    @Bean  // 标记为Spring管理的Bean
    public Assistant1 getAssistant1(// 注入标准聊天语言模型（用于普通响应）
                                  ChatLanguageModel chatLanguageModel,
// 注入流式聊天语言模型（用于流式响应）
                                  StreamingChatLanguageModel streamingChatLanguageModel){
        System.out.println("******构造出一个Assistant1对象"+chatLanguageModel.getClass());
// 创建聊天记忆，保留最近10条消息的对话上下文
//        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
// 使用AI服务构建助手实现
        // 使用AI服务构建助手实现
        Assistant1 assistant1 = AiServices.builder(Assistant1.class)
// 设置标准聊天模型（用于chat()方法）
                .chatLanguageModel(chatLanguageModel)
// 设置流式聊天模型（用于stream()方法）
                .streamingChatLanguageModel(streamingChatLanguageModel)
//                // 配置聊天记忆保持对话上下文
//                .chatMemory(chatMemory)
                // 配置对话记忆提供者（为每个memoryId创建独立的记忆窗口）
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.builder().maxMessages(10).id(memoryId).build()
                )
// 完成构建
                .build();
        return assistant1;
    }


}
