package com.example.healthapp.fragment;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healthapp.R;
import com.example.healthapp.db.AppDatabase;
import com.example.healthapp.db.DateValueEntity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReportFragment extends Fragment {

    private BarChart barChartCalories, barChartBurn;
    private LineChart lineChartSteps;
    private TextView tvReportSummary;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        // 绑定控件
        barChartCalories = view.findViewById(R.id.bar_chart_calories);
        lineChartSteps = view.findViewById(R.id.line_chart_steps);
        barChartBurn = view.findViewById(R.id.chart_calories_burn);
        tvReportSummary = view.findViewById(R.id.tv_report_summary);

        loadWeeklyData();
        return view;
    }

    private void loadWeeklyData() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        String endDate = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, -6);
        String startDate = sdf.format(cal.getTime());

        new Thread(() -> {
            // 获取三组核心数据
            List<DateValueEntity> calorieData = AppDatabase.getInstance(getContext()).healthDao().getWeeklyCalories(startDate, endDate);
            List<DateValueEntity> stepData = AppDatabase.getInstance(getContext()).healthDao().getWeeklySteps(startDate, endDate);
            List<DateValueEntity> burnData = AppDatabase.getInstance(getContext()).healthDao().getWeeklyBurn(startDate, endDate);

            if (getActivity() != null && isAdded()) {
                getActivity().runOnUiThread(() -> {
                    // 1. 绘制美化后的图表
                    drawEnhancedBarChart(barChartCalories, calorieData, "每日摄入", "#FF7043");
                    drawEnhancedLineChart(lineChartSteps, stepData, "步数趋势", "#2196F3");
                    drawEnhancedBarChart(barChartBurn, burnData, "每日消耗", "#E91E63");

                    // 2. 生成并显示总结语句
                    generateSummary(calorieData, stepData, burnData);
                });
            }
        }).start();
    }

    /**
     * 根据图表数据自动生成分析文字
     */
    private void generateSummary(List<DateValueEntity> calIn, List<DateValueEntity> steps, List<DateValueEntity> calOut) {
        if (calIn == null || steps == null || calIn.isEmpty()) {
            tvReportSummary.setText("暂无足够数据生成周报。");
            return;
        }

        float totalIn = 0;
        float totalSteps = 0;
        float maxSteps = 0;
        String bestStepDate = "";

        for (DateValueEntity d : calIn) totalIn += d.total;
        for (DateValueEntity s : steps) {
            totalSteps += s.total;
            if (s.total > maxSteps) {
                maxSteps = s.total;
                bestStepDate = s.date;
            }
        }

        int avgIn = (int) (totalIn / calIn.size());
        int avgSteps = (int) (totalSteps / steps.size());

        StringBuilder sb = new StringBuilder();

        // 饮食分析
        sb.append("🍎 【饮食记录】\n");
        sb.append("本周平均每日摄入 ").append(avgIn).append(" kcal。");
        if (avgIn > 2200) {
            sb.append("摄入量略高于平均标准，建议减少高糖食物，增加蛋白质摄入。");
        } else if (avgIn < 1200 && avgIn > 0) {
            sb.append("摄入量偏低，请确保营养均衡，避免过度节食。");
        } else {
            sb.append("饮食控制非常稳定，请继续保持！");
        }

        sb.append("\n\n🏃 【运动趋势】\n");
        sb.append("本周平均步数 ").append(avgSteps).append(" 步。");
        if (!bestStepDate.isEmpty()) {
            sb.append("表现最棒的一天是 ").append(bestStepDate.substring(5)).append("，达到了 ").append((int)maxSteps).append(" 步。");
        }

        if (avgSteps < 5000) {
            sb.append("\n下周尝试每天多走 1000 步，让身体更有活力。");
        } else {
            sb.append("\n你的运动量非常达标，身体素质正在稳步提升！");
        }

        tvReportSummary.setText(sb.toString());
    }

    private void drawEnhancedBarChart(BarChart chart, List<DateValueEntity> data, String label, String colorCode) {
        if (chart == null || data == null || data.isEmpty()) return;

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> xLabels = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            entries.add(new BarEntry(i, data.get(i).total));
            String date = data.get(i).date;
            xLabels.add(date.length() > 5 ? date.substring(5) : date);
        }

        BarDataSet dataSet = new BarDataSet(entries, label);
        int color = Color.parseColor(colorCode);
        dataSet.setGradientColor(color, Color.argb(100, Color.red(color), Color.green(color), Color.blue(color)));
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(9f);

        chart.setData(new BarData(dataSet));
        chart.getBarData().setBarWidth(0.45f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setSpaceTop(20f);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setGridColor(Color.parseColor("#EEEEEE"));
        leftAxis.setGridDashedLine(new DashPathEffect(new float[]{10f, 10f}, 0f));

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setForm(Legend.LegendForm.CIRCLE);
        chart.animateY(1000);
        chart.invalidate();
    }

    private void drawEnhancedLineChart(LineChart chart, List<DateValueEntity> data, String label, String colorCode) {
        if (chart == null || data == null || data.isEmpty()) return;

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> xLabels = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            entries.add(new Entry(i, data.get(i).total));
            String date = data.get(i).date;
            xLabels.add(date.length() > 5 ? date.substring(5) : date);
        }

        LineDataSet dataSet = new LineDataSet(entries, label);
        int color = Color.parseColor(colorCode);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setColor(color);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleColor(color);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.WHITE);

        dataSet.setDrawFilled(true);
        dataSet.setFillColor(color);
        dataSet.setFillAlpha(50);

        chart.setData(new LineData(dataSet));

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setSpaceTop(25f);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setGridColor(Color.parseColor("#EEEEEE"));
        leftAxis.setGridDashedLine(new DashPathEffect(new float[]{10f, 10f}, 0f));

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setForm(Legend.LegendForm.CIRCLE);
        chart.animateX(1000);
        chart.invalidate();
    }
}