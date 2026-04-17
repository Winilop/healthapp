package com.example.healthapp.fragment;
import android.app.AlertDialog;

import android.content.Context;

import android.graphics.Color;

import android.hardware.Sensor;

import android.hardware.SensorEvent;

import android.hardware.SensorEventListener;

import android.hardware.SensorManager;

import android.os.Bundle;

import android.os.SystemClock;

import android.os.Vibrator;

import android.text.InputType;

import android.view.LayoutInflater;

import android.view.View;

import android.view.ViewGroup;

import android.widget.Button;

import android.widget.EditText;

import android.widget.LinearLayout;

import android.widget.ProgressBar;

import android.widget.TextView;

import android.widget.Toast;



import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;



import com.example.healthapp.R;

import com.example.healthapp.db.AppDatabase;

import com.example.healthapp.db.DrinkRecord;

import com.example.healthapp.db.SleepRecord;

import com.example.healthapp.db.SportRecord;

import com.example.healthapp.db.User;

import com.example.healthapp.utils.GoalManager;

import com.example.healthapp.utils.PlanGenerator;



import java.util.Locale;



public class DashboardFragment extends Fragment implements SensorEventListener {



// 控件声明

    private TextView tvSteps, tvType, tvTime;

    private TextView tvBmiValue, tvBmiStatus;

    private TextView tvAge, tvHeight, tvWeight, tvGender, tvTip;

    private TextView tvDrinkTotal, tvDrinkPercent;

    private ProgressBar pbDrink, pbSteps, pbCalories;

    private Button btnDrink;

    private TextView tvStepsDisplay, tvCaloriesTotal;



// 睡眠与个性化组件

    private TextView tvSleepScore, tvSleepBrief;

    private Button btnAddSleep;

    private TextView tvSportPlan;

    private Button btnRecordRun, btnRecordYoga;



// 传感器与逻辑变量

    private SensorManager sensorManager;

    private Sensor stepSensor;

    private int baseSteps = -1;

    private long startTime;

    private boolean isGoalReached = false;



    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        initViews(view);

        setupListeners();



        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {

            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        }



        startTime = SystemClock.elapsedRealtime();

        return view;

    }



    private void initViews(View view) {

        tvAge = view.findViewById(R.id.tv_age);

        tvGender = view.findViewById(R.id.tv_gender);

        tvHeight = view.findViewById(R.id.tv_height);

        tvWeight = view.findViewById(R.id.tv_weight);

        tvBmiValue = view.findViewById(R.id.tv_bmi_value);

        tvBmiStatus = view.findViewById(R.id.tv_bmi_status);

        tvTip = view.findViewById(R.id.tv_tip);

        tvStepsDisplay = view.findViewById(R.id.tv_steps_display);

        pbSteps = view.findViewById(R.id.pb_steps);

        tvSteps = view.findViewById(R.id.tv_steps);

        tvType = view.findViewById(R.id.tv_type);

        tvTime = view.findViewById(R.id.tv_time);

        tvCaloriesTotal = view.findViewById(R.id.tv_calories_total);

        pbCalories = view.findViewById(R.id.pb_calories);

        tvDrinkTotal = view.findViewById(R.id.tv_drink_total);

        tvDrinkPercent = view.findViewById(R.id.tv_drink_percent);

        pbDrink = view.findViewById(R.id.pb_drink);

        btnDrink = view.findViewById(R.id.btn_add_water);

        tvSleepScore = view.findViewById(R.id.tv_dashboard_sleep_score);

        tvSleepBrief = view.findViewById(R.id.tv_sleep_brief);

        btnAddSleep = view.findViewById(R.id.btn_add_sleep);

        tvSportPlan = view.findViewById(R.id.tv_sport_plan);

        btnRecordRun = view.findViewById(R.id.btn_record_run);

        btnRecordYoga = view.findViewById(R.id.btn_record_yoga);

    }



    private void setupListeners() {

        if (btnDrink != null) btnDrink.setOnClickListener(v -> showAddDrinkDialog());

        if (btnAddSleep != null) {

            btnAddSleep.setOnClickListener(v -> getParentFragmentManager().beginTransaction()

                    .replace(R.id.fragment_container, new SleepFragment())

                    .addToBackStack(null).commit());

        }

        if (btnRecordRun != null) btnRecordRun.setOnClickListener(v -> showSportInputDialog("跑步", 8.0f));

        if (btnRecordYoga != null) btnRecordYoga.setOnClickListener(v -> showSportInputDialog("瑜伽", 3.0f));



// 长按卡片清空数据

        if (pbSteps != null) {

            View stepCard = (View) pbSteps.getParent().getParent();

            stepCard.setOnLongClickListener(v -> {

                showClearDataConfirmDialog();

                return true;

            });

        }



// 短按步数文字：弹出修改目标对话框 (核心新增)

        if (tvStepsDisplay != null) {

            tvStepsDisplay.setOnClickListener(v -> showEditGoalDialog());

        }

    }



    @Override

    public void onResume() {

        super.onResume();

        loadHealthData();

        refreshDrinkData();

        loadLatestSleep();

        if (sensorManager != null && stepSensor != null) {

            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);

        }

    }



// --- 核心：目标修改对话框 ---

    private void showEditGoalDialog() {

// 动态创建一个布局（为了毕设演示方便，也可以直接加载xml）

        LinearLayout layout = new LinearLayout(getContext());

        layout.setOrientation(LinearLayout.VERTICAL);

        layout.setPadding(50, 40, 50, 10);



        final EditText etStepGoal = new EditText(getContext());

        etStepGoal.setHint("步数目标 (如:8000)");

        etStepGoal.setInputType(InputType.TYPE_CLASS_NUMBER);

        etStepGoal.setText(String.valueOf(GoalManager.getStepGoal(getContext())));



        final EditText etDrinkGoal = new EditText(getContext());

        etDrinkGoal.setHint("饮水目标ml (如:2500)");

        etDrinkGoal.setInputType(InputType.TYPE_CLASS_NUMBER);

        etDrinkGoal.setText(String.valueOf(GoalManager.getDrinkGoal(getContext())));



        layout.addView(etStepGoal);

        layout.addView(etDrinkGoal);



        new AlertDialog.Builder(getContext())

                .setTitle("设定个人每日目标")

                .setView(layout)

                .setPositiveButton("保存", (dialog, which) -> {

                    try {

                        int s = Integer.parseInt(etStepGoal.getText().toString());

                        int d = Integer.parseInt(etDrinkGoal.getText().toString());

                        GoalManager.setGoals(getContext(), s, d);

                        loadHealthData(); // 刷新

                        refreshDrinkData(); // 刷新

                        Toast.makeText(getContext(), "目标已更新", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {

                        Toast.makeText(getContext(), "输入有误", Toast.LENGTH_SHORT).show();

                    }

                })

                .setNegativeButton("取消", null).show();

    }



    private void loadHealthData() {

        String today = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();

        new Thread(() -> {

            User user = AppDatabase.getInstance(getContext()).healthDao().getUser();

            int totalCalories = AppDatabase.getInstance(getContext()).sportRecordDao().getTodayTotalCalories(today);

            SportRecord sportRecord = AppDatabase.getInstance(getContext()).sportRecordDao().getRecordByDate(today);



            if (getActivity() != null && isAdded()) {

                getActivity().runOnUiThread(() -> {

                    if (tvCaloriesTotal != null) tvCaloriesTotal.setText("今日消耗：" + totalCalories + " kcal");

                    if (pbCalories != null) pbCalories.setProgress(totalCalories);



                    if (sportRecord != null) {

                        if (tvStepsDisplay != null) tvStepsDisplay.setText(String.valueOf(sportRecord.steps));

                        if (pbSteps != null) {

                            pbSteps.setMax(GoalManager.getStepGoal(getContext())); // 动态进度条

                            pbSteps.setProgress(sportRecord.steps);

                        }

                        checkGoalLinkage(sportRecord.steps, true); // 静默检查

                    }

                    updateBmiUI(user);

                });

            }

        }).start();

    }



    private void updateBmiUI(User user) {

        if (user == null) return;

        float hM = user.height / 100;

        float bmi = (hM > 0) ? user.weight / (hM * hM) : 0;

        String status; int color; String tip;

        if (bmi <= 0) { status = "未知"; color = Color.GRAY; tip = "暂无健康数据"; }

        else if (bmi < 18.5) { status = "偏瘦"; color = Color.parseColor("#FFBB33"); tip = "建议加强营养与抗阻训练"; }

        else if (bmi < 24) { status = "正常"; color = Color.parseColor("#99CC00"); tip = "状态极佳，请保持运动"; }

        else if (bmi < 28) { status = "超重"; color = Color.parseColor("#FF8800"); tip = "建议增加有氧运动量"; }

        else { status = "肥胖"; color = Color.RED; tip = "建议控制饮食，循序渐进运动"; }



        tvAge.setText("年龄：" + user.age);

        tvGender.setText("性别：" + (user.gender == 1 ? "男" : "女"));

        tvHeight.setText("身高：" + (int)user.height + " cm");

        tvWeight.setText("体重：" + (int)user.weight + " kg");

        tvBmiValue.setText(String.format(Locale.getDefault(), "%.1f", bmi));

        if (tvBmiStatus != null) { tvBmiStatus.setText(status); tvBmiStatus.setTextColor(color); }

        if (tvTip != null) tvTip.setText(tip);

        if (tvSportPlan != null) tvSportPlan.setText(PlanGenerator.generate(bmi, "减脂"));

    }



    private void showClearDataConfirmDialog() {

        new AlertDialog.Builder(getContext()).setTitle("调试模式").setMessage("确定要清空今日记录吗？")

                .setPositiveButton("确定清空", (dialog, which) -> clearTodayData())

                .setNegativeButton("取消", null).show();

    }



    private void clearTodayData() {

        String today = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();

        if (sensorManager != null) sensorManager.unregisterListener(this);



        new Thread(() -> {

            AppDatabase.getInstance(getContext()).sportRecordDao().deleteTodayRecord(today);

            if (getActivity() != null) {

                getActivity().runOnUiThread(() -> {

                    baseSteps = -1;

                    isGoalReached = false;

                    if (tvStepsDisplay != null) tvStepsDisplay.setText("0");

                    if (pbSteps != null) pbSteps.setProgress(0);

                    if (tvCaloriesTotal != null) tvCaloriesTotal.setText("今日消耗：0 kcal");



                    if (getView() != null) {

                        try {

                            View pb = getView().findViewById(R.id.pb_steps);

                            ((View) pb.getParent().getParent()).setBackgroundColor(Color.WHITE);

                        } catch (Exception e) {}

                    }



                    if (sensorManager != null && stepSensor != null) {

                        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);

                    }

                    Toast.makeText(getContext(), "重置成功", Toast.LENGTH_SHORT).show();

                });

            }

        }).start();

    }



    private void saveManualSport(String type, int duration, float met) {

        new Thread(() -> {

            String today = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();

            User user = AppDatabase.getInstance(getContext()).healthDao().getUser();

            if (user == null) return;



            SportRecord record = AppDatabase.getInstance(getContext()).sportRecordDao().getRecordByDate(today);

            if (record == null) { record = new SportRecord(); record.date = today; record.steps = 0; record.calories = 0; }



            int extraSteps = ("跑步".equals(type)) ? duration * 160 : duration * 40;

            float burned = met * user.weight * (duration / 60.0f);



            record.steps += extraSteps;

            record.calories += burned;

            record.type = type;

            record.duration += duration;



            AppDatabase.getInstance(getContext()).sportRecordDao().insert(record);



            if (getActivity() != null) {

                final int finalTotalSteps = record.steps;

                getActivity().runOnUiThread(() -> {

                    Toast.makeText(getContext(), "记录成功！", Toast.LENGTH_SHORT).show();

                    loadHealthData();

                    checkGoalLinkage(finalTotalSteps, false); // 手动触发检查

                });

            }

        }).start();

    }



    private void checkGoalLinkage(int currentSteps, boolean isSilent) {

        int GOAL = GoalManager.getStepGoal(getContext()); // 动态获取目标值



        if (currentSteps < GOAL) {

            isGoalReached = false;

            return;

        }



        if (currentSteps >= GOAL) {

            if (getView() != null) {

                try {

                    View pb = getView().findViewById(R.id.pb_steps);

                    View stepCard = (View) pb.getParent().getParent();

                    stepCard.setBackgroundColor(Color.parseColor("#E8F5E9"));

                } catch (Exception e) {}

            }



            if (isGoalReached || isSilent) {

                isGoalReached = true;

                return;

            }



            isGoalReached = true;

            Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

            if (v != null && v.hasVibrator()) v.vibrate(500);



            new AlertDialog.Builder(getContext()).setTitle("🎉 目标达成！")

                    .setMessage("太棒了！你已经完成了 " + GOAL + " 步的目标！")

                    .setPositiveButton("太赞了", null).show();

        }

    }



    @Override

    public void onSensorChanged(SensorEvent event) {

        if (baseSteps == -1) baseSteps = (int) event.values[0];

        int steps = (int) event.values[0] - baseSteps;



        if (tvStepsDisplay != null) tvStepsDisplay.setText(String.valueOf(steps));

        if (pbSteps != null) {

            pbSteps.setMax(GoalManager.getStepGoal(getContext()));

            pbSteps.setProgress(steps);

        }



        updateTime();

        updateSportType(steps);

        checkGoalLinkage(steps, false);

        saveCurrentSteps(steps);

    }



    private void saveCurrentSteps(int steps) {

        String today = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();

        new Thread(() -> {

            SportRecord record = AppDatabase.getInstance(getContext()).sportRecordDao().getRecordByDate(today);

            if (record == null) {

                record = new SportRecord(); record.date = today; record.steps = steps; record.type = "步行";

            } else if (steps > record.steps) {

                record.steps = steps;

            }

            AppDatabase.getInstance(getContext()).sportRecordDao().insert(record);

        }).start();

    }



    private void refreshDrinkData() {

        String today = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();

        int goal = GoalManager.getDrinkGoal(getContext()); // 动态获取目标值

        new Thread(() -> {

            int total = AppDatabase.getInstance(getContext()).healthDao().getTodayTotalDrink(today);

            if (getActivity() != null && isAdded()) {

                getActivity().runOnUiThread(() -> {

                    if (tvDrinkTotal != null) tvDrinkTotal.setText("今日饮水：" + total + " ml");

                    if (pbDrink != null) {

                        pbDrink.setMax(goal);

                        pbDrink.setProgress(total);

                    }

                    if (tvDrinkPercent != null) {

                        int percent = (int)(total / (float)goal * 100);

                        tvDrinkPercent.setText("完成度：" + Math.min(percent, 100) + "%");

                    }

                });

            }

        }).start();

    }



// --- 其他原有逻辑(饮水/睡眠/时间)保持不变 ---

    private void showSportInputDialog(String type, float met) {

        final EditText et = new EditText(getContext()); et.setInputType(InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(getContext()).setTitle("时长(分钟):" + type).setView(et)

                .setPositiveButton("确认", (d, w) -> {

                    if (!et.getText().toString().isEmpty()) saveManualSport(type, Integer.parseInt(et.getText().toString()), met);

                }).show();

    }



    private void updateTime() {

        long elapsed = SystemClock.elapsedRealtime() - startTime;

        int seconds = (int) (elapsed / 1000);

        if (tvTime != null) tvTime.setText(String.format(Locale.getDefault(), "时间：%02d:%02d", seconds / 60, seconds % 60));

    }



    private void updateSportType(int steps) {

        long elapsedMillis = SystemClock.elapsedRealtime() - startTime;

        long minutes = elapsedMillis / 60000;

        if (minutes <= 0) return;

        int rate = (int) (steps / minutes);

        if (tvType != null) tvType.setText("类型：" + (rate >= 130 ? "跑步" : (rate >= 90 ? "快走" : "步行")));

    }



    private void saveDrinkRecord(int amount) {

        new Thread(() -> {

            DrinkRecord r = new DrinkRecord(); r.amount = amount; r.timestamp = System.currentTimeMillis();

            r.date = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();

            AppDatabase.getInstance(getContext()).healthDao().insertDrink(r);

            refreshDrinkData();

        }).start();

    }



    private void showAddDrinkDialog() {

        final EditText et = new EditText(getContext()); et.setInputType(InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(getContext()).setTitle("饮水量(ml)").setView(et)

                .setPositiveButton("确认", (d, w) -> {

                    if (!et.getText().toString().isEmpty()) saveDrinkRecord(Integer.parseInt(et.getText().toString()));

                }).show();

    }



    private void loadLatestSleep() {

        new Thread(() -> {

            SleepRecord r = AppDatabase.getInstance(getContext()).healthDao().getLatestSleep();

            if (getActivity() != null && isAdded()) {

                getActivity().runOnUiThread(() -> {

                    if (r != null) {

                        tvSleepScore.setText(String.valueOf(r.score));

                        tvSleepBrief.setText(String.format(Locale.getDefault(), "时长: %.1fh | 深睡: %.1fh", r.totalHours, r.deepSleepHours));

                    }

                });

            }

        }).start();

    }



    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override public void onPause() { super.onPause(); if (sensorManager != null) sensorManager.unregisterListener(this); }

}