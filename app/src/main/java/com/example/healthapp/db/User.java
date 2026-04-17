package com.example.healthapp.db; // 1. 必须有的包声明，对应你的文件夹路径

import androidx.room.Entity;      // 2. 导入 Room 相关的类
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String username;

    public String name;
    public int gender;
    public int age;
    public float height;
    public float weight;
    public int stepTarget; // 每日目标步数
}