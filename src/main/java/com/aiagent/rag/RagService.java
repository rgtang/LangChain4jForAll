package com.aiagent.rag;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 服务
 * 负责知识库检索和上下文增强
 *
 * 工作流程：
 * 1. 用户提问
 * 2. 将问题向量化
 * 3. 在向量数据库中检索相似内容
 * 4. 将检索结果作为上下文
 * 5. 拼接到 AI 提示词中
 * 6. AI 基于上下文回答
 */
@Service
public class RagService {

    private static final Logger logger = LoggerFactory.getLogger(RagService.class);

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 检索相关知识
     *
     * @param query 用户问题
     * @param maxResults 最大返回结果数
     * @return 相关知识文本
     */
    public String retrieveRelevantKnowledge(String query, int maxResults) {
        logger.debug("检索知识库，问题：{}", query);

        // 1. 将问题向量化
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        // 2. 在向量数据库中检索相似内容
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(
                queryEmbedding,
                maxResults,
                0.5  // 相似度阈值，0.5 表示至少 50% 相似
        );

        // 3. 提取文本内容
        String context = matches.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n"));

        logger.debug("检索到 {} 条相关知识", matches.size());

        return context;
    }

    /**
     * 构建增强提示词
     * 将检索到的知识拼接到用户问题中
     *
     * @param userQuestion 用户问题
     * @param context 检索到的上下文
     * @return 增强后的提示词
     */
    public String buildAugmentedPrompt(String userQuestion, String context) {
        if (context == null || context.trim().isEmpty()) {
            return userQuestion;
        }

        return "请基于以下知识库内容回答问题：\n\n" +
                "【知识库内容】\n" +
                context + "\n\n" +
                "【用户问题】\n" +
                userQuestion + "\n\n" +
                "请根据知识库内容准确回答，如果知识库中没有相关信息，请说明。";
    }

    /**
     * 判断问题是否需要查询知识库
     * 简单的关键词匹配
     *
     * @param question 用户问题
     * @return 是否需要查询知识库
     */
    public boolean shouldUseRag(String question) {
        // 知识库相关关键词
        String[] keywords = {
                "公司", "年假", "福利", "制度", "规定",
                "假期", "员工", "薪资", "工资", "待遇",
                "加班", "请假", "考勤", "社保", "公积金"
        };

        String lowerQuestion = question.toLowerCase();
        for (String keyword : keywords) {
            if (lowerQuestion.contains(keyword)) {
                return true;
            }
        }

        return false;
    }
}
