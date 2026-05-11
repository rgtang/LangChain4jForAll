package com.aiagent.config;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.aiagent.service.Assistant;
import com.aiagent.tools.CalculatorTools;
import com.aiagent.tools.TimeTools;
import com.aiagent.tools.WeatherTools;

import java.time.Duration;

/**
 * AI 配置类
 * 负责配置 LangChain4j 的核心组件：
 * 1. ChatLanguageModel - 同步聊天模型
 * 2. StreamingChatLanguageModel - 流式聊天模型（用于 SSE）
 * 3. ChatMemoryStore - 聊天记忆存储（支持多用户隔离）
 * 4. Assistant - AI 助手服务（集成 Tools 和 Memory）
 */
@Configuration
public class AiConfig {

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.base-url}")
    private String baseUrl;

    @Value("${ai.model}")
    private String modelName;

    @Value("${ai.temperature:0.7}")
    private Double temperature;

    @Value("${ai.max-tokens:2000}")
    private Integer maxTokens;

    @Value("${ai.timeout:60}")
    private Integer timeout;

    /**
     * 配置同步聊天模型
     * 用于普通的请求-响应模式
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(timeout))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    /**
     * 配置流式聊天模型
     * 用于 SSE（Server-Sent Events）流式输出
     * 实现类似 ChatGPT 的打字机效果
     */
    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(timeout))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    /**
     * 配置聊天记忆存储
     * 使用内存存储，支持多用户隔离
     * 每个用户/会话有独立的 memoryId，互不干扰
     *
     * 注意：InMemoryChatMemoryStore 是内存存储，重启后数据会丢失
     * 生产环境建议使用持久化存储（如 Redis、数据库等）
     */
    @Bean
    public ChatMemoryStore chatMemoryStore() {
        return new InMemoryChatMemoryStore();
    }

    /**
     * 配置 AI 助手服务
     * 集成了：
     * 1. 同步聊天模型（用于工具调用）
     * 2. 流式聊天模型（用于流式输出）
     * 3. 聊天记忆（多轮对话，支持多用户隔离）
     * 4. Tools（工具调用能力）
     *
     * 注意：使用 ChatMemoryProvider 自动为每个 memoryId 创建独立的 ChatMemory
     * - 同时配置两个模型，LangChain4j 会智能选择：
     *   - 需要工具调用时使用同步模型
     *   - 普通对话时使用流式模型
     */
    @Bean
    public Assistant assistant(ChatLanguageModel chatLanguageModel,
                               StreamingChatLanguageModel streamingChatLanguageModel,
                               ChatMemoryStore chatMemoryStore,
                               WeatherTools weatherTools,
                               TimeTools timeTools,
                               CalculatorTools calculatorTools) {
        return AiServices.builder(Assistant.class)
                .chatLanguageModel(chatLanguageModel)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(10)
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                .tools(weatherTools, timeTools, calculatorTools)
                .build();
    }
}
