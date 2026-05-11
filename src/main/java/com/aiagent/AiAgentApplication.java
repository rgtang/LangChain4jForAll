package com.aiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Java AI Agent 应用启动类
 *
 * 这是一个基于 LangChain4j 和 DeepSeek API 的 AI Agent 智能助手系统
 *
 * 主要功能：
 * 1. AI 流式聊天（SSE）
 * 2. 聊天记忆（多轮对话）
 * 3. Tool Calling（自动工具调用）
 * 4. RAG（知识库问答）
 * 5. Agent 智能决策
 *
 * 技术栈：
 * - JDK 8
 * - Spring Boot 2.7.x
 * - Spring WebFlux
 * - LangChain4j
 * - DeepSeek API
 *
 * @author AI Agent Team
 * @version 1.0.0
 */
@SpringBootApplication
public class AiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAgentApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  Java AI Agent 启动成功！");
        System.out.println("  访问地址：http://localhost:8080");
        System.out.println("========================================\n");
    }
}
