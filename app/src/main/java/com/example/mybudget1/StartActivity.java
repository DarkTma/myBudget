package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StartActivity extends AppCompatActivity {
    private LinearLayout menuLayout;
    private Button btnOpenMenu, btnCloseMenu, btnExpandList;
    private ListView listView;
    private WeekItemAdapter adapter;
    private List<String> dataList;
    private boolean isExpanded = false;
    private Button btnIncome;
    private TextView spentText;
    public Button incomeText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);

        // Инициализация элементов
        menuLayout = findViewById(R.id.menuLayout);
        btnOpenMenu = findViewById(R.id.btnOpenMenu);
        btnCloseMenu = findViewById(R.id.btnCloseMenu);
        btnExpandList = findViewById(R.id.btnExpandList);
        incomeText = findViewById(R.id.tvIncome);
        btnIncome = findViewById(R.id.btnIncomeList);
        spentText = findViewById(R.id.tvSpent);
        listView = findViewById(R.id.listView);

        TextView podskazka = findViewById(R.id.textpodskazka);


        //закрытие менюшки
        LinearLayout menuLayout = findViewById(R.id.menuLayout);
        View dimLayer = findViewById(R.id.dimLayer);

        dimLayer.setOnClickListener(v -> {
            menuLayout.setVisibility(View.GONE);
            dimLayer.setVisibility(View.GONE);
        });

        Button btnIncomeActivityGo = findViewById(R.id.btnincomeData);
        btnIncomeActivityGo.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, IncomeActivity.class);
            startActivity(intent);
            finish();
        });


        int spent = databaseHelper.checkAllSpents();
        spentText.setText("расход: " + spent);
        int income = databaseIncome.getIncome();
        if (income == 0){
            incomeText.setText("доход: не указан \n нажмите чтоб указать");
            incomeText.setTextColor(Color.RED);
        } else {
            incomeText.setText("доход: " + income);
            if (income < spent){
                incomeText.setText("доход: " + income + "\n ваш доход превышивает траты");
                incomeText.setTextColor(Color.RED);
            }else {
                incomeText.setTextColor(Color.GREEN);
            }
        }



        incomeText.setOnClickListener(v -> setIncomeDialog());

        btnIncome.setOnClickListener(v -> showincomeList());



        // Данные для ListView
        List<WeekItem> dataList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int getCurrentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentDayIndex = getCurrentDay;
        int prevDay = currentDayIndex - 1;
        int nextDay = currentDayIndex + 1;

        String prevDayName = DayAdapter.getDayName(prevDay);
        String currentDayName = DayAdapter.getDayName(currentDayIndex);
        String nextDayName = DayAdapter.getDayName(nextDay);


        int prevDaySpent = databaseHelper.getDoneSpents(prevDay, prevDay);
        int prevDayMustDo = databaseHelper.getAllSpents(prevDay, prevDay);
        int todaySpent = databaseHelper.getDoneSpents(currentDayIndex, currentDayIndex);
        int todayMustDo = databaseHelper.getAllSpents(currentDayIndex, currentDayIndex);
        int nextDaySpent = databaseHelper.getDoneSpents(nextDay, nextDay);
        int nextDayMustDo = databaseHelper.getAllSpents(nextDay, nextDay);



        dataList.add(new WeekItem(prevDayName , "потрачено: " + prevDaySpent + "₽", "из: " + prevDayMustDo + "₽"));
        dataList.add(new WeekItem(currentDayName + " (сегодня)", "потрачено: " + todaySpent + "₽", "из: " + todayMustDo + "₽"));
        dataList.add(new WeekItem(nextDayName, "потрачено: " + nextDaySpent + "₽", "из: " + nextDayMustDo + "₽"));


        adapter = new WeekItemAdapter(this, dataList);
        listView.setAdapter(adapter);

        // Открыть меню
        btnOpenMenu.setOnClickListener(v -> {
            menuLayout.setVisibility(View.VISIBLE);
            dimLayer.setVisibility(View.VISIBLE);
        });

        // Закрыть меню
        btnCloseMenu.setOnClickListener(v -> {
            menuLayout.setVisibility(View.GONE);
            dimLayer.setVisibility(View.GONE);
        });

        // Расширение списка
        btnExpandList.setOnClickListener(v -> {
            if (isExpanded) {
                while (dataList.size() > 0) {
                    dataList.remove(dataList.size() - 1);
                }
                dataList.add(new WeekItem(prevDayName , "потрачено: " + prevDaySpent + "₽", "из: " + prevDayMustDo + "₽"));
                dataList.add(new WeekItem(currentDayName + " (сегодня)", "потрачено: " + todaySpent + "₽", "из: " + todayMustDo + "₽"));
                dataList.add(new WeekItem(nextDayName, "потрачено: " + nextDaySpent + "₽", "из: " + nextDayMustDo + "₽"));

                isExpanded = false;
                podskazka.setVisibility(View.GONE);
                listView.getLayoutParams().height -= 700; // Увеличиваем высоту
            } else {
                while (dataList.size() > 0) {
                    dataList.remove(dataList.size() - 1);
                }
                int day = DayAdapter.getStartOfWeek();
                String[] weekDays = {"понедельник", "вторник", "среда", "четверг" , "пятница", "суббота", "воскресение"};
                for (int i = 0; i < 7; i++) {
                    dataList.add(new WeekItem(weekDays[i] , "потрачено: " + databaseHelper.getDoneSpents(day+i, day+i)+"₽",
                            "из: " + databaseHelper.getAllSpents(day+i, day+i) + "₽"));
                }
                listView.getLayoutParams().height += 700; // Возвращаем высоту
                isExpanded = true;
                podskazka.setVisibility(View.VISIBLE);
            }
            adapter.notifyDataSetChanged();
        });



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Получаем выбранный элемент списка
                WeekItem selectedItem = (WeekItem) parent.getItemAtPosition(position);

                // Создаем Intent для перехода на новую активность
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                if (!isExpanded) {
                    int moneday = DayAdapter.getStartOfWeek();
                    int choosenDay = DayAdapter.findDayOfMonth(moneday, selectedItem.getDayName());
                    intent.putExtra("day", choosenDay);
                    intent.putExtra("isexpented", "false");
                }else {
                    intent.putExtra("day", position);
                    intent.putExtra("isexpented", "true");
                }

                // Запускаем новую активность
                startActivity(intent);
                finish();
            }
        });
    }

    private void showincomeList() {
        ArrayList<String> dataList = getIncomData();

        // Кастомный адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.income_item_forshow, R.id.tvItem, dataList);

        // Создаем ListView
        ListView listView = new ListView(this);
        listView.setAdapter(adapter);

        // Создаем AlertDialog с кастомным фоном
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(Html.fromHtml("<font color='#1EFF00'>Ваши доходы (чтоб изменить их зайдите в меню)</font>"));
        builder.setView(listView);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        // Создаем AlertDialog
        AlertDialog dialog = builder.create();

        // Устанавливаем фон из drawable
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

        // Показываем диалог
        dialog.show();
    }

    private ArrayList<String> getIncomData(){
        ArrayList<String> dataList = new ArrayList<>();
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        Cursor cursor = databaseIncome.getIncomeList();
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(1);
                int spent = cursor.getInt(2);
                int day = cursor.getInt(3);
                int once = cursor.getInt(4);
                String isOnce = "одноразовый";
                if (once == 1){
                    isOnce = "ежемесечный";
                }
                dataList.add("\n" + "данные дохода: " + name + " - " + spent + "₽" + "\n" + "день получения: " + day + " числа"  + "\n" + "тип: " + isOnce);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dataList;
    }

    private void setIncomeDialog() {

        TextView customTitle = new TextView(this);
        customTitle.setText("Добавить доход");
        customTitle.setTextSize(20);
        customTitle.setTextColor(ContextCompat.getColor(this, R.color.my_green));
        customTitle.setPadding(0, 20, 0, 20);
        customTitle.setGravity(Gravity.CENTER);

        EditText name = new EditText(this);
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        name.setHint("Название дохода");
        name.setPadding(0, 30, 0, 10); // Добавляем больше отступов
        name.setBackgroundResource(R.drawable.edit_text_style);

        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        nameParams.setMargins(0, 10, 0, 20); // Устанавливаем отступы
        name.setLayoutParams(nameParams);

        EditText income = new EditText(this);
        income.setInputType(InputType.TYPE_CLASS_NUMBER);
        income.setHint("доход");
        income.setPadding(0, 30, 0, 10); // Добавляем больше отступов
        income.setBackgroundResource(R.drawable.edit_text_style);

        LinearLayout.LayoutParams incomeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        incomeParams.setMargins(0, 10, 0, 20); // Устанавливаем отступы
        income.setLayoutParams(nameParams);

        EditText day = new EditText(this);
        day.setInputType(InputType.TYPE_CLASS_NUMBER);
        day.setHint("день получение(по умлч 1 число)");
        day.setPadding(0, 30, 0, 10); // Добавляем больше отступов
        day.setBackgroundResource(R.drawable.edit_text_style);

        LinearLayout.LayoutParams dayParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        dayParams.setMargins(0, 10, 0, 20); // Устанавливаем отступы
        day.setLayoutParams(nameParams);

        CheckBox checkBox = new CheckBox(this);
        checkBox.setText("ежемесячный доход?");
        checkBox.setTextColor(ContextCompat.getColor(this, R.color.my_green));
        checkBox.setChecked(true);
        checkBox.setButtonDrawable(R.drawable.checkbox_style);

        LinearLayout.LayoutParams checkBoxParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        checkBoxParams.setMargins(0, 20, 0, 20); // Устанавливаем отступы
        checkBox.setLayoutParams(checkBoxParams);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(name);
        layout.addView(income);
        layout.addView(day);
        layout.addView(checkBox);

        // Создаём AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        SpannableString positiveButtonText = new SpannableString("Добавить");
        positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.my_green)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableString negativeButtonText = new SpannableString("Отмена");
        negativeButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.my_green)), 0, negativeButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setTitle(Html.fromHtml("<font color='#00FF82'>Введите данные</font>"));
        builder.setView(layout)
                .setPositiveButton(positiveButtonText, (dialog, which) -> {
                    if (Integer.parseInt(income.getText().toString()) != 0){
                        String nameText = name.getText().toString();
                        if (nameText.equals("")){
                            nameText = "доход";
                        }

                        int incomeText = Integer.parseInt(income.getText().toString());

                        int dayText;
                        if (!day.getText().toString().equals("") && !day.getText().toString().matches("0")) {
                            dayText = Integer.parseInt(day.getText().toString());
                        }else {
                            dayText = 1;
                        }

                        boolean once = true;
                        if(!checkBox.isChecked()){
                            once = false;
                        }

                        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
                        databaseIncome.setIncome(incomeText, nameText, dayText, once);
                        refreshIncomeText();

                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "вы недобавили доход", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(negativeButtonText, null);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background); // Устанавливаем фон
        dialog.show();
    }

    private void refreshIncomeText() {
        DatabaseHelper2 databaseHelper2 = new DatabaseHelper2(this);
        int income = databaseHelper2.getIncome();
        incomeText.setText("доход: " + income);
    }

    private int getProcent(int day , int a){
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        int b = databaseHelper.getNotDoneSpents(day, day);
        if (a + b > 0) {
            double resultD = (a / (double)(a + b)) * 100;
            int result = (int) Math.round(resultD);
            return result;
        } else {
            if (databaseHelper.getAllSpents(day, day) == 0){
                return 100;
            } else {
                return 0;
            }
        }
    }


}

