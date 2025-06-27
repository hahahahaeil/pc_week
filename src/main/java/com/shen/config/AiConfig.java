package com.shen.config;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByCharacterSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;
import java.util.List;

@Configuration
public class AiConfig {

    // 定义AI助手的公共接口
    public interface Assistant{
        // 普通聊天方法（阻塞式，返回完整响应）
        String chat(String message);
        // 流式响应方法（实时返回生成的token）
        TokenStream stream(String message);



        // 普通聊天方法（阻塞式，返回完整响应）带聊天记忆功能
        String chat(@MemoryId String memoryId, @UserMessage String message);
        // 流式响应方法（实时返回生成的token）带聊天记忆功能
        TokenStream stream(@MemoryId String memoryId, @UserMessage String message);
    }
    // 1. 注册 FileChatMemoryStore
    @Bean
    public ChatMemoryStore fileChatMemoryStore() {
        return new FileChatMemoryStore();
    }

    @Bean
    public EmbeddingStore embeddingStore() {
        return new InMemoryEmbeddingStore();
    }

    @Bean
    public QwenEmbeddingModel qwenEmbeddingModel() {
        // 使用建造者模式配置模型参数
        QwenEmbeddingModel embeddingModel = QwenEmbeddingModel.builder()
                .apiKey("sk-d57b75841af44c9aad2091c8a20f23ea")
                .build();  // 完成模型构建
        return embeddingModel;
    }

//    提前存储向量数据到向量数据库

    @Bean
    CommandLineRunner ingestTermOfServiceToVectorStore(QwenEmbeddingModel qwenEmbeddingModel, EmbeddingStore embeddingStore)
            throws URISyntaxException {
        Document document = ClassPathDocumentLoader.loadDocument("rag/rag-service.txt",new TextDocumentParser());
        return args->{
            DocumentByCharacterSplitter splitter = new DocumentByCharacterSplitter(200, 50);
            List<TextSegment> segments = splitter.split(document);
            // 4. 向量化所有文本片段
            List<Embedding> embeddings = qwenEmbeddingModel.embedAll(segments).content();
            embeddingStore.addAll(embeddings,segments);
        };

    }

    // 定义Spring Bean来创建助手实例
    @Bean  // 标记为Spring管理的Bean
    public Assistant getAssistant(// 注入标准聊天语言模型（用于普通响应）
                                  ChatLanguageModel chatLanguageModel,
                                  // 注入自定义的聊天记录持久化方案
                                  ChatMemoryStore chatMemoryStore,
                                  // 注入流式聊天语言模型（用于流式响应）
                                  StreamingChatLanguageModel streamingChatLanguageModel,
                                  EmbeddingStore embeddingStore,
                                  QwenEmbeddingModel qwenEmbeddingModel){

        // 内容检索器
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                .maxResults(5) // 最相似的5个结果
                .minScore(0.6) // 只找相似度在0.6以上的内容
                .build();

        // 使用AI服务构建助手实现
        Assistant assistant = AiServices.builder(Assistant.class)
                // 设置标准聊天模型（用于chat()方法）
                .chatLanguageModel(chatLanguageModel)
                // 内容检索器
                .contentRetriever(contentRetriever)
                // 设置流式聊天模型（用于stream()方法）
                .streamingChatLanguageModel(streamingChatLanguageModel)
//                // 配置聊天记忆保持对话上下文
//                .chatMemory(chatMemory)
                // 配置对话记忆提供者（为每个memoryId创建独立的记忆窗口）
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.builder()
                                .maxMessages(10)
                                .id(memoryId)
                                // 使用自定义的持久化方案
                                .chatMemoryStore(chatMemoryStore)
                                .build()
                )
                // 完成构建
                .build();
        // 返回配置好的助手实例
        return assistant;
    }

}
