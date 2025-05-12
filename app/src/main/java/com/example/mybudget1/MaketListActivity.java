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
                        if (maket.getAmount() > 0) {
                            databaseHelper.saveNote(currentDate, "выполнен расход:\n" + maket.getName() + " - " + maket.getAmount() + cursd.symbol, "Spent", "add");
                        } else {
                            databaseHelper.saveNote(currentDate, "получен доход:\n" + maket.getName() + " - " + -1 * maket.getAmount() + cursd.symbol, "Income", "add");
                        }
                    } else{
                        databaseHelper.insertData(selectedDay, maket.getName(), -1 * maket.getAmount(), 0, true);
                        databaseIncome.addIncome(maket.getAmount());

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
                AlertDialog.Builder builder = new AlertDialog.Builder(MaketListActivity.this);
                builder.setTitle("Редактировать макет");

                final int[] selectedCategoryId = {maket.getCategory_id()};
                final int[] selectedType = maket.getType().equals("Spent") ? new int[]{0} : new int[]{1};

                LinearLayout layout = new LinearLayout(MaketListActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(50, 40, 50, 10);

                // Тип: расход/доход
                LinearLayout typeLayout = new LinearLayout(MaketListActivity.this);
                typeLayout.setOrientation(LinearLayout.HORIZONTAL);
                typeLayout.setPadding(0, 0, 0, 20);

                Button btnExpense = new Button(MaketListActivity.this);
                Button btnIncome = new Button(MaketListActivity.this);
                btnExpense.setText("Расход");
                btnIncome.setText("Доход");

                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                buttonParams.setMargins(10, 0, 10, 0);
                btnExpense.setLayoutParams(buttonParams);
                btnIncome.setLayoutParams(buttonParams);

                typeLayout.addView(btnExpense);
                typeLayout.addView(btnIncome);

                FileHelper fileHelper = new FileHelper(MaketListActivity.this);
                List<String> categories = fileHelper.getAllCategories();

                Spinner categorySpinner = new Spinner(MaketListActivity.this);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MaketListActivity.this, android.R.layout.simple_spinner_item, categories);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(adapter);
                categorySpinner.setPopupBackgroundResource(R.drawable.spinner_background_cyan);
                categorySpinner.setBackgroundResource(R.drawable.spinner_background_cyan);
                categorySpinner.setDropDownVerticalOffset(10);

                if (selectedCategoryId[0] > 0 && selectedCategoryId[0] <= categories.size()) {
                    categorySpinner.setSelection(selectedCategoryId[0] - 1);
                }

                categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedCategoryId[0] = position + 1;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

                Spinner currencySpinner = new Spinner(MaketListActivity.this);
                String[] currencies = {"֏", "$", "₽", "元", "€", "¥", "₾"};
                ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(MaketListActivity.this, android.R.layout.simple_spinner_item, currencies);
                currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                currencySpinner.setAdapter(currencyAdapter);
                currencySpinner.setBackgroundResource(R.drawable.spinner_background_cyan);
                currencySpinner.setPopupBackgroundResource(R.drawable.spinner_background_cyan);

                String currentCurrencySymbol = databaseIncome.getCurs();

                String selectedSymbol = currentCurrencySymbol;
                int defaultCurrencyPosition = 0;

                switch (selectedSymbol) {
                    case "dollar":
                        selectedSymbol = "$";
                        defaultCurrencyPosition = 1;
                        break;
                    case "rubli":
                        selectedSymbol = "₽";
                        defaultCurrencyPosition = 2;
                        break;
                    case "yuan":
                        selectedSymbol = "元";
                        defaultCurrencyPosition = 3;
                        break;
                    case "evro":
                        selectedSymbol = "€";
                        defaultCurrencyPosition = 4;
                        break;
                    case "jen":
                        selectedSymbol = "¥";
                        defaultCurrencyPosition = 5;
                        break;
                    case "lari":
                        selectedSymbol = "₾";
                        defaultCurrencyPosition = 6;
                        break;
                    case "dram":
                    default:
                        selectedSymbol = "֏";
                        defaultCurrencyPosition = 0;
                        break;
                }
                currencySpinner.setSelection(defaultCurrencyPosition);

                EditText nameEdit = new EditText(MaketListActivity.this);
                nameEdit.setHint("название");
                nameEdit.setText(maket.getName());
                nameEdit.setBackgroundResource(R.drawable.edit_text_style);
                nameEdit.setPadding(20, 20, 20, 20);

                EditText amountEdit = new EditText(MaketListActivity.this);
                amountEdit.setHint("Сумма");
                amountEdit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                amountEdit.setText(String.valueOf(maket.getAmount()));
                amountEdit.setBackgroundResource(R.drawable.edit_text_style);
                amountEdit.setPadding(20, 20, 20, 20);

                btnExpense.setOnClickListener(v -> {
                    selectedType[0] = 0;
                    animateButtonColor(btnExpense, ContextCompat.getColor(MaketListActivity.this, R.color.primary));
                    animateButtonColor(btnIncome, ContextCompat.getColor(MaketListActivity.this, R.color.my_darkbtn));
                    categorySpinner.setVisibility(View.VISIBLE);
                });

                btnIncome.setOnClickListener(v -> {
                    selectedType[0] = 1;
                    animateButtonColor(btnIncome, ContextCompat.getColor(MaketListActivity.this, R.color.primary));
                    animateButtonColor(btnExpense, ContextCompat.getColor(MaketListActivity.this, R.color.my_darkbtn));
                    categorySpinner.setVisibility(View.GONE);
                });

                if (maket.getType().equals("Spent")) {
                    btnExpense.performClick();
                } else {
                    btnIncome.performClick();
                }

                layout.addView(typeLayout);
                layout.addView(nameEdit);
                layout.addView(amountEdit);
                layout.addView(categorySpinner);
                layout.addView(currencySpinner);

                builder.setView(layout);

                SpannableString positiveButtonText = new SpannableString("Сохранить");
                positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(MaketListActivity.this, R.color.my_cyan)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                SpannableString negativeButtonText = new SpannableString("Отмена");
                negativeButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(MaketListActivity.this, R.color.my_cyan)), 0, negativeButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
                    String newName = nameEdit.getText().toString().trim();
                    String amountStr = amountEdit.getText().toString().trim();

                    if (!newName.isEmpty() && !amountStr.isEmpty()) {
                        double amount = Double.parseDouble(amountStr);

                        double finalAmount = amount;
                        // преобразование валюты при необходимости

                        if (selectedType[0] == 1) {
                            selectedCategoryId[0] = -1;
                        }

                        // обновление макета
                        DatabaseHelper dbHelper = new DatabaseHelper(MaketListActivity.this);
                        dbHelper.updateMaket(maket.getId(), selectedType[0], newName, finalAmount, selectedCategoryId[0]);
                        loadMakets();
                    }
                });

                builder.setNegativeButton(negativeButtonText, null);

                AlertDialog dialog = builder.create();
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
                dialog.show();
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
        List<String> categories = fileHelper.getAllCategories();

        Spinner categorySpinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setPopupBackgroundResource(R.drawable.spinner_background_cyan);
        categorySpinner.setBackgroundResource(R.drawable.spinner_background_cyan);
        categorySpinner.setDropDownVerticalOffset(10);
        categorySpinner.setSelection(categories.indexOf("прочее"));

        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        spinnerParams.setMargins(0, 20, 0, 20);
        categorySpinner.setLayoutParams(spinnerParams);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedCategoryId[0] = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        // Создаем Spinner для выбора валюты
        Spinner currencySpinner = new Spinner(this);
        String[] currencies = {"֏", "$", "₽", "元", "€", "¥", "₾"};
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currencyAdapter);

        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        String currentCurrencySymbol = databaseIncome.getCurs();

        int defaultCurrencyPosition;

        switch (currentCurrencySymbol) {
            case "dollar":
                defaultCurrencyPosition = 1;
                break;
            case "rubli":
                defaultCurrencyPosition = 2;
                break;
            case "yuan":
                defaultCurrencyPosition = 3;
                break;
            case "eur":
                defaultCurrencyPosition = 4;
                break;
            case "jen":
                defaultCurrencyPosition = 5;
                break;
            case "lari":
                defaultCurrencyPosition = 6;
                break;
            case "dram":
            default:
                defaultCurrencyPosition = 0;
                break;
        }

        currencySpinner.setSelection(defaultCurrencyPosition);

        currencySpinner.setBackgroundResource(R.drawable.spinner_background_cyan);
        currencySpinner.setPopupBackgroundResource(R.drawable.spinner_background_cyan);

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
        layout.addView(currencySpinner);

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

                String selectedCurrency = currencySpinner.getSelectedItem().toString();
                double finalAmount = 0;

                switch (selectedCurrency) {
                    case "֏": // Армянский драм
                        finalAmount = amount / CursHelper.getToDram();
                        break;
                    case "$": // Доллар США
                        finalAmount = amount / CursHelper.getToDollar();
                        break;
                    case "₽": // Российский рубль
                        finalAmount = amount / CursHelper.getToRub();
                        break;
                    case "元": // Китайский юань
                        finalAmount = amount / CursHelper.getToJuan();
                        break;
                    case "€": // Евро
                        finalAmount = amount / CursHelper.getToEur();
                        break;
                    case "¥": // Японская иена
                        finalAmount = amount / CursHelper.getToJen();
                        break;
                    case "₾": // Грузинский лари
                        finalAmount = amount / CursHelper.getToLari();
                        break;
                    default:
                        finalAmount = amount;
                        break;
                }

                finalAmount = Math.round(finalAmount * 100.0) / 100.0;

                if(selectedType[0] == 1){
                    selectedCategoryId[0] = -1;
                }
                databaseHelper.createMaket(selectedType[0], itemName, finalAmount , selectedCategoryId[0]);
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