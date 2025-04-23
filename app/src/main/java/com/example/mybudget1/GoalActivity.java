package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class GoalActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private GoalAdapter goalAdapter;
    private List<Goal> goalList;
    private ImageView imagePreview;
    private final Uri[] localSelectedImageUri = {null};
    private Uri selectedImageUri;
    private ActivityResultLauncher<String> getImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal);

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                Intent intentGoBack = new Intent(GoalActivity.this, StartActivity.class);
                startActivity(intentGoBack);
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerGoals);

        ImageButton btnBack = findViewById(R.id.buttonBackFromGoals);
        btnBack.setOnClickListener(v -> {
            Intent intentGoBack = new Intent(GoalActivity.this, StartActivity.class);
            startActivity(intentGoBack);
        });

        // Инициализация ActivityResultLauncher для получения изображения
        getImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                result -> {
                    if (result != null) {
                        // Сохраняем изображение во внутреннем хранилище
                        String savedPath = saveImageToInternalStorage(result);
                        if (savedPath != null) {
                            selectedImageUri = Uri.fromFile(new File(savedPath));  // Сохраняем URI изображения
                            // Если есть imagePreview в диалоге, устанавливаем выбранное изображение
                            imagePreview.setImageURI(selectedImageUri);
                        } else {
                            Toast.makeText(this, "Не удалось сохранить изображение", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        loadGoals();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        goalAdapter = new GoalAdapter(this, goalList, new GoalAdapter.OnGoalClickListener() {
            @Override
            public void onGoalDeleteClick(int position) {
                deleteGoal(goalList.get(position).getId(), position);
            }

            @Override
            public void onAddMoneyClick(int position) {
                Goal goal = goalList.get(position);
                showAddMoneyDialog(goal);
            }

            @Override
            public void onGoalEditClick(int position) {
                Goal goal = goalList.get(position);
                showEditGoalDialog(goal);
            }
        });
        recyclerView.setAdapter(goalAdapter);

        // Кнопка добавления новой цели
        Button addGoalButton = findViewById(R.id.buttonAddGoal);
        addGoalButton.setOnClickListener(v -> {
            selectedImageUri = null; // сбрасываем URI перед новым диалогом
            showAddGoalDialog();
        });
    }

    // Метод для сохранения изображения во внутреннем хранилище
    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            String fileName = "goal_" + System.currentTimeMillis() + ".jpg";
            File file = new File(getFilesDir(), fileName);
            OutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return file.getAbsolutePath(); // возвращаем путь для сохранения
        } catch (Exception e) {
            e.printStackTrace();
            return null; // если ошибка, возвращаем null
        }
    }

    private void showAddGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Добавить цель");

        // Инициализация диалога для добавления цели
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_goal, null);
        builder.setView(dialogView);

        EditText goalNameEditText = dialogView.findViewById(R.id.editGoalName);
        EditText goalAmountEditText = dialogView.findViewById(R.id.editGoalAmount);
        imagePreview = dialogView.findViewById(R.id.imagePreview);

        // Установка изображения по умолчанию
        imagePreview.setImageResource(R.drawable.default_goal);

        // Кнопка выбора изображения
        Button selectImageButton = dialogView.findViewById(R.id.buttonSelectImage);
        selectImageButton.setOnClickListener(v -> getImageLauncher.launch("image/*"));

        Spinner currencySpinner = dialogView.findViewById(R.id.currencySpinnernewGoal);

        String[] currencies = {"֏", "$", "₽"};

        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currencyAdapter);

        currencySpinner.setBackgroundResource(R.drawable.spinner_background_cyan);
        currencySpinner.setPopupBackgroundResource(R.drawable.spinner_background_cyan);

        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        String currentCurrencySymbol = databaseIncome.getCurs();
        int defaultCurrencyPosition = 0;

        switch (currentCurrencySymbol) {
            case "dollar":
                defaultCurrencyPosition = 1;
                break;
            case "rubli":
                defaultCurrencyPosition = 2;
                break;
            case "dram":
            default:
                defaultCurrencyPosition = 0;
                break;
        }

        currencySpinner.setSelection(defaultCurrencyPosition);

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String goalName = goalNameEditText.getText().toString().trim();
            String goalAmountStr = goalAmountEditText.getText().toString().trim();

            if (goalName.isEmpty() || goalAmountStr.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            double goalAmountinX = Double.parseDouble(goalAmountStr);

            String selectedCurrency = currencySpinner.getSelectedItem().toString();
            double goalAmount = 0;

            switch (selectedCurrency) {
                case "֏":
                    goalAmount = goalAmountinX / CursHelper.getToDram();
                    break;
                case "$":
                    goalAmount = goalAmountinX / CursHelper.getToDollar();
                    break;
                case "₽":
                    goalAmount = goalAmountinX / CursHelper.getToRub();
                    break;
            }

            goalAmount = Math.round(goalAmount * 100.0) / 100.0;

            // Если путь к изображению не был выбран, использовать картинку по умолчанию
            if (selectedImageUri == null) {
                selectedImageUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.default_goal);
            }

            // Добавляем цель в базу данных
            Goal goal = new Goal(goalName, goalAmount, 0, selectedImageUri.toString());
            long id = dbHelper.addGoal(goal);
            if (id != -1) {
                loadGoals();  // Обновляем список целей
                goalAdapter.notifyDataSetChanged();
                Intent intentGoBack = new Intent(GoalActivity.this, GoalActivity.class);
                finish();
                startActivity(intentGoBack);
                Toast.makeText(this, "Цель добавлена!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background); // Устанавливаем фон
        dialog.show();
    }

    // Загрузка целей из базы данных
    private void loadGoals() {
        goalList = dbHelper.getGoals();
    }



    private void deleteGoal(int goalId, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить цель?")
                .setMessage("Вы уверены, что хотите удалить эту цель?")
                .setPositiveButton("Да", (dialog, which) -> {
                    dbHelper.deleteGoal(goalId);
                    goalList.remove(position);
                    goalAdapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Цель удалена", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    private void showAddMoneyDialog(Goal goal) {
        if (goal.getCurrentAmount() >= goal.getAmount()){
            Toast.makeText(this, "Цель уже достигнута", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить деньги");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_money, null);
        builder.setView(dialogView);

        EditText amountEditText = dialogView.findViewById(R.id.editAmount);

        Spinner currencySpinner = dialogView.findViewById(R.id.currencySpinneraddGoal);

        String[] currencies = {"֏", "$", "₽", "元", "€", "¥", "₾"};

        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currencyAdapter);

        currencySpinner.setBackgroundResource(R.drawable.spinner_background_cyan);
        currencySpinner.setPopupBackgroundResource(R.drawable.spinner_background_cyan);

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

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String amountStr = amountEditText.getText().toString().trim();

            if (!amountStr.isEmpty()) {
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

                if (goal.getCurrentAmount() + finalAmount < 0){
                    Toast.makeText(this, "мы открываем бизнес мы будем делать бабки?", Toast.LENGTH_SHORT).show();
                    return;
                }

                double budget = databaseIncome.getBudget();
                if(budget < finalAmount){
                    Toast.makeText(this, "У вас недостаточно средств чтоб откладовать", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    goal.setCurrentAmount(goal.getCurrentAmount() + finalAmount);

                    databaseIncome.addSpent(finalAmount);
                    if (goal.getCurrentAmount() + finalAmount >= goal.getAmount()){
                        Toast.makeText(this, "Поздровляю вы достигли цели!", Toast.LENGTH_SHORT).show();
                        databaseIncome.addIncome(goal.getCurrentAmount() - goal.getAmount());
                        goal.setCurrentAmount(goal.getAmount());
                    }
                    dbHelper.updateGoal(goal);
                }

                // Обновляем адаптер, чтобы отобразить изменения
                goalAdapter.notifyDataSetChanged();

                Toast.makeText(this, "Деньги добавлены!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Введите сумму!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }


    private void showEditGoalDialog(Goal goal) {
        // Создаём диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Редактировать цель");

        // Инициализация диалога для редактирования
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_goal, null);
        builder.setView(dialogView);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText goalNameEditText = dialogView.findViewById(R.id.editGoalName);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText goalSumEditText = dialogView.findViewById(R.id.editGoalSum);

        Spinner currencySpinner = dialogView.findViewById(R.id.currencySpinnerGoal);

        String[] currencies = {"֏", "$", "₽"};

        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currencyAdapter);

        currencySpinner.setBackgroundResource(R.drawable.spinner_background_cyan);
        currencySpinner.setPopupBackgroundResource(R.drawable.spinner_background_cyan);

        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        String currentCurrencySymbol = databaseIncome.getCurs();
        int defaultCurrencyPosition = 0;

        switch (currentCurrencySymbol) {
            case "dollar":
                defaultCurrencyPosition = 1;
                break;
            case "rubli":
                defaultCurrencyPosition = 2;
                break;
            case "dram":
            default:
                defaultCurrencyPosition = 0;
                break;
        }

        currencySpinner.setSelection(defaultCurrencyPosition);


        goalNameEditText.setText(goal.getName());
        goalSumEditText.setText(String.valueOf(goal.getAmount()));

        // Инициализация imagePreview внутри диалога
        imagePreview = dialogView.findViewById(R.id.imagePreview);

        goalSumEditText.setFilters(new InputFilter[]{
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        String input = dest.subSequence(0, dstart)
                                + source.toString()
                                + dest.subSequence(dend, dest.length());

                        // Проверка формата: до 2-х цифр после запятой
                        if (input.matches("^\\d*\\.?\\d{0,2}$")) {
                            return null;
                        }

                        return "";
                    }
                }
        });

        // Устанавливаем текущее изображение
        if (goal.getImagePath() != null) {
            Glide.with(this)
                    .load(Uri.parse(goal.getImagePath()))  // Загрузка изображения с текущего пути
                    .placeholder(R.drawable.default_goal)  // Картинка по умолчанию
                    .into(imagePreview);
        }

        // Кнопка выбора нового изображения
        Button selectImageButton = dialogView.findViewById(R.id.buttonSelectImage);
        selectImageButton.setOnClickListener(v -> {
            // Запуск выбора изображения
            getImageLauncher.launch("image/*");
        });

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String newGoalName = goalNameEditText.getText().toString().trim();
            String newGoalSum = goalSumEditText.getText().toString().trim();
            double sum = Double.parseDouble(newGoalSum);

            if (newGoalName.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите название цели", Toast.LENGTH_SHORT).show();
                return;
            }

            // Сохраняем новый путь к изображению, если оно изменилось
            String newImagePath = selectedImageUri != null
                    ? selectedImageUri.toString()
                    : goal.getImagePath();

            // Обновляем цель в базе данных
            goal.setName(newGoalName);
            goal.setImagePath(newImagePath);

            String selectedCurrency = currencySpinner.getSelectedItem().toString();
            double finalAmount = 0;

            // Конвертируем сумму в выбранную валюту
            switch (selectedCurrency) {
                case "֏":
                    finalAmount = sum / CursHelper.getToDram();
                    break;
                case "$":
                    finalAmount = sum / CursHelper.getToDollar();
                    break;
                case "₽":
                    finalAmount = sum / CursHelper.getToRub();
                    break;
            }

            finalAmount = Math.round(finalAmount * 100.0) / 100.0;

            goal.setAmount(finalAmount);

            // Здесь обновляем данные в базе данных (например, через updateGoal())
            dbHelper.updateGoal(goal);

            Intent intentGoBack = new Intent(GoalActivity.this, GoalActivity.class);
            finish();
            startActivity(intentGoBack);

            Toast.makeText(this, "Цель обновлена!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

}

