package com.example.healthapp.utils;

/**
 * 运动方案生成工具类
 * 放在 utils 包下方便 DashboardFragment 随时调用
 */
public class PlanGenerator {

    /**
     * 根据 BMI 和 运动目标生成个性化建议
     * @param bmi 用户当前的 BMI 值
     * @param goal 运动目标（减脂、增肌、维持）
     * @return 格式化后的方案字符串
     */
    public static String generate(float bmi, String goal) {
        StringBuilder sb = new StringBuilder();

        // 1. 基于 BMI 的安全性风险提示
        if (bmi >= 28) {
            sb.append("【建议】当前 BMI 偏高，建议优先选择游泳、椭圆机等低冲击运动，避免长跑以保护膝关节。\n\n");
        } else if (bmi < 18.5) {
            sb.append("【建议】当前体重偏轻，建议以力量训练为主，并适当增加蛋白质摄入，减少高强度有氧。\n\n");
        }

        // 2. 根据目标输出具体方案
        sb.append("【方案】");
        switch (goal) {
            case "减脂":
                sb.append("每周 4 次有氧运动（如慢跑/跳绳）30-40 分钟，配合 1 次基础力量训练。");
                break;
            case "增肌":
                sb.append("每周 3 次重量训练（推、拉、蹲），每次 45 分钟，辅以少量有氧维持心肺。");
                break;
            case "维持":
            default:
                sb.append("每周 3 次 30 分钟中等强度运动（快走/瑜伽），保持规律作息。");
                break;
        }

        return sb.toString();
    }
}