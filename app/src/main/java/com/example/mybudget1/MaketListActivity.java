package com.example.mybudget1;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.opengl.Visibility;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MaketListActivity extends AppCompatActivity {

    private RecyclerView maketRecyclerView;
    private MaketAdapter adapter;
    private List<Maket> maketList = new ArrayList<>();
    private DatabaseHelper databaseHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.makets_activity);

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                Intent intentGoBack = new Intent(MaketListActivity.this, StartActivity.class);
                startActivity(intentGoBack);
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        ImageButton btnGoBack = findViewById(R.id.buttonBackFromMakets);
        btnGoBack.setOnClickListener(v -> {
            Intent intentGoBack = new Intent(MaketListActivity.this, StartActivity.class);
            startActivity(intentGoBack);

        });

        databaseHelper = new DatabaseHelper(this);
        maketRecyclerView = findViewById(R.id.recyclerViewMakets);
        maketRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        maketRecyclerView.setHasFixedSize(true);

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);

        adapter = new MaketAdapter(this, maketList, new MaketAdapter.OnMaketActionListener() {
            @Override
            public void onCheck(Maket maket) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH); // 0-11
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(MaketListActivity.this, null, year, month, day);

                // Ограничиваем выбор только текущим месяцем
                Calendar minDate = Calendar.getInstance();
                minDate.set(Calendar.DAY_OF_MONTH, 1);

                Calendar maxDate = Calendar.getInstance();
                maxDate.set(Calendar.DAY_OF_MONTH, maxDate.getActualMaximum(Calendar.DAY_OF_MONTH));

                datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
                datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

                // Меняем кнопку и заголовок вручную через AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MaketListActivity.this);
                builder.setTitle("Выберите день");

                builder.setView(datePickerDialog.getDatePicker());

                builder.setPositiveButton("ОК", (dialog, which) -> {
                    int selectedDay = datePickerDialog.getDatePicker().getDayOfMonth();
                    Toast.makeText(MaketListActivity.this, "Выбран день: " + selectedDay, Toast.LENGTH_SHORT).show();
                    if (maket.getType().equals("Spent")){
                        databaseHelper.insertData(selectedDay, maket.getName(), maket.getAmount(), 0, true, maket.getCategory_id());
                        databaseIncome.addSpent(maket.getAmount());
                        DatabaseHelper databaseHelper = new DatabaseHelper(MaketListActivity.this);
                        CursData cursd = CursHelper.getCursData(databaseIncome.getDefaultCurrency());
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                        String currentDate = sdf.format(new Date());
                        databaseHelper.saveNote(currentDate, "выполнен расход:\n" + maket.getName() + " - " + maket.getAmount() + cursd.symbol , "Spent", "add" );
                    } else{
                        databaseIncome.setIncome(maket.getAmount() , maket.getName() , selectedDay ,false);

                        databaseIncome.setIncomeGiven(maket.getName() , selectedDay);
                        databaseIncome.addIncome(maket.getAmount());
                        databaseIncome.deactivateIncome(maket.getName() , selectedDay);

                        DatabaseHelper databaseHelper = new DatabaseHelper(MaketListActivity.this);
                        CursData cursd = CursHelper.getCursData(databaseIncome.getDefaultCurrency());
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                        String currentDate = sdf.format(new Date());
                        databaseHelper.saveNote(currentDate, "получен доход:\n" + maket.getName() + " - " + maket.getAmount() + cursd.symbol , "Income", "add" );
                    }
                });

                builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

                builder.show();
            }


            @Override
            public void onEdit(Maket maket) {
                // TODO: открыть диалог редактирования
                Toast.makeText(MaketListActivity.this, "✏️ " + maket.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDelete(Maket maket) {
                databaseHelper.deleteMaket(maket.getId());
                loadMakets();
            }
        });

        maketRecyclerView.setAdapter(adapter);

        Button addMaketButton = findViewById(R.id.btnAddMaket);
        addMaketButton.setOnClickListener(v -> showAddMaketDialog());

        loadMakets();
    }

    private void loadMakets() {
        maketList.clear();
        maketList.addAll(databaseHelper.getAllMakets());
        adapter.notifyDataSetChanged();
    }

    private void showAddMaketDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить макет");

        final int[] selectedCategoryId = {0};
        final int[] selectedType = {0}; // 0 - расход, 1 - доход

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Горизонтальный контейнер для кнопок
        LinearLayout typeLayout = new LinearLayout(this);
        typeLayout.setOrientation(LinearLayout.HORIZONTAL);
        typeLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        typeLayout.setPadding(0, 0, 0, 20);

        // Кнопки
        Button btnExpense = new Button(this);
        Button btnIncome = new Button(this);
        btnExpense.setText("Расход");
        btnIncome.setText("Доход");

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        );
        buttonParams.setMargins(10, 0, 10, 0);
        btnExpense.setLayoutParams(buttonParams);
        btnIncome.setLayoutParams(buttonParams);

        // Начальный стиль
        btnExpense.setBackgroundResource(R.drawable.button_primary);
        btnIncome.setBackgroundResource(R.drawable.button_black);
        btnExpense.setTextColor(Color.WHITE);
        btnIncome.setTextColor(Color.WHITE);

        typeLayout.addView(btnExpense);
        typeLayout.addView(btnIncome);

        // Spinner
        FileHelper fileHelper = new FileHelper(this);
        List<String> categories = fileHelper.readCategoriesFromFile();

        Spinner categorySpinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setPopupBackgroundResource(R.drawable.spinner_background_cyan);
        categorySpinner.setBackgroundResource(R.drawable.spinner_background_cyan);
        categorySpinner.setDropDownVerticalOffset(10);
        categorySpinner.setSelection(categories.indexOf("other"));

        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        spinnerParams.setMargins(0, 20, 0, 20);
        categorySpinner.setLayoutParams(spinnerParams);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedCategoryId[0] = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        // Анимация смены цвета

        btnExpense.setOnClickListener(v -> {
            selectedType[0] = 0;
            animateButtonColor(btnExpense, ContextCompat.getColor(this, R.color.primary));
            animateButtonColor(btnIncome, ContextCompat.getColor(this, R.color.my_darkbtn));
            categorySpinner.setVisibility(View.VISIBLE);
        });

        btnIncome.setOnClickListener(v -> {
            selectedType[0] = 1;
            animateButtonColor(btnIncome, ContextCompat.getColor(this, R.color.primary));
            animateButtonColor(btnExpense, ContextCompat.getColor(this, R.color.my_darkbtn));
            categorySpinner.setVisibility(View.GONE);
        });


        // Название
        EditText name = new EditText(this);
        name.setHint("название");
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        name.setBackgroundResource(R.drawable.edit_text_style);
        name.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        nameParams.setMargins(0, 20, 0, 20);
        name.setLayoutParams(nameParams);

        // Сумма
        EditText amountEdit = new EditText(this);
        amountEdit.setHint("Сумма");
        amountEdit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        amountEdit.setBackgroundResource(R.drawable.edit_text_style);
        amountEdit.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams amountParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        amountParams.setMargins(0, 0, 0, 20);
        amountEdit.setLayoutParams(amountParams);

        // Добавляем всё
        layout.addView(typeLayout);
        layout.addView(name);
        layout.addView(amountEdit);
        layout.addView(categorySpinner);

        builder.setView(layout);

        SpannableString positiveButtonText = new SpannableString("Добавить");
        positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.my_cyan)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableString negativeButtonText = new SpannableString("Отмена");
        negativeButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.my_cyan)), 0, negativeButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
            String itemName = name.getText().toString().trim();
            String amountStr = amountEdit.getText().toString().trim();

            if (!itemName.isEmpty() && !amountStr.isEmpty()) {
                double amount = Double.parseDouble(amountStr);
                if(selectedType[0] == 1){
                    selectedCategoryId[0] = -1;
                }
                databaseHelper.createMaket(selectedType[0], itemName, amount , selectedCategoryId[0]);
                loadMakets();
            }
        });

        builder.setNegativeButton(negativeButtonText, null);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();
    }

    // Анимация смены цвета
    private void animateButtonColor(Button button, int toColor) {
        Drawable background = button.getBackground();

        if (background instanceof GradientDrawable) {
            GradientDrawable drawable = (GradientDrawable) background;

            int fromColor = Color.BLACK;
            try {
                // Попробуем получить текущий цвет, если задан
                fromColor = ((GradientDrawable) background).getColor().getDefaultColor();
            } catch (Exception ignored) {}

            // Создаем анимацию
            ValueAnimator colorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
            colorAnim.setDuration(300);
            colorAnim.addUpdateListener(animator -> {
                int animatedColor = (int) animator.getAnimatedValue();
                drawable.setColor(animatedColor);
            });
            colorAnim.start();
        }
    }



}