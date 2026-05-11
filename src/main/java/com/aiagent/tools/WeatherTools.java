package com.aiagent.tools;

import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 天气查询工具
 * 使用 @Tool 注解标记，AI 可以自动调用
 *
 * 当用户询问天气相关问题时，AI 会自动调用此工具
 * 例如："北京天气怎么样"、"上海今天下雨吗"
 */
@Component
public class WeatherTools {

    private static final Logger logger = LoggerFactory.getLogger(WeatherTools.class);

    /**
     * 查询指定城市的天气
     *
     * @param city 城市名称
     * @return 天气信息
     */
    @Tool("查询指定城市的天气情况，返回温度、天气状况等信息")
    public String getWeather(String city) {
        logger.info("天气工具被调用！城市：{}", city);

        // 这里是模拟数据，实际应该调用真实的天气 API
        // 例如：和风天气、OpenWeatherMap 等

        // 模拟不同城市的天气数据
        String result;
        if (city.contains("北京")) {
            result = "北京今天天气晴朗，温度 15-25℃，空气质量良好，适合户外活动。";
        } else if (city.contains("上海")) {
            result = "上海今天多云转阴，温度 18-23℃，可能有小雨，建议携带雨具。";
        } else if (city.contains("深圳")) {
            result = "深圳今天晴转多云，温度 22-28℃，湿度较大，体感较热。";
        } else if (city.contains("广州")) {
            result = "广州今天阴有小雨，温度 20-26℃，湿度 80%，注意防雨。";
        } else {
            result = city + " 今天天气晴朗，温度适宜，是个好天气！";
        }

        logger.info("天气工具返回结果：{}", result);
        return result;
    }

    /**
     * 查询未来几天的天气预报
     *
     * @param city 城市名称
     * @param days 预报天数
     * @return 天气预报信息
     */
    @Tool("查询指定城市未来几天的天气预报")
    public String getWeatherForecast(String city, int days) {
        return String.format("%s 未来 %d 天天气预报：\n" +
                "明天：晴，15-25℃\n" +
                "后天：多云，16-24℃\n" +
                "第三天：小雨，14-22℃", city, days);
    }
}
