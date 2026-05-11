package com.aiagent.service;

import dev.langchain4j.service.SystemMessage;

/**
 * 简单的 AI 助手接口（不使用工具调用）
 * 用于测试基本的对话功能
 */
public interface SimpleAssistant {

    @SystemMessage({
            "你是一个智能 AI 助手，名字叫小智。",
            "当用户询问天气时，请直接回答：'抱歉，天气查询功能暂时不可用，我们正在修复中。'",
            "其他问题请正常回答。"
    })
    String chat(String userMessage);
}
