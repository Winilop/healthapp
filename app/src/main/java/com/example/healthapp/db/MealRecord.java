package com.example.healthapp.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "meal_records")
public class MealRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String foodName;   // 食物名称
    public int calories;      // 热量 (kcal)
    public String mealType;   // 类型：早餐/午餐/晚餐/其他
    public String date;       // 日期：yyyy-MM-dd
    public long timestamp;    // 用于排序的具体时间
}