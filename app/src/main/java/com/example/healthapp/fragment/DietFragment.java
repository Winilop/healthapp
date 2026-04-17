package com.example.healthapp.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthapp.MealAdapter;
import com.example.healthapp.R;
import com.example.healthapp.db.AppDatabase;
import com.example.healthapp.db.MealRecord;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class DietFragment extends Fragment {

    private TextView tvTotalCalories, tvTitle;
    private RecyclerView recyclerView;
    private MealAdapter adapter;
    private Button btnAddMeal;
    private ImageView ivCalendar;
    private ImageButton btnFoodWiki;

    // 日期处理相关
    private String selectedDate;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diet, container, false);

        // 1. 初始化控件
        tvTotalCalories = view.findViewById(R.id.tv_total_calories);
        // 如果你在 XML 标题处加了 ID，可以用这个来显示当前日期提示
        // tvTitle = view.findViewById(R.id.tv_diet_title);
        recyclerView = view.findViewById(R.id.rv_meal_list);
        btnAddMeal = view.findViewById(R.id.btn_add_meal);
        ivCalendar = view.findViewById(R.id.iv_calendar);
        btnFoodWiki = view.findViewById(R.id.btn_food_wiki);

        // 2. 初始化日期（默认为今天）
        selectedDate = sdf.format(new Date());

        // 3. 配置 RecyclerView
        adapter = new MealAdapter(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        setupSwipeToDelete();

        // 4. 设置点击事件
        btnAddMeal.setOnClickListener(v -> showAddMealDialog());
        ivCalendar.setOnClickListener(v -> showDatePicker());
        if (btnFoodWiki != null) {
            btnFoodWiki.setOnClickListener(v -> showFoodWikiDialog());
        }

        return view;
    }
    private void showFoodWikiDialog() {
        // 1. 创建 BottomSheetDialog 对象
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());

        // 2. 加载你之前创建的 layout_food_reference 布局
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.layout_food_reference, null);
        bottomSheetDialog.setContentView(sheetView);

        // 3. 处理关闭按钮逻辑
        Button btnClose = sheetView.findViewById(R.id.btn_close_sheet);
        if (btnClose != null) {
            btnClose.setOnClickListener(view -> bottomSheetDialog.dismiss());
        }

        // 4. 显示弹窗
        bottomSheetDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        // 1. 获取当前系统时间作为“最大可选日期”
        long now = System.currentTimeMillis();

        // 2. 如果当前已经选了日期，让弹窗默认选中那个日期
        try {
            Date date = sdf.parse(selectedDate);
            if (date != null) calendar.setTime(date);
        } catch (Exception e) { e.printStackTrace(); }

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            selectedDate = sdf.format(calendar.getTime());

            Toast.makeText(getContext(), "查看记录: " + selectedDate, Toast.LENGTH_SHORT).show();
            refreshData();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        // 【核心代码】限制最大可选日期为“现在”
        // 这样用户在日历里就点不动今天之后的日期了（通常显示为灰色）
        datePickerDialog.getDatePicker().setMaxDate(now);

        datePickerDialog.show();
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                adapter.deleteItem(position);
                // 删除后更新总热量显示
                refreshData();
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

    private void showAddMealDialog() {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 30, 60, 30);

        final EditText etFood = new EditText(getContext());
        etFood.setHint("食物名称");
        layout.addView(etFood);

        final EditText etCal = new EditText(getContext());
        etCal.setHint("卡路里 (kcal)");
        etCal.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etCal);

        new AlertDialog.Builder(getContext())
                .setTitle("添加饮食记录")
                .setView(layout)
                .setPositiveButton("保存", (dialog, which) -> {
                    String name = etFood.getText().toString();
                    String calStr = etCal.getText().toString();
                    if (!name.isEmpty() && !calStr.isEmpty()) {
                        saveMeal(name, Integer.parseInt(calStr));
                    } else {
                        Toast.makeText(getContext(), "请输入完整信息", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void saveMeal(String name, int calories) {
        // 永远获取当前的真实日期用于保存
        String today = sdf.format(new Date());

        new Thread(() -> {
            MealRecord record = new MealRecord();
            record.foodName = name;
            record.calories = calories;
            record.mealType = "日常饮食";
            record.date = today;
            record.timestamp = System.currentTimeMillis();

            AppDatabase.getInstance(getContext()).healthDao().insertMeal(record);

            // 保存成功后，自动切回今天并刷新
            selectedDate = today;
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::refreshData);
            }
        }).start();
    }

    private void refreshData() {
        new Thread(() -> {
            List<MealRecord> meals = AppDatabase.getInstance(getContext()).healthDao().getMealsByDate(selectedDate);
            int total = AppDatabase.getInstance(getContext()).healthDao().getTodayTotalCalories(selectedDate);

            if (getActivity() != null && isAdded()) {
                getActivity().runOnUiThread(() -> {
                    adapter.setMeals(meals);
                    if (tvTotalCalories != null) {
                        // 只显示数值，因为单位 kcal 已经在 XML 里写好了
                        tvTotalCalories.setText(String.valueOf(total));
                    }
                });
            }
        }).start();
    }
}