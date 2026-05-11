package com.aiagent.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * RAG（检索增强生成）配置类
 * 负责配置知识库相关组件：
 * 1. EmbeddingModel - 文本向量化模型
 * 2. EmbeddingStore - 向量存储
 * 3. DocumentSplitter - 文档切片器
 * 4. 加载并处理知识库文档
 */
@Configuration
public class RagConfig {

    private static final Logger logger = LoggerFactory.getLogger(RagConfig.class);

    /**
     * 配置 Embedding 模型
     * 使用本地 all-MiniLM-L6-v2 模型
     * 该模型轻量且高效，适合 JDK8 环境
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    /**
     * 配置向量存储
     * 使用内存存储，简单高效
     * 生产环境可替换为 Pinecone、Milvus 等
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(EmbeddingModel embeddingModel) {
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        // 加载知识库文档
        try {
            loadKnowledgeBase(embeddingStore, embeddingModel);
        } catch (Exception e) {
            logger.error("加载知识库失败", e);
        }

        return embeddingStore;
    }

    /**
     * 配置文档切片器
     * 将长文档切分为小片段，便于检索
     */
    @Bean
    public DocumentSplitter documentSplitter() {
        return DocumentSplitters.recursive(300, 50);
    }

    /**
     * 加载知识库文档
     * 从 resources/docs 目录加载所有 txt 文件
     * 并进行向量化存储
     */
    private void loadKnowledgeBase(EmbeddingStore<TextSegment> embeddingStore,
                                   EmbeddingModel embeddingModel) throws IOException {
        logger.info("开始加载知识库文档...");

        // 读取 company.txt 文件
        ClassPathResource resource = new ClassPathResource("docs/company.txt");
        Path path = Paths.get(resource.getURI());
        String content = new String(Files.readAllBytes(path), "UTF-8");

        // 创建文档对象
        Document document = Document.from(content);

        // 文档切片
        DocumentSplitter splitter = documentSplitter();
        List<TextSegment> segments = splitter.split(document);

        logger.info("文档切片完成，共 {} 个片段", segments.size());

        // 向量化并存储
        for (TextSegment segment : segments) {
            embeddingStore.add(embeddingModel.embed(segment).content(), segment);
        }

        logger.info("知识库加载完成");
    }
}
