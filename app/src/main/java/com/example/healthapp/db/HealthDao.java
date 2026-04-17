package com.example.healthapp.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HealthDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    /**
     * 根据用户名获取唯一用户
     * 注意：:name 必须与参数 String name 保持一致
     */
    @Query("SELECT * FROM users WHERE username = :name LIMIT 1")
    User getUserByUsername(String name);

    @Query("DELETE FROM users WHERE username = :name")
    void deleteUserByName(String name);

    @Query("SELECT * FROM users LIMIT 1")
    User getUser();

    @Update
    void updateUser(User user);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserById(int userId);

    @Query("DELETE FROM users WHERE id = 1")
    void deleteUserById();

    // --- 饮水记录相关 ---

    @Insert
    void insertDrink(DrinkRecord record);

    @Query("SELECT SUM(amount) FROM drink_records WHERE date = :date")
    int getTodayTotalDrink(String date);

    @Query("SELECT date, SUM(amount) as total FROM drink_records WHERE date BETWEEN :startDate AND :endDate GROUP BY date")
    List<DateValueEntity> getWeeklyDrink(String startDate, String endDate);

    // --- 饮食记录相关 ---

    @Insert
    void insertFood(FoodRecord record);

    @Query("SELECT SUM(calories) FROM food_records WHERE date >= :startTime AND date <= :endTime")
    double getTodayCaloriesSum(long startTime, long endTime);

    @Insert
    void insertMeal(MealRecord record);

    @Query("SELECT * FROM meal_records WHERE date = :date ORDER BY timestamp DESC")
    List<MealRecord> getMealsByDate(String date);

    @Query("SELECT SUM(calories) FROM meal_records WHERE date = :date")
    int getTodayTotalCalories(String date);

    @Delete
    void deleteMeal(MealRecord meal);

    // --- 运动与步数统计 ---

    @Query("SELECT date, SUM(calories) as total FROM sport_record WHERE date BETWEEN :start AND :end GROUP BY date ORDER BY date ASC")
    List<DateValueEntity> getWeeklyBurn(String start, String end);

    @Query("SELECT date, MAX(steps) as total FROM sport_record WHERE date BETWEEN :start AND :end GROUP BY date ORDER BY date ASC")
    List<DateValueEntity> getWeeklySteps(String start, String end);

    @Query("SELECT date, SUM(calories) as total FROM meal_records WHERE date BETWEEN :start AND :end GROUP BY date ORDER BY date ASC")
    List<DateValueEntity> getWeeklyCalories(String start, String end);

    // --- 睡眠记录相关 ---

    @Insert
    void insertSleep(SleepRecord record);

    @Query("SELECT * FROM sleep_records ORDER BY date DESC LIMIT 1")
    SleepRecord getLatestSleep();
}