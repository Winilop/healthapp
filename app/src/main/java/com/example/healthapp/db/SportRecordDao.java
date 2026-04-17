package com.example.healthapp.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SportRecordDao {

    // 使用 REPLACE 策略，确保同一天的数据更新时会覆盖旧记录，而不是重复插入
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SportRecord record);

    // 注意：表名必须与 SportRecord 实体类中的 tableName="sport_records" 保持一致
    @Query("SELECT * FROM sport_record WHERE date = :date LIMIT 1")
    SportRecord getRecordByDate(String date);

    // 获取所有记录，按 ID 倒序排列（最新的在前面）
    @Query("SELECT * FROM sport_record ORDER BY id DESC")
    List<SportRecord> getAllRecords();

    /**
     * 新增：查询某一天所有的运动卡路里总和
     * 这个方法会被 DashboardFragment 的 loadHealthData 调用
     */
    @Query("SELECT SUM(calories) FROM sport_record WHERE date = :date")
    int getTodayTotalCalories(String date);

    @Delete
    void delete(SportRecord record);
    @Query("DELETE FROM sport_record WHERE date = :today")
    void deleteTodayRecord(String today);
    class CaloriesStat {
        public String date;
        public float totalCalories;
    }

    // 查询最近 7 天的消耗总和
    @Query("SELECT date, SUM(calories) as totalCalories FROM sport_record " +
            "GROUP BY date ORDER BY date ASC LIMIT 7")
    List<CaloriesStat> getWeeklyCaloriesStats();
}