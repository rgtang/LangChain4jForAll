package com.aiagent.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

/**
 * 计算器工具
 * 使用 @Tool 注解标记，AI 可以自动调用
 *
 * 当用户询问数学计算问题时，AI 会自动调用此工具
 * 例如："123 加 456 等于多少"、"计算 10 的平方"
 */
@Component
public class CalculatorTools {

    /**
     * 加法运算
     *
     * @param a 第一个数
     * @param b 第二个数
     * @return 计算结果
     */
    @Tool("计算两个数的和")
    public double add(double a, double b) {
        return a + b;
    }

    /**
     * 减法运算
     *
     * @param a 被减数
     * @param b 减数
     * @return 计算结果
     */
    @Tool("计算两个数的差")
    public double subtract(double a, double b) {
        return a - b;
    }

    /**
     * 乘法运算
     *
     * @param a 第一个数
     * @param b 第二个数
     * @return 计算结果
     */
    @Tool("计算两个数的乘积")
    public double multiply(double a, double b) {
        return a * b;
    }

    /**
     * 除法运算
     *
     * @param a 被除数
     * @param b 除数
     * @return 计算结果
     */
    @Tool("计算两个数的商")
    public double divide(double a, double b) {
        if (b == 0) {
            throw new IllegalArgumentException("除数不能为零");
        }
        return a / b;
    }

    /**
     * 幂运算
     *
     * @param base 底数
     * @param exponent 指数
     * @return 计算结果
     */
    @Tool("计算一个数的幂次方")
    public double power(double base, double exponent) {
        return Math.pow(base, exponent);
    }

    /**
     * 平方根运算
     *
     * @param number 数字
     * @return 计算结果
     */
    @Tool("计算一个数的平方根")
    public double squareRoot(double number) {
        if (number < 0) {
            throw new IllegalArgumentException("不能计算负数的平方根");
        }
        return Math.sqrt(number);
    }
}
