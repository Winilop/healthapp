package com.example.healthapp.utils; // 必须放在第一行

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.healthapp.R; // 如果报错，请确保包名和你的项目一致

// ... 前面的 package 和 import 保持不变 ...

public class ReminderWorker extends Worker {

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // 【核心修改】从入参中读取数据，如果没有传则使用默认值
        String title = getInputData().getString("title");
        if (title == null) title = "健康提醒";

        String content = getInputData().getString("content");
        if (content == null) content = "记得查看今日健康目标哦！";

        // id 用于区分不同的通知（比如饮水和服药），防止互相覆盖
        int notificationId = getInputData().getInt("id", 1);

        showNotification(title, content, notificationId);
        return Result.success();
    }

    private void showNotification(String title, String content, int id) {
        String channelId = "health_reminders";
        NotificationManager manager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "健康任务提醒",
                    NotificationManager.IMPORTANCE_HIGH); // 设置为 HIGH 会有横幅弹出
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.drawable.outline_notifications_24)
                .setContentTitle(title)      // 使用动态标题
                .setContentText(content)     // 使用动态内容
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        manager.notify(id, builder.build()); // 使用动态 ID
    }
}