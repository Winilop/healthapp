package com.example.healthapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthapp.db.AppDatabase;
import com.example.healthapp.db.MealRecord;

import java.util.ArrayList;
import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private List<MealRecord> mealList = new ArrayList<>();
    private Context context; // --- 新增：为了在删除时使用数据库和主线程 ---

    // --- 新增：修改构造函数，传入 context ---
    public MealAdapter(Context context) {
        this.context = context;
    }

    public void setMeals(List<MealRecord> meals) {
        this.mealList = meals;
        notifyDataSetChanged();
    }

    // --- 新增：左滑删除的具体逻辑 ---
    public void deleteItem(int position) {
        MealRecord meal = mealList.get(position);

        new Thread(() -> {
            // 从数据库删除
            AppDatabase.getInstance(context).healthDao().deleteMeal(meal);

            // 回到主线程更新 UI
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> {
                    mealList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "已删除该记录", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        MealRecord record = mealList.get(position);
        holder.tvFoodName.setText(record.foodName);
        holder.tvMealType.setText(record.mealType);
        holder.tvCalories.setText(record.calories + " kcal");
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvMealType, tvCalories;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tv_item_food_name);
            tvMealType = itemView.findViewById(R.id.tv_item_meal_type);
            tvCalories = itemView.findViewById(R.id.tv_item_calories);
        }
    }
}