package com.shen;

import com.shen.config.AiConfig;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.List;

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

    @Test
    public void test31() {
        // ------------------------------------------ 向量嵌入（Embedding）阶段 ------------------------------------------
        // 创建一个内存中的向量存储，用于存储文本片段及其对应的向量
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        // 使用建造者模式配置模型参数
        QwenEmbeddingModel embeddingModel = QwenEmbeddingModel.builder()
                .apiKey("sk-d57b75841af44c9aad2091c8a20f23ea")
                .build(); // 完成模型构建
        // 存入向量数据库的第一个文本片段（关于航班预订的条款）
        TextSegment segment1 = TextSegment.from("""
                 预订酒店:
                - 通过我们的官方网站、移动应用程序或授权合作伙伴平台完成预订。
                - 预订时需要全额付款。
                - 请确保入住人信息（姓名、联系方式、入住日期、房型等）准确。
                """);
        // 将文本片段转换为向量并存储到向量数据库
        embeddingStore.add(embeddingModel.embed(segment1).content(), segment1);
        // 存入向量数据库的第二个文本片段（关于取消预订的条款）
        TextSegment segment2 = TextSegment.from("""
                 取消预订:
                - 最晚在入住日前 6 小时 取消以避免罚金。
                 - 取消费用：经济房型：首晚房费的60%，豪华房型：首晚房费的40%，套房/别墅：首晚房费
                的20%。
                 - 退款将在 5-10个工作日 原路退回。
                """);
        // 将文本片段转换为向量并存储到向量数据库
        embeddingStore.add(embeddingModel.embed(segment2).content(), segment2);
        // ---------------------- 数据检索阶段 ----------------------
        // 将用户的查询语句"取消航班要多少钱"转换为向量
        Embedding queryEmbedding = embeddingModel.embed("取消预订要多少钱").content();
        // 构建向量搜索请求
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding) // 设置查询向量
                .maxResults(2) // 设置最大返回结果数为1
                .minScore(0.6) // 设置最小相似度阈值为0.6
                .build();
        // 执行向量搜索
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);
        // 处理搜索结果
        if (result.matches().isEmpty()) {
            // 如果没有找到匹配结果
            System.out.println("未找到匹配结果！");
        } else {
            // 遍历所有匹配结果（这里由于maxResults=1，只会有一个结果）
            result.matches().forEach(match -> {
                System.out.println("相似度: " + match.score()); // 打印匹配相似度分数
                System.out.println("匹配内容: " + match.embedded().text()); // 打印匹配的文本内容
            });
        }
    }

    // 文本转向量
    @Test
    void contextLoads() {
        QwenEmbeddingModel model = QwenEmbeddingModel.builder()
                .apiKey("sk-aee5d2d760b342639dd31256d4f66587")
                .build(); // 完成模型构建
// 2. 调用模型进行文本嵌入
// 对输入文本"我是小红"生成嵌入向量
        Response<Embedding> embed = model.embed("我是阿杜");
// 3. 打印嵌入结果
// 输出嵌入结果的元信息（如token用量等）
        System.out.println(embed.content().toString());
// 4. 输出嵌入向量的维度
// 打印嵌入向量的长度（维度大小）
        System.out.println(embed.content().vector().length);
    }

    // 文档解析器
    @Test
    public void test4() {
        Document document = ClassPathDocumentLoader.loadDocument("rag/rag-service.txt", new TextDocumentParser());
        System.out.println(document.text());
    }

    // 文档拆分器
    @Test
    public void test5() {
        Document document = ClassPathDocumentLoader.loadDocument("rag/rag-service.txt", new TextDocumentParser());
        System.out.println(document.text());
        DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(
                50, 10
        );
        // 实际上我感觉langchain4j 的这个分割器还有待完善，DocumentByParagraphSplitter 看样子是按段落分割，但实际情况下还和这个参数 maxSegmentSize 有关系
        // 查看源码是，只要不超过maxSegmentSize，就不会分割
        // 我认为的这个maxSegmentSize的作用是，在按照段落分完之后，如果某个段落的超过了这个值的。那么该段落在继续分割。
        List<TextSegment> segments = splitter.split(document);
        System.out.println(segments);
    }


    @Test
    public void test6() {
        Document document = ClassPathDocumentLoader.loadDocument("rag/rag-service.txt", new TextDocumentParser());
        System.out.println(document.text());

        DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(
                50, 10
        );

        // 实际上我感觉langchain4j 的这个分割器还有待完善，DocumentByParagraphSplitter 看样子是按段落分割，但实际情况下还和这个参数 maxSegmentSize 有关系
        // 查看源码是，只要不超过maxSegmentSize，就不会分割
        // 我认为的这个maxSegmentSize的作用是，在按照段落分完之后，如果某个段落的超过了这个值的。那么该段落在继续分割。
        List<TextSegment> segments = splitter.split(document);
        System.out.println(segments);

        // 使用建造者模式配置模型参数
        QwenEmbeddingModel embeddingModel = QwenEmbeddingModel.builder()
                .apiKey("sk-aee5d2d760b342639dd31256d4f66587")
                .build();  // 完成模型构建

        List<Embedding> content = embeddingModel.embedAll(segments).content();
        System.out.println(content);

        // 创建一个内存中的向量存储，用于存储文本片段及其对应的向量
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        // 向量存储  // 正确方式：同时添加向量和对应的文本片段
        embeddingStore.addAll(content, segments);
//        embeddingStore.addAll(content);

        // ============向量检索阶段===============
        // 将用户的查询语句"取消航班要多少钱"转换为向量
        Embedding queryEmbedding = embeddingModel.embed("取消预订要多少钱").content();
        // 构建向量搜索请求
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)  // 设置查询向量
                .maxResults(2)                   // 设置最大返回结果数为2
                .minScore(0.6)                   // 设置最小相似度阈值为0.6
                .build();

        // 执行向量搜索
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);

        // 处理搜索结果
        if (result.matches().isEmpty()) {
            // 如果没有找到匹配结果
            System.out.println("未找到匹配结果！");
        } else {
            // 遍历所有匹配结果（这里由于maxResults=1，只会有一个结果）
            result.matches().forEach(match -> {
                System.out.println("相似度: " + match.score());             // 打印匹配相似度分数
                System.out.println("匹配内容: " + match.embedded().text()); // 打印匹配的文本内容
            });
        }

    }
}