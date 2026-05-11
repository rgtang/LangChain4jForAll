package com.aiagent.service;

import com.aiagent.tools.CalculatorTools;
import com.aiagent.tools.TimeTools;
import com.aiagent.tools.WeatherTools;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 手动工具调用服务
 * 当 LLM 的原生工具调用不可用时，使用此服务手动解析和执行工具
 */
@Service
public class ManualToolService {

    private static final Logger logger = LoggerFactory.getLogger(ManualToolService.class);

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Autowired
    private WeatherTools weatherTools;

    @Autowired
    private TimeTools timeTools;

    @Autowired
    private CalculatorTools calculatorTools;

    /**
     * 处理用户消息，检测是否需要调用工具
     */
    public String processWithTools(String userMessage) {
        logger.info("手动工具服务处理消息：{}", userMessage);

        // 检测天气查询
        if (isWeatherQuery(userMessage)) {
            String city = extractCity(userMessage);
            logger.info("检测到天气查询，城市：{}", city);
            String weatherInfo = weatherTools.getWeather(city);

            // 让 AI 用自然语言包装结果
            String prompt = String.format(
                "用户问：%s\n天气信息：%s\n请用友好的语气回答用户。",
                userMessage, weatherInfo
            );
            return chatLanguageModel.generate(prompt);
        }

        // 检测时间查询
        if (isTimeQuery(userMessage)) {
            logger.info("检测到时间查询");
            String timeInfo = timeTools.getCurrentTime();

            String prompt = String.format(
                "用户问：%s\n当前时间：%s\n请用友好的语气回答用户。",
                userMessage, timeInfo
            );
            return chatLanguageModel.generate(prompt);
        }

        // 检测计算请求
        if (isCalculationQuery(userMessage)) {
            logger.info("检测到计算请求");
            try {
                // 简单的数学表达式提取
                Pattern pattern = Pattern.compile("(\\d+)\\s*([+\\-*/])\\s*(\\d+)");
                Matcher matcher = pattern.matcher(userMessage);

                if (matcher.find()) {
                    double a = Double.parseDouble(matcher.group(1));
                    String op = matcher.group(2);
                    double b = Double.parseDouble(matcher.group(3));

                    double result = 0;
                    switch (op) {
                        case "+": result = calculatorTools.add(a, b); break;
                        case "-": result = calculatorTools.subtract(a, b); break;
                        case "*": result = calculatorTools.multiply(a, b); break;
                        case "/": result = calculatorTools.divide(a, b); break;
                    }

                    String prompt = String.format(
                        "用户问：%s\n计算结果：%s\n请用友好的语气回答用户。",
                        userMessage, result
                    );
                    return chatLanguageModel.generate(prompt);
                }
            } catch (Exception e) {
                logger.error("计算失败", e);
            }
        }

        // 普通对话
        return chatLanguageModel.generate(userMessage);
    }

    private boolean isWeatherQuery(String message) {
        String lower = message.toLowerCase();
        return lower.contains("天气") || lower.contains("气温") ||
               lower.contains("下雨") || lower.contains("晴天");
    }

    private boolean isTimeQuery(String message) {
        String lower = message.toLowerCase();
        return lower.contains("时间") || lower.contains("几点") ||
               lower.contains("日期") || lower.contains("今天");
    }

    private boolean isCalculationQuery(String message) {
        return message.matches(".*\\d+\\s*[+\\-*/]\\s*\\d+.*");
    }

    private String extractCity(String message) {
        // 简单的城市提取逻辑
        String[] cities = {"北京", "上海", "广州", "深圳", "南京", "杭州", "成都", "重庆"};
        for (String city : cities) {
            if (message.contains(city)) {
                return city;
            }
        }
        return "未知城市";
    }
}
