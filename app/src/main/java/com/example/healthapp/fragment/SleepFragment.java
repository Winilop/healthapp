package com.example.healthapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.healthapp.R;
import com.example.healthapp.db.AppDatabase;
import com.example.healthapp.db.SleepRecord;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SleepFragment extends Fragment {

    private TextView tv_sleep_score, tv_sleep_advice;
    private Button btn_save_sleep;
    private EditText et_total_hours, et_deep_sleep, et_wake_up;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sleep, container, false);

        // 绑定 ID (必须与 fragment_sleep.xml 一致)
        tv_sleep_score = view.findViewById(R.id.tv_sleep_score);
        tv_sleep_advice = view.findViewById(R.id.tv_sleep_advice);
        et_total_hours = view.findViewById(R.id.et_total_hours);
        et_deep_sleep = view.findViewById(R.id.et_deep_sleep);
        et_wake_up = view.findViewById(R.id.et_wake_up);
        btn_save_sleep = view.findViewById(R.id.btn_save_sleep);

        btn_save_sleep.setOnClickListener(v -> saveRecord());

        return view;
    }

    private void saveRecord() {
        String totalStr = et_total_hours.getText().toString();
        String deepStr = et_deep_sleep.getText().toString();
        String wakeStr = et_wake_up.getText().toString();

        if (totalStr.isEmpty() || deepStr.isEmpty() || wakeStr.isEmpty()) {
            Toast.makeText(getContext(), "请完整录入", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            float total = Float.parseFloat(totalStr);
            float deep = Float.parseFloat(deepStr);
            int wake = Integer.parseInt(wakeStr);

            // 基础算法：根据时长、深睡比、醒来次数扣分
            int score = 100 - (int)((8 - total) * 5) - (int)((0.25 - deep/total) * 100) - (wake * 5);
            score = Math.min(100, Math.max(0, score));

            SleepRecord record = new SleepRecord();
            record.date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
            record.totalHours = total;
            record.deepSleepHours = deep;
            record.wakeCount = wake;
            record.score = score;

            AppDatabase.getInstance(getContext()).healthDao().insertSleep(record);

            int finalScore = score;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tv_sleep_score.setText(String.valueOf(finalScore));
                    tv_sleep_advice.setText(getAdvice(finalScore, deep/total));
                    Toast.makeText(getContext(), "保存成功，首页已更新", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private String getAdvice(int score, float ratio) {
        if (score >= 85) return "睡眠优秀，继续保持！";
        if (ratio < 0.2) return "深睡比例偏低，建议睡前放松。";
        return "建议规律作息。";
    }
}