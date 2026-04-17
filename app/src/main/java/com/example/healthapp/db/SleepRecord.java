package com.example.healthapp.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sleep_records")
public class SleepRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String date;           // 日期 yyyy-MM-dd
    public String startTime;      // 入睡时间 HH:mm
    public String endTime;        // 起床时间 HH:mm
    public float totalHours;      // 总时长
    public float deepSleepHours;  // 深睡时长
    public int wakeCount;         // 醒来次数
    public int score;             // 睡眠评分
}