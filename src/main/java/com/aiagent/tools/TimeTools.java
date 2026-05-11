package com.aiagent.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间查询工具
 * 使用 @Tool 注解标记，AI 可以自动调用
 *
 * 当用户询问时间相关问题时，AI 会自动调用此工具
 * 例如："现在几点了"、"今天是几号"
 */
@Component
public class TimeTools {

    /**
     * 获取当前时间
     *
     * @return 当前时间字符串
     */
    @Tool("获取当前的日期和时间")
    public String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");
        return "当前时间是：" + now.format(formatter);
    }

    /**
     * 获取当前日期
     *
     * @return 当前日期字符串
     */
    @Tool("获取今天的日期")
    public String getCurrentDate() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        String weekDay = getWeekDay(now.getDayOfWeek().getValue());
        return "今天是：" + now.format(formatter) + " " + weekDay;
    }

    /**
     * 获取星期几
     *
     * @param dayOfWeek 星期数字（1-7）
     * @return 星期字符串
     */
    private String getWeekDay(int dayOfWeek) {
        String[] weekDays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        return weekDays[dayOfWeek - 1];
    }
}
