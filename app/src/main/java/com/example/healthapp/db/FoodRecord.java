package com.example.healthapp.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "food_records")
public class FoodRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String foodName;
    public double calories;
    public String mealType; // 早餐/午餐/晚餐
    public long date;       // 日期时间戳
}