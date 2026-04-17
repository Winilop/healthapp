package com.example.healthapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.healthapp.db.AppDatabase;
import com.example.healthapp.db.SportRecord;
import java.util.ArrayList;
import java.util.List;

public class SportAdapter extends RecyclerView.Adapter<SportAdapter.ViewHolder> {
    private List<SportRecord> list = new ArrayList<>();
    private Context context;

    // 构造函数接收 Context
    public SportAdapter(Context context) {
        this.context = context;
    }

    public void setList(List<SportRecord> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        SportRecord record = list.get(position);
        new Thread(() -> {
            AppDatabase.getInstance(context).sportRecordDao().delete(record);
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> {
                    list.remove(position);
                    notifyItemRemoved(position);
                });
            }
        }).start();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // --- 核心修改：使用 parent.getContext() 确保 inflated 成功 ---
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sport_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SportRecord record = list.get(position);
        holder.tvDate.setText(record.date);
        holder.tvSteps.setText(record.steps + " 步");
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvSteps;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // --- 核心修改：确保 ID 与 item_sport_record.xml 一致 ---
            tvDate = itemView.findViewById(R.id.tv_item_sport_date);
            tvSteps = itemView.findViewById(R.id.tv_item_sport_steps);
        }
    }
}