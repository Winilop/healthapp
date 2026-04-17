package com.example.healthapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.healthapp.R;

public class RecordFragment extends Fragment {

    private Button btnDiet, btnSport;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        btnDiet = view.findViewById(R.id.btn_show_diet);
        btnSport = view.findViewById(R.id.btn_show_sport);

        // 1. 默认显示饮食记录
        switchFragment(new DietFragment());
        updateButtonStyles(true); // 饮食按钮高亮

        // 2. 点击饮食按钮
        btnDiet.setOnClickListener(v -> {
            switchFragment(new DietFragment());
            updateButtonStyles(true);
        });

        // 3. 点击运动按钮
        btnSport.setOnClickListener(v -> {
            // 关键：现在点击会跳转到 SportFragment 了
            switchFragment(new SportFragment());
            updateButtonStyles(false);
        });

        return view;
    }

    private void switchFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.record_content_container, fragment)
                .commit();
    }

    // 简单的视觉反馈：点击哪个，哪个颜色变深
    private void updateButtonStyles(boolean isDiet) {
        if (isDiet) {
            btnDiet.setAlpha(1.0f);
            btnSport.setAlpha(0.5f);
        } else {
            btnDiet.setAlpha(0.5f);
            btnSport.setAlpha(1.0f);
        }
    }
}