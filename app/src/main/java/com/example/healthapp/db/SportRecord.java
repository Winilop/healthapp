package com.example.healthapp.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sport_record")
public class SportRecord {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String userId;

    public String date;       // yyyy-MM-dd
    public int steps;         // 今日步数
    public String type;       // 步行 / 跑步
    public int duration;      // 秒
    public float calories;  // 消耗卡路里
}
