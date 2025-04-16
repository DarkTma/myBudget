package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
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
    private String monthName;
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
        int day = intent.getIntExtra("day",-1);
        if (day == -1){
            Toast.makeText(this, "этот день не в этом месяце , пожалуйста передите в ручную", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "этот день не в этом месяце ,  пожалуйста передите в ручную", Toast.LENGTH_SHORT).show();
                Intent intent2 = new Intent(MainActivity.this , StartActivity.class);
                startActivity(intent2);
            }
        }

        monthName = getMonthName(currentMonthOffset);
        selectedDayText.setText(choosenDay + " " + monthName);

        int daysInMonth = getDaysInMonth(currentMonthOffset);
        viewPager.setAdapter(new DayAdapter(this, daysInMonth, currentMonthOffset));
        viewPager.setCurrentItem(choosenDay-1, false);
        selectedDay = choosenDay;


        // Обновляем текст при смене страницы
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onPageSelected(int position) {
                selectedDayText.setText((position + 1) + " " + monthName);
                selectedDay = position + 1;
            }
        });

        btnBack = findViewById(R.id.buttonBack);
        btnBack.setOnClickListener(v -> {
            Intent intentGoBack = new Intent(MainActivity.this, StartActivity.class);
            startActivity(intentGoBack);
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                Intent intentGoBack = new Intent(MainActivity.this, StartActivity.class);
                startActivity(intentGoBack);
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

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

    public String getMonthName(int monthOffset) {
        Calendar calendar = Calendar.getInstance();

        // Получаем текущий месяц и добавляем смещение
        calendar.add(Calendar.MONTH, monthOffset);

        // Форматируем дату, чтобы получить название месяца
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", java.util.Locale.getDefault());

        return monthFormat.format(calendar.getTime());
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
        monthName = getMonthName(currentMonthOffset);
        selectedDayText.setText( "1 " + monthName );
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
        name.setTextColor(Color.WHITE);
        name.setHintTextColor(Color.WHITE);
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
        spent.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        spent.setHint("Сумма");
        spent.setTextColor(Color.WHITE);
        spent.setHintTextColor(Color.WHITE);
        spent.setPadding(0, 20, 0, 20);
        spent.setBackgroundResource(R.drawable.edit_text_style);

        LinearLayout.LayoutParams spentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        spentParams.setMargins(0, 20, 0, 20); // Устанавливаем отступы
        spent.setLayoutParams(spentParams);

        spent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String text = charSequence.toString();
                if (text.contains(".")) {
                    // Разрешаем только одну точку
                    if (text.indexOf(".", text.indexOf(".") + 1) != -1) {
                        spent.setText(text.substring(0, text.lastIndexOf(".")));
                        spent.setSelection(spent.getText().length()); // Устанавливаем курсор в конец
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        Spinner currencySpinner = new Spinner(this);
        String[] currencies = {"֏", "$", "₽"};
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        currencySpinner.setAdapter(currencyAdapter);
        currencySpinner.setBackgroundResource(R.drawable.spinner_background_cyan);
        currencySpinner.setPopupBackgroundResource(R.drawable.spinner_background_cyan);

// Получаем текущую валюту из базы данных
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        String currentCurrencySymbol = databaseIncome.getCurs(); // Это возвращает символ валюты (например, "dram", "dollar", "rubli")

// Определяем валюту по символу
        int defaultCurrencyPosition = 0; // Изначально установим на 0 (например, драм)
        final String[] selectedSymbol = {""}; // Строка для текущей валюты

// Устанавливаем позицию в Spinner в зависимости от текущей валюты
        switch (currentCurrencySymbol) {
            case "dollar":
                selectedSymbol[0] = "$";
                defaultCurrencyPosition = 1;
                break;
            case "rubli":
                selectedSymbol[0] = "₽";
                defaultCurrencyPosition = 2;
                break;
            case "dram":
            default:
                selectedSymbol[0] = "֏";
                defaultCurrencyPosition = 0;
                break;
        }

        // Устанавливаем выбранную валюту в Spinner
        currencySpinner.setSelection(defaultCurrencyPosition); // Устанавливаем валюту по умолчанию

        // Обработчик выбора валюты
        int finalDefaultCurrencyPosition = defaultCurrencyPosition;
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Обновляем выбранную валюту на основе выбранной позиции в Spinner
                selectedSymbol[0] = currencies[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Если ничего не выбрано, можно оставить по умолчанию
                selectedSymbol[0] = currencies[finalDefaultCurrencyPosition];
            }
        });

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

        offset[0] = currentMonthOffset;

        // Выбор дня (календарь)
        final TextView dayTextView = new TextView(this);
        dayTextView.setText("день: " + selectedDay + " " + monthName);  // Изначально показываем выбранный день
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
                        offset[0] = (monthOfYear < currentMonth) ? -1 : (monthOfYear > currentMonth) ? 1 : 0;
                        selectedDay = dayOfMonth;
                        dayTextView.setText("день: " + selectedDay + " " + monthName); // Обновляем отображаемый текст
                    },
                    currentYear, currentMonth + currentMonthOffset, selectedDay);

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

// Создаем кастомный Spinner с нашим стилем
        Spinner categorySpinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

// Применяем стиль для спиннера
        categorySpinner.setPopupBackgroundResource(R.drawable.spinner_background_cyan); // фоновое изображение для выпадающего списка
        categorySpinner.setSelection(categories.indexOf("other")); // Устанавливаем дефолтную категорию

// Устанавливаем кастомный стиль для спиннера
        categorySpinner.setBackgroundResource(R.drawable.spinner_background_cyan);
        categorySpinner.setDropDownVerticalOffset(10); // Отступ вниз для выпадающего списка

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
        layout.addView(currencySpinner);
        layout.addView(dayTextView);
        layout.addView(categorySpinner);
        layout.addView(checkBox);

        // Создаём AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        SpannableString positiveButtonText = new SpannableString("Добавить");
        positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.my_cyan)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableString negativeButtonText = new SpannableString("Отмена");
        negativeButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.my_cyan)), 0, negativeButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.setTitle(Html.fromHtml("<font color='#00FF82'>Введите данные</font>"));
        String finalSelectedSymbol = selectedSymbol[0];
        builder.setView(layout)
                .setPositiveButton(positiveButtonText, (dialog, which) -> {
                    String nameData = name.getText().toString().trim();
                    String spentDataStr = spent.getText().toString().replaceAll("[^\\d.]", ""); // Убираем все, кроме цифр и точки
                    double newitemSpent = 0;

                    try {
                        newitemSpent = Double.parseDouble(spentDataStr);  // Преобразуем строку в double
                    } catch (NumberFormatException e) {
                        // Обработка ошибки, если ввод некорректен
                        e.printStackTrace();
                    }

                    // Переменная для финальной суммы
                    double finalAmount = 0;

                    double valueInX = newitemSpent;
                    valueInX = Math.round(valueInX * 100.0) / 100.0;

                    // Конвертируем введенную сумму в нужную валюту
                    if (selectedSymbol[0].equals("֏")) { // Если текущая валюта "dram"
                        finalAmount = valueInX / CursHelper.getToDram(); // Преобразуем в драм
                    } else if (selectedSymbol[0].equals("$")) { // Если текущая валюта "dollar"
                        finalAmount = valueInX / CursHelper.getToDollar(); // Конвертируем в доллары
                    } else if (selectedSymbol[0].equals("₽")) { // Если текущая валюта "rubli"
                        finalAmount = valueInX / CursHelper.getToRub(); // Конвертируем в рубли
                    }


                    int dayData = selectedDay;  // Используем выбранный день

                    if (nameData.isEmpty()) nameData = "Трата";
                    if (!spentDataStr.isEmpty() && Double.parseDouble(spentDataStr) != 0) {
                        boolean isDone = checkBox.isChecked();

                        // Вставляем данные в базу
                        DatabaseHelper databaseHelper = new DatabaseHelper(mainActivity);
                        databaseHelper.insertData(dayData, nameData, finalAmount, offset[0], isDone, selectedCategoryId[0]); // Вставляем с id категории

                        if (isDone) {
                            databaseIncome.addSpent(finalAmount); // Добавляем в доходы, если отметка стоит
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
