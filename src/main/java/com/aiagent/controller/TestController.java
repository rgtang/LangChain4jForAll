package com.aiagent.controller;

import com.aiagent.tools.WeatherTools;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 测试控制器
 * 用于测试工具调用是否正常工作
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private WeatherTools weatherTools;

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    /**
     * 直接测试天气工具
     */
    @GetMapping("/weather")
    public String testWeather(@RequestParam String city) {
        return weatherTools.getWeather(city);
    }

    /**
     * 测试同步模型的简单对话
     */
    @GetMapping("/chat")
    public String testChat(@RequestParam String message) {
        logger.info("测试同步聊天：{}", message);
        String response = chatLanguageModel.generate(message);
        logger.info("同步聊天响应：{}", response);
        return response;
    }
}
