package com.example.healthapp.db;

public class DateValueEntity {
    public String date;
    public int total;

    // 1. 必须保留一个无参构造函数供 Room 使用
    public DateValueEntity() {
    }

    // 2. 这是你方便自己使用的构造函数
    public DateValueEntity(String date, int total) {
        this.date = date;
        this.total = total;
    }
}