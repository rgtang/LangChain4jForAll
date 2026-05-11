package com.aiagent.service;

import com.aiagent.rag.RagService;
import dev.langchain4j.service.TokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AI 聊天服务
 * 整合 Assistant 和 RAG，实现智能对话
 *
 * 功能：
 * 1. 流式聊天（SSE）
 * 2. 多轮对话记忆
 * 3. 手动工具调用（因为 DeepSeek 的原生工具调用支持有限）
 * 4. RAG 知识库检索
 * 5. Agent 智能决策
 */
@Service
public class AiChatService {

    private static final Logger logger = LoggerFactory.getLogger(AiChatService.class);

    @Autowired
    private Assistant assistant;

    @Autowired
    private RagService ragService;

    @Autowired
    private ManualToolService manualToolService;

    /**
     * 流式聊天
     * 返回 Flux<String> 用于 SSE 输出
     *
     * @param userMessage 用户消息
     * @param sessionId 会话ID，用于隔离不同用户的上下文
     * @return 流式响应
     */
    public Flux<String> chatStream(String userMessage, String sessionId) {
        logger.info("收到用户消息，会话ID：{}，消息：{}", sessionId, userMessage);
        logger.debug("开始处理聊天请求，Assistant 类型：{}", assistant.getClass().getName());

        // 优先使用手动工具服务处理
        if (needsToolCall(userMessage)) {
            logger.info("检测到需要工具调用，使用手动工具服务");
            String response = manualToolService.processWithTools(userMessage);
            return Flux.just(response);
        }

        // Agent 智能决策：判断是否需要使用 RAG
        String finalMessage = userMessage;
        if (ragService.shouldUseRag(userMessage)) {
            logger.info("检测到知识库相关问题，启用 RAG");
            // 检索相关知识
            String context = ragService.retrieveRelevantKnowledge(userMessage, 3);
            // 构建增强提示词
            finalMessage = ragService.buildAugmentedPrompt(userMessage, context);
        }

        logger.debug("最终发送给 AI 的消息：{}", finalMessage);

        // 调用 Assistant 进行流式对话，传入会话ID
        TokenStream tokenStream = assistant.chat(sessionId, finalMessage);

        // 将 TokenStream 转换为 Flux<String>
        return Flux.create(sink -> {
            AtomicBoolean isComplete = new AtomicBoolean(false);

            tokenStream
                    .onNext(token -> {
                        if (!isComplete.get()) {
                            logger.trace("收到 token: {}", token);
                            sink.next(token);
                        }
                    })
                    .onComplete(response -> {
                        if (!isComplete.get()) {
                            isComplete.set(true);
                            logger.info("AI 响应完成，会话ID：{}，完整响应：{}", sessionId, response.content().text());
                            sink.complete();
                        }
                    })
                    .onError(error -> {
                        if (!isComplete.get()) {
                            isComplete.set(true);
                            logger.error("AI 响应错误，会话ID：{}", sessionId, error);
                            sink.error(error);
                        }
                    })
                    .start();
        });
    }

    /**
     * 判断是否需要工具调用
     */
    private boolean needsToolCall(String message) {
        String lower = message.toLowerCase();
        return lower.contains("天气") || lower.contains("气温") ||
               lower.contains("时间") || lower.contains("几点") ||
               lower.contains("日期") || lower.matches(".*\\d+\\s*[+\\-*/]\\s*\\d+.*");
    }
}
