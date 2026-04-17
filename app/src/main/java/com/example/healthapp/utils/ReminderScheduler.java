package com.example.healthapp.utils;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import android.content.Context;
import java.util.concurrent.TimeUnit;

public class ReminderScheduler {

    /**
     * 排程一个提醒任务
     * @param tag 任务标签 (如 "water_task")
     * @param delayMinutes 延迟多少分钟后触发
     * @param title 通知标题
     * @param content 通知内容
     */
    public static void scheduleReminder(Context context, String tag, long delayMinutes, String title, String content, int id) {

        // 将数据打包传给 Worker
        Data inputData = new Data.Builder()
                .putString("title", title)
                .putString("content", content)
                .putInt("id", id)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES) // 设置延迟
                .addTag(tag)
                .setInputData(inputData)
                .build();

        // 使用 REPLACE 模式：如果旧任务还没执行，新任务会替换掉它
        WorkManager.getInstance(context).enqueueUniqueWork(
                tag,
                ExistingWorkPolicy.REPLACE,
                request
        );
    }
}