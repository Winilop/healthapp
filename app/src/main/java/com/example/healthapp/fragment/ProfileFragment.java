package com.example.healthapp.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.work.*;

import com.example.healthapp.R;
import com.example.healthapp.db.AppDatabase;
import com.example.healthapp.db.User;
import com.example.healthapp.utils.ReminderWorker;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ProfileFragment extends Fragment {

    private EditText etUsername, etAge, etHeight, etWeight, etCustomContent, etCustomInterval;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private Button btnSave, btnEditMode, btnLogout, btnAddCustom;
    private View layoutSpace, layoutSetup;
    private TextView tvStatsSummary, tvWelcomeUser;
    private ImageView ivAvatarSetup, ivAvatarDisplay;
    private SwitchMaterial switchWater, switchPill;
    private LinearLayout llCustomTasksContainer;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化图片选择器
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            saveAvatar(uri.toString());
                            updateAvatarUI(uri);
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initViews(view);

        SharedPreferences sp = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String currentUserName = sp.getString("current_login_user", null);

        // 如果已登录，显示个人空间并加载数据
        if (currentUserName != null && !currentUserName.isEmpty()) {
            toggleLayout(true);
            refreshUIFromDb(currentUserName);
            restoreCustomReminders(currentUserName);
            loadSavedData(currentUserName);
        } else {
            toggleLayout(false);
        }

        setupClickListeners();
        return view;
    }

    private void initViews(View view) {
        etUsername = view.findViewById(R.id.et_username);
        etAge = view.findViewById(R.id.et_age);
        etHeight = view.findViewById(R.id.et_height);
        etWeight = view.findViewById(R.id.et_weight);
        rgGender = view.findViewById(R.id.rg_gender);
        rbMale = view.findViewById(R.id.rb_male);
        rbFemale = view.findViewById(R.id.rb_female);

        btnSave = view.findViewById(R.id.btn_save);
        layoutSpace = view.findViewById(R.id.layout_personal_space);
        layoutSetup = view.findViewById(R.id.layout_profile_setup);
        tvStatsSummary = view.findViewById(R.id.tv_stats_summary);
        tvWelcomeUser = view.findViewById(R.id.tv_welcome_user);
        btnEditMode = view.findViewById(R.id.btn_edit_mode);
        btnLogout = view.findViewById(R.id.btn_logout_mock);
        ivAvatarSetup = view.findViewById(R.id.iv_avatar_setup);
        ivAvatarDisplay = view.findViewById(R.id.iv_avatar_display);

        switchWater = view.findViewById(R.id.switch_water_reminder);
        switchPill = view.findViewById(R.id.switch_pill_reminder);
        etCustomContent = view.findViewById(R.id.et_custom_reminder_content);
        etCustomInterval = view.findViewById(R.id.et_custom_interval);
        btnAddCustom = view.findViewById(R.id.btn_add_custom_reminder);
        llCustomTasksContainer = view.findViewById(R.id.ll_custom_tasks_container);
    }

    private void setupClickListeners() {
        View.OnClickListener avatarClick = v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        };
        ivAvatarSetup.setOnClickListener(avatarClick);
        ivAvatarDisplay.setOnClickListener(avatarClick);

        btnSave.setOnClickListener(v -> handleLoginOrSave());
        btnEditMode.setOnClickListener(v -> toggleLayout(false));
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        bindWaterListener();
        bindPillListener();

        btnAddCustom.setOnClickListener(v -> {
            String content = etCustomContent.getText().toString().trim();
            String intervalStr = etCustomInterval.getText().toString().trim();
            String user = getCurrentUser();
            if (content.isEmpty() || intervalStr.isEmpty() || user == null) return;

            try {
                int hours = Integer.parseInt(intervalStr);
                String uniqueTag = "custom_" + user + "_" + System.currentTimeMillis();
                startTask("WORK_" + uniqueTag, "健康助手", content, (int)(System.currentTimeMillis()%10000), uniqueTag, hours);
                saveCustomReminderToPrefs(user, content + "|" + uniqueTag);
                addReminderSwitchToUI(content, uniqueTag);
                etCustomContent.setText("");
                etCustomInterval.setText("");
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "请输入正确的数字", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLoginOrSave() {
        String name = etUsername.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String hStr = etHeight.getText().toString().trim();
        String wStr = etWeight.getText().toString().trim();

        // 核心修改：将性别映射为 int (1=男, 2=女)
        int genderVal = 0;
        if (rbMale != null && rbMale.isChecked()) genderVal = 1;
        else if (rbFemale != null && rbFemale.isChecked()) genderVal = 2;

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "请输入用户名", Toast.LENGTH_SHORT).show();
            return;
        }

        final int finalGender = genderVal;
        new Thread(() -> {
            User user = AppDatabase.getInstance(getContext()).healthDao().getUserByUsername(name);
            if (user == null) {
                if (ageStr.isEmpty() || hStr.isEmpty() || wStr.isEmpty()) {
                    showToastOnUI("新用户请填写完整资料");
                    return;
                }
                user = new User();
                user.username = name;
                user.age = Integer.parseInt(ageStr);
                user.height = Float.parseFloat(hStr);
                user.weight = Float.parseFloat(wStr);
                user.gender = finalGender;
                AppDatabase.getInstance(getContext()).healthDao().insertUser(user);
            } else {
                if (!ageStr.isEmpty()) user.age = Integer.parseInt(ageStr);
                if (!hStr.isEmpty()) user.height = Float.parseFloat(hStr);
                if (!wStr.isEmpty()) user.weight = Float.parseFloat(wStr);
                user.gender = finalGender;
                AppDatabase.getInstance(getContext()).healthDao().updateUser(user);
            }

            SharedPreferences sp = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            sp.edit().putString("current_login_user", name).apply();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    toggleLayout(true);
                    refreshUIFromDb(name);
                    restoreCustomReminders(name);
                    loadSavedData(name);
                    Toast.makeText(getContext(), "资料已保存", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void refreshUIFromDb(String username) {
        new Thread(() -> {
            User user = AppDatabase.getInstance(getContext()).healthDao().getUserByUsername(username);
            if (user != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvWelcomeUser.setText("你好, " + username);
                    // 逻辑展示 int 型性别
                    String genderText = (user.gender == 1) ? "男" : (user.gender == 2 ? "女" : "未设置");
                    tvStatsSummary.setText(String.format(Locale.getDefault(), "性别: %s | 年龄: %d | 身高: %.1f cm | 体重: %.1f kg",
                            genderText, user.age, user.height, user.weight));
                });
            }
        }).start();
    }

    private void loadSavedData(String username) {
        SharedPreferences sp = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        // 恢复头像
        String uriStr = sp.getString("avatar_" + username, null);
        if (uriStr != null) updateAvatarUI(Uri.parse(uriStr));

        // 恢复提醒开关 (需要清除之前的监听防止初始化触发)
        if (switchWater != null) {
            switchWater.setOnCheckedChangeListener(null);
            switchWater.setChecked(sp.getBoolean("water_on_" + username, false));
            bindWaterListener();
        }
        if (switchPill != null) {
            switchPill.setOnCheckedChangeListener(null);
            switchPill.setChecked(sp.getBoolean("pill_on_" + username, false));
            bindPillListener();
        }

        // 核心修改：从数据库恢复 RadioButton 选中状态
        new Thread(() -> {
            User user = AppDatabase.getInstance(getContext()).healthDao().getUserByUsername(username);
            if (user != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (user.gender == 1 && rbMale != null) rbMale.setChecked(true);
                    else if (user.gender == 2 && rbFemale != null) rbFemale.setChecked(true);

                    // 填充 EditText 方便直接修改
                    etUsername.setText(user.username);
                    etAge.setText(String.valueOf(user.age));
                    etHeight.setText(String.valueOf(user.height));
                    etWeight.setText(String.valueOf(user.weight));
                });
            }
        }).start();
    }

    // --- 提醒绑定逻辑 ---
    private void bindWaterListener() {
        if (switchWater == null) return;
        switchWater.setOnCheckedChangeListener((bv, isChecked) -> {
            if (bv.isPressed()) {
                String user = getCurrentUser();
                if (isChecked) startTask("WATER_" + user, "饮水提醒", "该喝水了", 101, "tag_water_" + user, 2);
                else stopTask("tag_water_" + user);
                saveSwitchState("water_on_" + user, isChecked);
            }
        });
    }

    private void bindPillListener() {
        if (switchPill == null) return;
        switchPill.setOnCheckedChangeListener((bv, isChecked) -> {
            if (bv.isPressed()) {
                String user = getCurrentUser();
                if (isChecked) startTask("PILL_" + user, "服药提醒", "记得吃药哦", 102, "tag_pill_" + user, 24);
                else stopTask("tag_pill_" + user);
                saveSwitchState("pill_on_" + user, isChecked);
            }
        });
    }

    private void toggleLayout(boolean isShowSpace) {
        if (layoutSpace != null && layoutSetup != null) {
            layoutSpace.setVisibility(isShowSpace ? View.VISIBLE : View.GONE);
            layoutSetup.setVisibility(isShowSpace ? View.GONE : View.VISIBLE);
        }
    }

    private void restoreCustomReminders(String username) {
        SharedPreferences sp = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        Set<String> savedReminders = sp.getStringSet("reminders_" + username, new HashSet<>());
        llCustomTasksContainer.removeAllViews();
        for (String item : savedReminders) {
            String[] parts = item.split("\\|");
            if (parts.length == 2) addReminderSwitchToUI(parts[0], parts[1]);
        }
    }

    private void showLogoutDialog() {
        String[] options = {"退出登录", "销毁账号"};
        new AlertDialog.Builder(requireContext()).setTitle("账号管理")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) handleSoftLogout();
                    else handleDeepLogout();
                }).show();
    }

    private void handleSoftLogout() {
        WorkManager.getInstance(requireContext()).cancelAllWork();
        requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit()
                .remove("current_login_user").apply();
        toggleLayout(false);
        // 清除输入框
        etUsername.setText(""); etAge.setText(""); etHeight.setText(""); etWeight.setText("");
    }

    private void handleDeepLogout() {
        String user = getCurrentUser();
        if (user == null) return;
        new Thread(() -> {
            AppDatabase.getInstance(getContext()).healthDao().deleteUserByName(user);
            if (getActivity() != null) getActivity().runOnUiThread(this::handleSoftLogout);
        }).start();
    }

    private String getCurrentUser() {
        return requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("current_login_user", null);
    }

    private void saveAvatar(String path) {
        String user = getCurrentUser();
        if (user != null) requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .edit().putString("avatar_" + user, path).apply();
    }

    private void addReminderSwitchToUI(String content, String tag) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 10, 0, 10);
        TextView tv = new TextView(getContext());
        tv.setText(content);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        SwitchMaterial sw = new SwitchMaterial(getContext());
        sw.setChecked(true);
        sw.setOnCheckedChangeListener((btn, isChecked) -> {
            if (!isChecked) {
                stopTask(tag);
                removeCustomReminderFromPrefs(content + "|" + tag);
                llCustomTasksContainer.removeView(row);
            }
        });
        row.addView(tv); row.addView(sw);
        llCustomTasksContainer.addView(row);
    }

    private void saveCustomReminderToPrefs(String username, String entry) {
        SharedPreferences sp = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(sp.getStringSet("reminders_" + username, new HashSet<>()));
        set.add(entry);
        sp.edit().putStringSet("reminders_" + username, set).apply();
    }

    private void removeCustomReminderFromPrefs(String entry) {
        String user = getCurrentUser();
        if (user == null) return;
        SharedPreferences sp = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(sp.getStringSet("reminders_" + user, new HashSet<>()));
        set.remove(entry);
        sp.edit().putStringSet("reminders_" + user, set).apply();
    }

    private void startTask(String workName, String title, String content, int id, String tag, int hours) {
        Data data = new Data.Builder().putString("title", title).putString("content", content).putInt("id", id).build();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(ReminderWorker.class, hours, TimeUnit.HOURS)
                .setInputData(data).addTag(tag).build();
        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(workName, ExistingPeriodicWorkPolicy.REPLACE, request);
    }

    private void stopTask(String tag) {
        WorkManager.getInstance(requireContext()).cancelAllWorkByTag(tag);
    }

    private void updateAvatarUI(Uri uri) {
        if (ivAvatarSetup != null) ivAvatarSetup.setImageURI(uri);
        if (ivAvatarDisplay != null) ivAvatarDisplay.setImageURI(uri);
    }

    private void saveSwitchState(String key, boolean val) {
        requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit().putBoolean(key, val).apply();
    }

    private void showToastOnUI(String msg) {
        if (getActivity() != null) getActivity().runOnUiThread(() ->
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show());
    }
}