package com.example.healthapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class GoalManager {
    private static final String PREF_NAME = "health_goals";

    // 获取步数目标，默认为 6000
    public static int getStepGoal(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt("step_goal", 6000);
    }

    // 获取饮水目标，默认为 2000
    public static int getDrinkGoal(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt("drink_goal", 2000);
    }

    // 保存新目标
    public static void setGoals(Context context, int stepGoal, int drinkGoal) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt("step_goal", stepGoal);
        editor.putInt("drink_goal", drinkGoal);
        editor.apply();
    }
}