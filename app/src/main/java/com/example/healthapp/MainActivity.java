package com.example.healthapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.healthapp.fragment.DashboardFragment;
import com.example.healthapp.fragment.ProfileFragment;
import com.example.healthapp.fragment.RecordFragment;
import com.example.healthapp.utils.ReminderWorker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.healthapp.fragment.ReportFragment;

import java.util.concurrent.TimeUnit;
// 注意：如果你的包名不是 com.example.healthapp，请根据实际情况修改

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ACTIVITY_RECOGNITION = 1001;

    private SharedPreferences preferences;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1️⃣ 检查登录状态
        checkLoginStatus();

        // 2️⃣ 请求计步权限（Android 10+）
        requestActivityRecognitionPermission();

        // 3️⃣ 初始化底部导航
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 4️⃣ 默认显示 DashboardFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
        }

        // 5️⃣ 底部导航切换 Fragment
        bottomNavigationView.setOnItemSelectedListener(item -> {

            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                fragment = new DashboardFragment();
            } else if (id == R.id.nav_profile) {
                fragment = new ProfileFragment();
            } else if (id == R.id.nav_diet) {
                fragment = new RecordFragment();
            } else if (id == R.id.navigation_report) { // 关键：添加这一段
                fragment = new ReportFragment();      // 跳转到你的报告页面
            }

            if (fragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
                return true;
            }

            return false;
        });
    }

    // 🔐 登录检查
    private void checkLoginStatus() {
        preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int userId = preferences.getInt("user_id", -1);

        if (userId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    // 🏃 请求计步权限
    private void requestActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        REQUEST_ACTIVITY_RECOGNITION
                );
            }
        }
    }
    private void setupDailyReminder() {
        // 创建一个周期性任务：每 12 小时执行一次（注意：WorkManager 最小间隔是 15 分钟）
        PeriodicWorkRequest reminderRequest = new PeriodicWorkRequest.Builder(
                ReminderWorker.class, 12, TimeUnit.HOURS)
                .addTag("diet_reminder")
                .build();

        // 关键：使用 KEEP 模式，防止每次打开 App 都重复排查任务
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "daily_diet_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                reminderRequest
        );
    }

    // 📩 权限结果回调
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "计步权限已开启", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "未开启计步权限，步数功能不可用", Toast.LENGTH_LONG).show();
            }
        }
    }
}