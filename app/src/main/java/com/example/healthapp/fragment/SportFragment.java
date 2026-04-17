package com.example.healthapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.healthapp.R;
import com.example.healthapp.SportAdapter;
import com.example.healthapp.db.AppDatabase;
import com.example.healthapp.db.SportRecord;
import java.util.List;

public class SportFragment extends Fragment {

    private RecyclerView recyclerView;
    private SportAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 1. 加载布局
        View view = inflater.inflate(R.layout.fragment_sport_detail, container, false);

        // 2. 绑定 RecyclerView (确保你的 fragment_sport_detail.xml 里有这个 ID)
        recyclerView = view.findViewById(R.id.rv_sport_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 3. 设置适配器
        adapter = new SportAdapter(getContext());
        recyclerView.setAdapter(adapter);

        // 4. 开启左滑删除功能
        setupSwipeToDelete();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSportData(); // 每次刷新页面加载最新数据
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // 执行删除
                adapter.deleteItem(viewHolder.getAdapterPosition());
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

    private void loadSportData() {
        new Thread(() -> {
            // 获取所有运动记录展示在列表中
            List<SportRecord> list = AppDatabase.getInstance(getContext()).sportRecordDao().getAllRecords();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.setList(list);
                });
            }
        }).start();
    }
}