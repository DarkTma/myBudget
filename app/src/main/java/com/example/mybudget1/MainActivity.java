package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private ViewPager2 monthViewPager;
    private int currentDay;
    private TextView selectedDayText;
    private int currentMonthOffset;
    private int currentDayIndex;
    private Button btnNewSpent;
    private Button otherSettings;
    private Button weekStats;
    private ImageButton btnBack;
    private int selectedDay;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnNewSpent = findViewById(R.id.btnNewSpent);

        btnNewSpent.setOnClickListener(v -> newSpent(this));

        viewPager = findViewById(R.id.viewPager);
        currentDay = getCurrentDay();

        selectedDayText = findViewById(R.id.selectedDayText);
        currentMonthOffset = 0;
        currentDayIndex = getCurrentDay() - 1;

        Intent intent = getIntent();
        int day = intent.getIntExtra("day",0);
        if (day == 0){
            Toast.makeText(this, "этот день не в этом месяце , в меню есть опция след. месяц", Toast.LENGTH_SHORT).show();
            Intent intent2 = new Intent(MainActivity.this , StartActivity.class);
            startActivity(intent2);
        }
        int choosenDay;
        String isExpented = intent.getStringExtra("isexpented");
        if (isExpented.equals("false")) {
            choosenDay = day;
        } else {
            choosenDay = DayAdapter.getStartOfWeek() + day;
            if (choosenDay <= 0){
                Toast.makeText(this, "этот день не в этом месяце , в меню есть опция след. месяц", Toast.LENGTH_SHORT).show();
                Intent intent2 = new Intent(MainActivity.this , StartActivity.class);
                startActivity(intent2);
            }
        }

        selectedDayText.setText("День " + choosenDay);

        int daysInMonth = getDaysInMonth(currentMonthOffset);
        viewPager.setAdapter(new DayAdapter(this, daysInMonth, currentMonthOffset));
        viewPager.setCurrentItem(choosenDay-1, false);
        selectedDay = choosenDay;


        // Обновляем текст при смене страницы
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onPageSelected(int position) {
                selectedDayText.setText("День " + (position + 1));
                selectedDay = position + 1;
            }
        });

        otherSettings = findViewById(R.id.otherbtn);
        otherSettings.setOnClickListener(v -> goToSettings());

        weekStats = findViewById(R.id.weekStats);
        weekStats.setOnClickListener(v -> showWeekStats(this));

        btnBack = findViewById(R.id.buttonBack);
        btnBack.setOnClickListener(v -> {
            Intent intentGoBack = new Intent(MainActivity.this, StartActivity.class);
            startActivity(intentGoBack);
        });

        monthViewPager = findViewById(R.id.monthSelectViewPager);
        monthViewPager.setAdapter(new MonthSelectAdapter(this, this::updateMonth));
        monthViewPager.setCurrentItem(1, false); // Ставим на текущий месяц

        monthViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateMonth(position - 1); // -1 = предыдущий, 0 = текущий, 1 = следующий
            }
        });

        selectedDayText.setOnClickListener(v -> showDayPicker());
    }

    private void showDayPicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + currentMonthOffset); // Выбираем нужный месяц
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Первый день месяца
        long minDate = calendar.getTimeInMillis(); // Минимальная дата

        int daysInMonth = getDaysInMonth(currentMonthOffset); // Узнаем количество дней в месяце
        calendar.set(Calendar.DAY_OF_MONTH, daysInMonth); // Последний день месяца
        long maxDate = calendar.getTimeInMillis(); // Максимальная дата

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDay = dayOfMonth;
            selectedDayText.setText("День " + selectedDay);
            viewPager.setCurrentItem(selectedDay - 1, true); // Меняем день в ViewPager
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), selectedDay); // По умолчанию текущий день

        datePickerDialog.getDatePicker().setMinDate(minDate);
        datePickerDialog.getDatePicker().setMaxDate(maxDate);

        datePickerDialog.show();
    }

    public int getoffset(){
        return currentMonthOffset;
    }

    private void updateMonth(int monthOffset) {
        currentMonthOffset = monthOffset;
        int daysInMonth = getDaysInMonth(currentMonthOffset);
        viewPager.setAdapter(new DayAdapter(this, daysInMonth, currentMonthOffset));
        viewPager.setCurrentItem(0, false);
        selectedDayText.setText("День 1");
    }

@SuppressLint("SetTextI18n")
private void showWeekStats(MainActivity mainActivity) {
    TextView customTitle = new TextView(this);
    customTitle.setText("Статистика недели");
    customTitle.setTextSize(20);
    customTitle.setTextColor(ContextCompat.getColor(this, R.color.my_green));
    customTitle.setPadding(0, 20, 0, 20);
    customTitle.setGravity(Gravity.CENTER);

    TextView doneStatsText = new TextView(this);
    doneStatsText.setTextSize(16);
    doneStatsText.setTextColor(ContextCompat.getColor(this, R.color.my_cyan));
    doneStatsText.setPadding(0, 10, 0, 10);
    doneStatsText.setGravity(Gravity.START);

    LinearLayout.LayoutParams donestats = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
    );
    donestats.setMargins(50, 10, 0, 20); // Устанавливаем отступы
    doneStatsText.setLayoutParams(donestats);

    TextView notDoneStatsText = new TextView(this);
    notDoneStatsText.setTextSize(16);
    notDoneStatsText.setTextColor(ContextCompat.getColor(this, R.color.my_red));
    notDoneStatsText.setPadding(0, 10, 0, 10);
    notDoneStatsText.setGravity(Gravity.START);

    LinearLayout.LayoutParams notdonestats = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
    );
    notdonestats.setMargins(50, 10, 0, 20); // Устанавливаем отступы
    notDoneStatsText.setLayoutParams(notdonestats);

    // Добавляем кастомизированный LinearLayout
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(20, 20, 20, 20);
    layout.addView(customTitle);
    layout.addView(doneStatsText);
    layout.addView(notDoneStatsText);


    doneStatsText.setText("выполненые траты: " + setStatsText(true));
    notDoneStatsText.setText("невыполненые траты: " + setStatsText(false));

    // Создаём кастомный AlertDialog
    AlertDialog.Builder builder = new AlertDialog.Builder(this);

    // Добавляем в диалог кастомное содержимое
    builder.setView(layout);
    builder.setCancelable(true); // Диалог можно закрыть по нажатию вне

    // Добавляем кнопки
    SpannableString positiveButtonText = new SpannableString("закрыть");
    positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.my_green)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    builder.setPositiveButton(positiveButtonText, (dialog, which) -> dialog.dismiss());

    // Создаём и показываем AlertDialog
    AlertDialog dialog = builder.create();
    dialog.getWindow().setBackgroundDrawableResource(R.drawable.btn_light_green); // Устанавливаем фон
    dialog.show();
}

    private String setStatsText(boolean donetext) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        int startOfWeek = DayAdapter.getStartOfWeek();
        int endOfWeek = DayAdapter.getEndOfWeek();
        String result = "";

        if (donetext) {
            result = String.valueOf(databaseHelper.getDoneSpents(startOfWeek, endOfWeek));
        }else {
            result += String.valueOf(databaseHelper.getNotDoneSpents(startOfWeek, endOfWeek));
        }

        return result;
    }


    private void goToSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void newSpent(MainActivity mainActivity) {
        // Переменная для хранения выбранной категории (индекс)
        final int[] selectedCategoryId = {0};
        final int[] offset = {0};

        // Создаём EditText с кастомным стилем для ввода названия
        EditText name = new EditText(this);
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        name.setHint("Название траты");
        name.setPadding(0, 30, 0, 10); // Добавляем больше отступов
        name.setBackgroundResource(R.drawable.edit_text_style);

        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        nameParams.setMargins(0, 10, 0, 20); // Устанавливаем отступы
        name.setLayoutParams(nameParams);

        // Сумма
        EditText spent = new EditText(this);
        spent.setInputType(InputType.TYPE_CLASS_NUMBER);
        spent.setHint("Сумма");
        spent.setPadding(0, 20, 0, 20);
        spent.setBackgroundResource(R.drawable.edit_text_style);

        LinearLayout.LayoutParams spentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        spentParams.setMargins(0, 20, 0, 20); // Устанавливаем отступы
        spent.setLayoutParams(spentParams);

        CheckBox checkBox = new CheckBox(this);
        checkBox.setText("Выполнена?");
        checkBox.setTextColor(ContextCompat.getColor(this, R.color.my_cyan));
        checkBox.setChecked(true);
        checkBox.setButtonDrawable(R.drawable.checkbox_style);

        LinearLayout.LayoutParams checkBoxParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        checkBoxParams.setMargins(0, 20, 0, 20); // Устанавливаем отступы
        checkBox.setLayoutParams(checkBoxParams);


        // Выбор дня (календарь)
        final TextView dayTextView = new TextView(this);
        dayTextView.setText("Выберите день: " + selectedDay);  // Изначально показываем выбранный день
        dayTextView.setPadding(0, 20, 0, 20);
        dayTextView.setTextColor(ContextCompat.getColor(this, R.color.my_cyan));
        dayTextView.setTextSize(18);
        dayTextView.setOnClickListener(v -> {
            // Создаём диалог выбора дня
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH);  // Текущий месяц
            int currentYear = calendar.get(Calendar.YEAR);    // Текущий год
            int prevMonth = (currentMonth - 1 + 12) % 12;  // Предыдущий месяц
            int nextMonth = (currentMonth + 1) % 12;  // Следующий месяц

            // Устанавливаем минимальную и максимальную дату для выбора
            calendar.set(currentYear, prevMonth, 1); // Предыдущий месяц
            long minDate = calendar.getTimeInMillis();

            calendar.set(currentYear, nextMonth, 1); // Следующий месяц
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            long maxDate = calendar.getTimeInMillis();

            // Устанавливаем DatePickerDialog с ограничениями на месяц
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        // Обновляем день, если пользователь выбрал новый
                        offset[0] = monthOfYear - 2;
                        selectedDay = dayOfMonth;
                        dayTextView.setText("Выбран день: " + selectedDay); // Обновляем отображаемый текст
                    },
                    currentYear, currentMonth, selectedDay);

            // Устанавливаем минимальную и максимальную дату
            datePickerDialog.getDatePicker().setMinDate(minDate);
            datePickerDialog.getDatePicker().setMaxDate(maxDate);

            try {
                // Используем reflection для скрытия месяцев и года
                int monthId = Resources.getSystem().getIdentifier("month", "id", "android");
                int yearId = Resources.getSystem().getIdentifier("year", "id", "android");
                View monthView = datePickerDialog.getDatePicker().findViewById(monthId);
                View yearView = datePickerDialog.getDatePicker().findViewById(yearId);

                if (monthView != null) monthView.setVisibility(View.GONE);
                if (yearView != null) yearView.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace(); // Игнорируем ошибку в случае неудачной кастомизации
            }

            // Показываем диалог
            datePickerDialog.show();
        });

        // Загрузим категории из файла
        FileHelper fileHelper = new FileHelper(this);
        List<String> categories = fileHelper.readCategoriesFromFile(); // Чтение категорий
        Spinner categorySpinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Устанавливаем дефолтную категорию
        categorySpinner.setSelection(categories.indexOf("other"));

        // Обработчик выбора категории
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Сохраняем выбранную категорию
                selectedCategoryId[0] = position;  // Записываем выбранный индекс категории
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        // Размещаем элементы в LinearLayout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(name);
        layout.addView(spent);
        layout.addView(dayTextView); // Добавляем TextView с календарем
        layout.addView(categorySpinner); // Добавляем Spinner с категориями

        // Создаём AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        SpannableString positiveButtonText = new SpannableString("Добавить");
        positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.my_cyan)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableString negativeButtonText = new SpannableString("Отмена");
        negativeButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.my_cyan)), 0, negativeButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.setTitle(Html.fromHtml("<font color='#00FF82'>Введите данные</font>"));
        builder.setView(layout)
                .setPositiveButton(positiveButtonText, (dialog, which) -> {
                    String nameData = name.getText().toString().trim();
                    String spentDataStr = spent.getText().toString().trim();

                    int dayData = selectedDay;  // Используем выбранный день
                    Calendar calendar = Calendar.getInstance();
                    int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

                        if (nameData.isEmpty()) nameData = "Трата";
                        if (!spentDataStr.isEmpty() && Integer.parseInt(spentDataStr) != 0) {
                            int spentData = Integer.parseInt(spentDataStr);
                            boolean isDone = checkBox.isChecked();

                            // Используем выбранную категорию
                            String selectedCategory = categories.get(selectedCategoryId[0]);

                            DatabaseHelper databaseHelper = new DatabaseHelper(mainActivity);
                            DatabaseHelper2 databaseIncome = new DatabaseHelper2(mainActivity);
                            databaseHelper.insertData(dayData, nameData, spentData, offset[0], isDone , selectedCategoryId[0]); // Вставляем с id категории

                            if (isDone){
                                databaseIncome.addSpent(spentData);
                            }

                            updateAdapter();
                    }
                })
                .setNegativeButton(negativeButtonText, null);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background); // Устанавливаем фон
        dialog.show();
    }









    public void updateAdapter() {
        int daysInMonth = getDaysInMonth(currentMonthOffset);
        viewPager.setAdapter(new DayAdapter(this, daysInMonth, currentMonthOffset));
        viewPager.setCurrentItem( selectedDay - 1, false);  // Устанавливаем нужный день после обновления
    }

    private int getDaysInMonth(int monthOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, monthOffset);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private int getCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_MONTH);
    }


}


class DayAdapter extends FragmentStateAdapter {
    private final int daysInMonth;
    private final int monthOffset; // -1 (предыдущий месяц), 0 (текущий), 1 (следующий)

    public DayAdapter(@NonNull AppCompatActivity fragmentActivity, int daysInMonth, int monthOffset) {
        super(fragmentActivity);
        this.daysInMonth = daysInMonth;
        this.monthOffset = monthOffset;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        int day = position + 1; // Дни начинаются с 1
        return DayFragment.newInstance(day, monthOffset);
    }

    @Override
    public int getItemCount() {
        return daysInMonth;
    }

    public static int getStartOfWeek() {
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_MONTH); // Текущий день месяца
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // День недели (1 - воскресенье, 2 - понедельник, ..., 7 - суббота)

        // Определяем сдвиг назад до понедельника (если сегодня воскресенье, отнимаем 6)
        int daysToMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : (dayOfWeek - Calendar.MONDAY);
        return today - daysToMonday; // Номер понедельника
    }

    public static int getEndOfWeek() {
        return getStartOfWeek() + 6; // Воскресенье - на 6 дней после понедельника
    }

    public static int findDayOfMonth(int mondayDay, String targetDay) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Получаем текущий месяц и год
            LocalDate now = LocalDate.now();
            int month = now.getMonthValue();
            int year = now.getYear();

            // Преобразуем название дня в DayOfWeek
            DayOfWeek targetDayOfWeek = getDayOfWeek(targetDay);
            if (targetDayOfWeek == null) {
                throw new IllegalArgumentException("Неверное название дня: " + targetDay);
            }

            // Определяем дату понедельника
            LocalDate mondayDate = LocalDate.of(year, month, mondayDay);

            // Находим сдвиг до целевого дня недели
            int shift = targetDayOfWeek.getValue() - DayOfWeek.MONDAY.getValue();

            // Вычисляем целевую дату
            LocalDate targetDate = mondayDate.plusDays(shift);

            // Проверяем, что дата принадлежит текущему месяцу
            return targetDate.getMonthValue() == month ? targetDate.getDayOfMonth() : -1;
        }
        return 1;
    }

    private static DayOfWeek getDayOfWeek(String day) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (day.equalsIgnoreCase("понедельник")) return DayOfWeek.MONDAY;
            if (day.equalsIgnoreCase("вторник")) return DayOfWeek.TUESDAY;
            if (day.equalsIgnoreCase("среда")) return DayOfWeek.WEDNESDAY;
            if (day.equalsIgnoreCase("четверг")) return DayOfWeek.THURSDAY;
            if (day.equalsIgnoreCase("пятница")) return DayOfWeek.FRIDAY;
            if (day.equalsIgnoreCase("суббота")) return DayOfWeek.SATURDAY;
            if (day.equalsIgnoreCase("воскресенье")) return DayOfWeek.SUNDAY;
            return null;
        }
        return null;
    }

    public static String getDayName(int day){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, day);
        SimpleDateFormat sdf3 = new SimpleDateFormat("EEEE", new Locale("ru"));
        String dayName = sdf3.format(calendar.getTime());
        return  dayName;
    }
}

class MonthSelectAdapter extends RecyclerView.Adapter<MonthSelectAdapter.MonthViewHolder> {
    private final String[] monthNames = {"Предыдущий месяц", "Текущий месяц", "Следующий месяц"};

    private final Context context;

    public interface OnMonthChangeListener {
        void onMonthChanged(int offset);
    }

    private final OnMonthChangeListener listener;

    public MonthSelectAdapter(Context context, OnMonthChangeListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_month_select, parent, false);
        return new MonthViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
        holder.textView.setText(monthNames[position]);
    }

    @Override
    public int getItemCount() {
        return monthNames.length;
    }

    public class MonthViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public MonthViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.monthText);
        }
    }
}
