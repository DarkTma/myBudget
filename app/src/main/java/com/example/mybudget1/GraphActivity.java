package com.example.mybudget1;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.renderer.PieChartRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import yuku.ambilwarna.AmbilWarnaDialog;

public class GraphActivity extends AppCompatActivity {

    private BarChart barChart;
    private Spinner typeSpinner;
    private DatabaseHelper databaseHelper;
    private DatabaseHelper2 databaseIncome;
    private ImageButton btnBack;
    private GestureDetector gestureDetector;

    private DecimalFormat df = new DecimalFormat("#.##");

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        barChart = findViewById(R.id.barChart);
        PieChart pieChart = findViewById(R.id.pieChart);
        typeSpinner = findViewById(R.id.typeSpinner);
        Spinner categorySpinner = findViewById(R.id.categorySpinner);
        Button switchChartButton = findViewById(R.id.switchChartButton);
        btnBack = findViewById(R.id.buttonBackFromGraf);

        databaseHelper = new DatabaseHelper(this);
        databaseIncome = new DatabaseHelper2(this);

        // Состояние: по умолчанию BarChart
        final boolean[] isPieChartVisible = {false};

        // Обработка выбора типа графика (дни/месяцы)
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isPieChartVisible[0]) {
                    if (position == 0) {
                        showMonthlyChart();
                    } else {
                        showDayChart();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Обработка выбора периода для PieChart
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isPieChartVisible[0]) {
                    showPieChart(pieChart, position + 1); // 0→1, 1→2, 2→3
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        pieChart.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false; // позволяем PieChart тоже обрабатывать события
        });


        // Переключение графиков
        switchChartButton.setOnClickListener(v -> {
            isPieChartVisible[0] = !isPieChartVisible[0];

            if (isPieChartVisible[0]) {
                // Показ PieChart
                pieChart.setVisibility(View.VISIBLE);
                barChart.setVisibility(View.GONE);
                categorySpinner.setVisibility(View.VISIBLE);
                typeSpinner.setVisibility(View.GONE);
                switchChartButton.setText("назад");

                categorySpinner.setSelection(0); // по умолчанию текущий месяц
                showPieChart(pieChart, 1);
            } else {
                // Показ BarChart
                pieChart.setVisibility(View.GONE);
                barChart.setVisibility(View.VISIBLE);
                categorySpinner.setVisibility(View.GONE);
                typeSpinner.setVisibility(View.VISIBLE);
                switchChartButton.setText("категории");

                int selectedType = typeSpinner.getSelectedItemPosition();
                if (selectedType == 0) {
                    showMonthlyChart();
                } else {
                    showDayChart();
                }
            }
        });

        // Назад
        btnBack.setOnClickListener(v -> {
            Intent intentGoBack = new Intent(GraphActivity.this, StartActivity.class);
            startActivity(intentGoBack);
        });

        // Запуск по умолчанию
        showMonthlyChart();
    }

    private void showPieChart(PieChart pieChart, int i) {
        FileHelper fileHelper = new FileHelper(this);
        List<CategoryItem> categories = fileHelper.getCategoriesWithPrices(this,i);
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        Map<Integer, Integer> savedColors = loadCategoryColors(this);

        CursData curs = CursHelper.getCursData(databaseIncome.getCurs());
        Random rnd = new Random();

        for (CategoryItem item : categories) {
            if (item.getPrice() > 0) {
                float value = (float) (item.getPrice() * curs.rate);
                entries.add(new PieEntry(value, item.getName()));

                // Цвет для категории
                int id = item.getId();
                int color;

                if (savedColors.containsKey(id)) {
                    color = savedColors.get(id);
                } else {
                    color = Color.rgb(100 + rnd.nextInt(150), 100 + rnd.nextInt(150), 100 + rnd.nextInt(150));
                    savedColors.put(id, color); // Сохраняем новый цвет
                }

                item.setColor(color); // На всякий случай
                colors.add(color);
            }
        }

        // Сохраняем обратно в SharedPreferences
        saveCategoryColors(savedColors, this);

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(0.8f); // минимальное расстояние между секторами
        dataSet.setSelectionShift(2f); // меньше выдвигается при выборе
        dataSet.setValueLinePart1Length(0.1f); // линия от центра
        dataSet.setValueLinePart2Length(0.1f); // линия до текста
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        // Форматирование
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (curs.symbol.equals("֏")) {
                    return formatPrice(Math.round(value)) + " " + curs.symbol;
                } else {
                    return formatPrice(value) + " " + curs.symbol;
                }
            }
        });

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setRenderer(new CustomPieChartRenderer(pieChart, pieChart.getAnimator(), pieChart.getViewPortHandler()));
        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.getDescription().setEnabled(false);

        pieChart.setDrawEntryLabels(true);
        pieChart.setEntryLabelColor(Color.YELLOW);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(getResources().getColor(R.color.my_darkbg)); // Центр — тёмный
        pieChart.setTransparentCircleRadius(0f);
        pieChart.setHoleRadius(90f); // Увеличим центр — сектора станут "уже"

        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(12f);

        // Клик по сектору
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry entry = (PieEntry) e;
                float val = e.getY();
                String value;
                if (curs.symbol.equals("֏")) {
                    value = formatPrice(Math.round(val)) + " " + curs.symbol;
                } else {
                    value = formatPrice(val) + " " + curs.symbol;
                }
                Toast.makeText(GraphActivity.this, entry.getLabel() + ": " + value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {}
        });

        pieChart.setExtraOffsets(24f, 24f, 24f, 24f);
        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);

        gestureDetector = new GestureDetector(pieChart.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Highlight highlight = pieChart.getHighlightByTouchPoint(e.getX(), e.getY());
                if (highlight != null) {
                    Entry entry = pieChart.getData().getDataSetByIndex(highlight.getDataSetIndex()).getEntryForIndex((int) highlight.getX());
                    if (entry instanceof PieEntry) {
                        String categoryName = ((PieEntry) entry).getLabel();

                        // Найти соответствующий CategoryItem
                        for (CategoryItem item : categories) {
                            if (item.getName().equals(categoryName)) {
                                showColorPickerDialog(pieChart.getContext(), item.getId(), pieChart, i);
                                break;
                            }
                        }
                    }
                }
                return true;
            }
        });



        pieChart.invalidate(); // Обновление
    }



    private void showMonthlyChart() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        List<MonthData> monthDataList = databaseHelper.getMonthData(db);

        // Сортируем по году и месяцу
        Collections.sort(monthDataList, new Comparator<MonthData>() {
            @Override
            public int compare(MonthData o1, MonthData o2) {
                String[] o1Parts = o1.getMonthName().split("_");
                String[] o2Parts = o2.getMonthName().split("_");

                int yearComparison = Integer.compare(Integer.parseInt(o1Parts[1]), Integer.parseInt(o2Parts[1]));
                if (yearComparison == 0) {
                    // Если года одинаковые, сортируем по месяцу
                    return Integer.compare(Integer.parseInt(o1Parts[2]), Integer.parseInt(o2Parts[2]));
                }
                return yearComparison;
            }
        });

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < monthDataList.size(); i++) {
            MonthData data = monthDataList.get(i);
            entries.add(new BarEntry(i, (float) data.getSpent()));
            String name = getName(data.getMonthName().split("_")[2]) + "  " + data.getMonthName().split("_")[1];
            labels.add(name);
        }

        drawChart(entries, labels, "Траты по месяцам", false, monthDataList);
    }


    private void showDayChart() {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        String[] weekDays = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        CursData currency = CursHelper.getCursData(databaseIncome.getDefaultCurrency());
        Calendar calendar = Calendar.getInstance();
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);


        for (int i = 0; i < daysInMonth; i++) {
            float spent = (float) databaseHelper.getDoneSpents(i,i);
            entries.add(new BarEntry(i, spent));
            labels.add(String.valueOf(i));
        }

        drawChart(entries, labels, "Траты по дням недели", true, null);

    }

    private void drawChart(List<BarEntry> entries, List<String> labels, String labelText, boolean days, @Nullable List<MonthData> monthDataList) {
        barChart.fitScreen(); // Сброс зума и позиции

        String currencySymbol = CursHelper.getCursData(databaseIncome.getCurs()).symbol;
        double rate = CursHelper.getCursData(databaseIncome.getCurs()).rate ;

        // Переводим значения, умножив на курс
        for (int i = 0; i < entries.size(); i++) {
            BarEntry entry = entries.get(i);
            entry.setY(entry.getY() * (float) rate);  // Умножаем на курс
        }

        BarDataSet dataSet = new BarDataSet(entries, labelText);
        dataSet.setColor(getResources().getColor(R.color.primary));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        dataSet.setValueFormatter(new CurrencyValueFormatter(currencySymbol, rate));

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.setDrawGridBackground(false);
        barChart.getDescription().setEnabled(false);

        // X Axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.YELLOW);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);
        xAxis.setDrawGridLines(false);

        if (!days) {
            xAxis.setLabelRotationAngle(-45f); // Повернуть текст на 45 градусов
            barChart.setVisibleXRangeMaximum(4); // Показываем до 4 месяцев на экране
        } else {
            barChart.setVisibleXRangeMaximum(7); // Показываем 7 дней на экране
            xAxis.setLabelRotationAngle(0f); // По дням — обычное выравнивание
        }

        // Y Axis
        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisRight().setEnabled(false);

        // Legend
        Legend legend = barChart.getLegend();
        legend.setTextColor(Color.WHITE);

        // Разрешаем скролл и зум
        barChart.setScaleEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setVisibleXRangeMaximum(7);

        if (days){
            int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            barChart.moveViewToX(today - 3);
        } else if (monthDataList != null) {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            String currentMonthKey = "month_" + year + "_" + (month < 10 ? "0" + month : month);

            for (int i = 0; i < monthDataList.size(); i++) {
                if (monthDataList.get(i).getMonthName().equals(currentMonthKey)) {
                    barChart.moveViewToX(i);
                    break;
                }
            }
        }
        barChart.setExtraBottomOffset(50f);
        barChart.invalidate();
    }

    private String formatPrice(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        DecimalFormat df = new DecimalFormat("#,##0.##", symbols);
        return df.format(value);
    }

    public static void saveCategoryColors(Map<Integer, Integer> map, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("category_colors", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            editor.putInt("color_" + entry.getKey(), entry.getValue());
        }
        editor.apply();
    }

    public static Map<Integer, Integer> loadCategoryColors(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("category_colors", Context.MODE_PRIVATE);
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < 100; i++) { // например 100 категорий максимум
            if (prefs.contains("color_" + i)) {
                map.put(i, prefs.getInt("color_" + i, Color.GRAY));
            }
        }
        return map;
    }

    public void showColorPickerDialog(Context context, int categoryId, PieChart pieChart, int i) {
        SharedPreferences prefs = context.getSharedPreferences("category_colors", Context.MODE_PRIVATE);
        int currentColor = prefs.getInt("color_" + categoryId, Color.GRAY);

        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(context, currentColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("color_" + categoryId, color);
                editor.apply();
                showPieChart(pieChart, i); // Обновляем график
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                // Ничего не делаем
            }
        });

        colorPicker.show();
    }



    public static void updateCategoryColor(int categoryId, int red, int green, int blue, Context context) {
        int color = Color.rgb(red, green, blue);
        SharedPreferences prefs = context.getSharedPreferences("category_colors", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("color_" + categoryId, color);
        editor.apply();
    }






    private String getName(String s) {
        int id = Integer.parseInt(s);
        String name = "";
        String[] months = {
                "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        };

        switch (id) {
            case 1:
                name = months[0]; // Январь
                break;
            case 2:
                name = months[1]; // Февраль
                break;
            case 3:
                name = months[2]; // Март
                break;
            case 4:
                name = months[3]; // Апрель
                break;
            case 5:
                name = months[4]; // Май
                break;
            case 6:
                name = months[5]; // Июнь
                break;
            case 7:
                name = months[6]; // Июль
                break;
            case 8:
                name = months[7]; // Август
                break;
            case 9:
                name = months[8]; // Сентябрь
                break;
            case 10:
                name = months[9]; // Октябрь
                break;
            case 11:
                name = months[10]; // Ноябрь
                break;
            case 12:
                name = months[11]; // Декабрь
                break;
            default:
                name = "Неизвестный месяц"; // Защита от некорректного ввода
        }

        return name;
    }
}

class CurrencyValueFormatter extends ValueFormatter {
    private String currencySymbol;
    private double rate;

    public CurrencyValueFormatter(String currencySymbol, double rate) {
        this.currencySymbol = currencySymbol;
        this.rate = rate;
    }

    @Override
    public String getFormattedValue(float value) {
        float convertedValue = value;

        // Выбираем формат
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');

        DecimalFormat formatter;
        if (currencySymbol.equals("֏")) { // Если драмы, без дробной части
            formatter = new DecimalFormat("#,###", symbols);
        } else {
            formatter = new DecimalFormat("#,##0.00", symbols);
        }

        return formatter.format(convertedValue) + " " + currencySymbol;
    }
}

class CustomPieChartRenderer extends PieChartRenderer {
    public CustomPieChartRenderer(PieChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
    }

    @Override
    public void drawValues(Canvas c) {
        super.drawValues(c); // рисует суммы как раньше

        PieData data = mChart.getData();
        float radius = mChart.getRadius();
        MPPointF center = mChart.getCenterCircleBox();
        float rotationAngle = mChart.getRotationAngle();

        float[] drawAngles = mChart.getDrawAngles();
        float[] absoluteAngles = mChart.getAbsoluteAngles();

        List<PieEntry> entries = data.getDataSet().getEntriesForXValue(0f);
        IPieDataSet dataSet = data.getDataSet();

        float angle = 0;

        for (int i = 0; i < dataSet.getEntryCount(); i++) {
            PieEntry entry = dataSet.getEntryForIndex(i);
            float sliceAngle = drawAngles[i];
            float valueAngle = angle + sliceAngle / 2f;

            float transformedAngle = rotationAngle + valueAngle;

            float percentage = entry.getValue() / data.getYValueSum() * 100;
            String percentText = String.format("%.0f%%", percentage);

            float x = (float) (center.x + (radius * 0.5f) * Math.cos(Math.toRadians(transformedAngle)));
            float y = (float) (center.y + (radius * 0.5f) * Math.sin(Math.toRadians(transformedAngle)));

            mValuePaint.setColor(Color.LTGRAY);
            mValuePaint.setTextSize(35f);
            mValuePaint.setColor(Color.YELLOW);
            mValuePaint.setTextAlign(Paint.Align.CENTER);

            c.drawText(percentText, x, y, mValuePaint);

            angle += sliceAngle;
        }
    }
}


