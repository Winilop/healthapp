package com.example.healthapp.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "drink_records")
public class DrinkRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int amount;       // 饮水量，单位 ml
    public long timestamp;   // 记录时间戳
    public String date;      // 日期（方便后续按天统计，如 "2026-03-11"）
}