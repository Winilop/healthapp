package com.example.healthapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUserId;
    private Button btnLogin;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化视图
        etUserId = findViewById(R.id.etUserId);
        btnLogin = findViewById(R.id.btnLogin);

        // 初始化 SharedPreferences
        preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // 登录按钮点击事件
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = etUserId.getText().toString().trim();
                if (!userId.isEmpty()) {
                    // 保存用户 ID
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("user_id", Integer.parseInt(userId));
                    editor.apply();

                    // 跳转到 MainActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // 关闭 LoginActivity
                }
            }
        });
    }
}
