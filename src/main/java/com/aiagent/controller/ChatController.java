package com.aiagent.controller;

import com.aiagent.service.AiChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * 聊天控制器
 * 提供 RESTful API 接口
 *
 * 主要接口：
 * 1. POST /api/chat/stream - 流式聊天（SSE）
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")  // 允许跨域，方便前端调用
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private AiChatService aiChatService;

    /**
     * 流式聊天接口
     * 使用 SSE（Server-Sent Events）实现流式输出
     *
     * @param request 聊天请求（包含消息和会话ID）
     * @return 流式响应
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        String sessionId = request.getSessionId() != null ? request.getSessionId() : "default";
        logger.info("接收到聊天请求，会话ID：{}，消息：{}", sessionId, request.getMessage());

        return aiChatService.chatStream(request.getMessage(), sessionId)
                .doOnComplete(() -> logger.info("流式响应完成，会话ID：{}", sessionId))
                .doOnError(error -> logger.error("流式响应错误，会话ID：{}", sessionId, error));
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    /**
     * 聊天请求对象
     */
    public static class ChatRequest {
        private String message;
        private String sessionId;

        public ChatRequest() {
        }

        public ChatRequest(String message) {
            this.message = message;
        }

        public ChatRequest(String message, String sessionId) {
            this.message = message;
            this.sessionId = sessionId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
    }
}
