package com.example.healthapp.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {SportRecord.class, User.class, DrinkRecord.class, FoodRecord.class,MealRecord.class,SleepRecord.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract SportRecordDao sportRecordDao();
    public abstract HealthDao healthDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "health_db"
            ).allowMainThreadQueries().build();
        }
        return INSTANCE;
    }
}
