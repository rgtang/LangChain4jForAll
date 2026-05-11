package com.aiagent.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

/**
 * AI 助手服务接口
 * 使用 LangChain4j 的 AiServices 自动实现
 *
 * 集成了：
 * 1. 流式聊天模型（SSE）
 * 2. 聊天记忆（多轮对话）- 支持多用户隔离
 * 3. Tools（自动工具调用）
 */
public interface Assistant {

    /**
     * 系统提示词
     * 定义 AI 助手的角色和行为
     */
    @SystemMessage({
            "你是一个智能 AI 助手，名字叫小智。",
            "你可以：",
            "1. 回答各种问题",
            "2. 查询天气信息",
            "3. 查询时间日期",
            "4. 进行数学计算",
            "5. 回答公司相关问题（基于知识库）",
            "",
            "当用户询问天气、时间、计算等问题时，你会自动调用相应的工具。",
            "当用户询问公司相关问题时，请基于提供的知识库内容准确回答。",
            "请用友好、专业的语气回答问题。"
    })
    TokenStream chat(@MemoryId String memoryId, @UserMessage String userMessage);
}
