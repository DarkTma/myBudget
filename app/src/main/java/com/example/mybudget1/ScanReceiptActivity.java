package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ScanReceiptActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> galleryLauncher;
    private EditText nameText;
    private ImageButton infoBtn;
    private Spinner currencySpinner;
    private Spinner categorySpinner;
    private Button addButton;
    private LinearLayout infoLayaut;
    private EditText editSum;

    private int currentProgress = 0;
    private boolean responseReceived = false;
    private Handler progressHandler = new Handler();
    private Runnable progressRunnable;
    private TextView progressText;
    private ProgressBar progressBar;
    private LinearLayout progressLayout;
    private LinearLayout ButtonsLayout;
    private TextView podskazka;
    private final int[] selectedType = new int[1];
    private ActivityResultLauncher<Uri> cameraLauncher;
    private Uri imageUri;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_receipt);

        Button buttonSelectImage = findViewById(R.id.buttonSelectImage);
        infoLayaut = findViewById(R.id.infoRow);
        categorySpinner = findViewById(R.id.categorySpinner);
        currencySpinner = findViewById(R.id.currencySpinner);
        infoBtn = findViewById(R.id.infoButton);
        nameText = findViewById(R.id.nameText);
        addButton = findViewById(R.id.buttonAddIncome);
        editSum = findViewById(R.id.editSum);
        ButtonsLayout = findViewById(R.id.buttonContainer);
        podskazka = findViewById(R.id.podskazkatext);

        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        progressLayout = findViewById(R.id.progressLayout);

        Button btnExpense = findViewById(R.id.btnExpense);
        Button btnIncome = findViewById(R.id.btnIncome);

        btnExpense.setOnClickListener(v -> {
            selectedType[0] = 0;
            btnExpense.setBackgroundResource(R.drawable.button_primary);
            btnIncome.setBackgroundResource(R.drawable.button_black);
            categorySpinner.setVisibility(View.VISIBLE);
        });

        btnIncome.setOnClickListener(v -> {
            selectedType[0] = 1;
            btnExpense.setBackgroundResource(R.drawable.button_black);
            btnIncome.setBackgroundResource(R.drawable.button_primary);
            categorySpinner.setVisibility(View.GONE);
        });


        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageButton btnBack = findViewById(R.id.buttonBackFromScan);
        btnBack.setOnClickListener(v -> {
            Intent intentGoBack = new Intent(ScanReceiptActivity.this, StartActivity.class);
            startActivity(intentGoBack);
        });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        recognizeTextFromImage(imageUri);
                    }
                });


        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                isSuccess -> {
                    if (isSuccess && imageUri != null) {
                        recognizeTextFromImage(imageUri);
                    }
                });

        buttonSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        // Кнопка для съемки с камеры
        Button buttonTakePhoto = findViewById(R.id.buttonTakePhoto);  // Добавьте кнопку в ваш layout
        buttonTakePhoto.setOnClickListener(v -> {
            imageUri = createImageUri();  // Создаём URI для фото
            cameraLauncher.launch(imageUri);
        });
    }

    // Метод для создания URI изображения
    private Uri createImageUri() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "photo_" + System.currentTimeMillis());
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
    }



    private void analyzeWithGemini(String receiptText) {
        startFakeProgress();

        if (receiptText.isEmpty()){
            stopFakeProgress();
            resultGot("");
            return;
        }

        String apiKey = "AIzaSyDbDjzTS9neanGArHGyI2ey7fR5zyTshcg";  // Твой API-ключ
//        String promptText = "Проанализируй данные , игнорируй ненужные символы , итог должен быть в чеке , если его нет вычти сам ,  другие данные мне не нужны , дай название расходу по своим умотрениям, в канце в обязательно в квадратных скопках запиши итог из чека - неважно какие у тебя расчеты если есть итог в чеке то запиши обезательно его , если некоторые числа с минусом значит вычитай их , если есть какие то странности или сомнения о чем стоит знать пользовотелю запиши их в канце после знака '^', затем поставь еше '^' и запиши число из  квадратных скобок(только число) , так же больше негде не исползуй етот нак '^':\n\n" + receiptText;
        String promptText = "Проанализируй чек. Игнорируй ненужные символы. Если есть итог — используй его, иначе вычисли сам. " +
                "Назови расход по своему усмотрению. Выведи в следующем формате:\n\n" +
                "НАИМЕНОВАНИЕ: <твоё название>\n" +
                "ПОЗИЦИИ:\n" +
                "- товар 1: сумма\n" +
                "- товар 2: сумма\n" +
                "...\n" +
                "ИТOГ: [сумма]\n" +
                "^пояснение, если есть^\n" +
                "^число из итоговой суммы^\n\n" +
                "Чек:\n" + receiptText;

        // Новый формат JSON
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", promptText);

        JsonArray partsArray = new JsonArray();
        partsArray.add(textPart);

        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");
        userContent.add("parts", partsArray);

        JsonArray contentsArray = new JsonArray();
        contentsArray.add(userContent);

        JsonObject requestData = new JsonObject();
        requestData.add("contents", contentsArray);


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GeminiService service = retrofit.create(GeminiService.class);

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody requestBody = RequestBody.create(mediaType, new Gson().toJson(requestData));

        Call<GeminiResponse> call = service.generateResponse(apiKey, requestBody);

        call.enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().getCandidates().get(0).getContent().getParts().get(0).getText();
                    stopFakeProgress();
                    runOnUiThread(() -> resultGot(result));
                } else {
                    try {
                        stopFakeProgress();
                        String errorBody = response.errorBody().string();
                        Log.e("Gemini Error", errorBody);
                        runOnUiThread(() -> nameText.setText("Ошибка Gemini: " + errorBody));
                        nameText.setVisibility(View.VISIBLE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                stopFakeProgress();
                t.printStackTrace();
                runOnUiThread(() -> nameText.setText("Ошибка Gemini: " + t.getMessage()));
                nameText.setVisibility(View.VISIBLE);
            }
        });
    }
    private void resultGot(String result){
        nameText.setVisibility(View.VISIBLE);
        ButtonsLayout.setVisibility(View.VISIBLE);
        podskazka.setVisibility(View.VISIBLE);

//        String[] parts = result.split("\\^", 3);
//        String mainText = parts[0].trim();
//        String infoText = parts.length > 1 ? parts[1].trim() : "";
//        String summ = parts.length > 2 ? parts[2].trim() : "";
//
//        Pattern numberPattern = Pattern.compile("([\\d]+(?:[\\.,][\\d]+)?)");
//        Matcher numberMatcher = numberPattern.matcher(summ);
//
//        String onlyNumber = "";
//        if (numberMatcher.find()) {
//            onlyNumber = numberMatcher.group(1); // например: "2440.5" или "123,45"
//            editSum.setText(onlyNumber);
//        }
//
//
//        if (mainText.isEmpty()){
//            nameText.setText("чек");
//        } else {
//            nameText.setText(mainText);
//        }
        // Название
        String title = "";
        Pattern titlePattern = Pattern.compile("НАИМЕНОВАНИЕ:\\s*(.+)");
        Matcher titleMatcher = titlePattern.matcher(result);
        if (titleMatcher.find()) {
            title = titleMatcher.group(1).trim();
        }

// Список позиций
        List<String> items = new ArrayList<>();
        Pattern itemsPattern = Pattern.compile("-\\s*(.+?):\\s*([\\d\\.,\\-]+)");
        Matcher itemsMatcher = itemsPattern.matcher(result);
        while (itemsMatcher.find()) {
            String itemLine = itemsMatcher.group(1).trim() + ": " + itemsMatcher.group(2).trim();
            items.add(itemLine);
        }

// Итог в скобках
        String total = "";
        Pattern totalPattern = Pattern.compile("ИТОГ:\\s*\\[(.+?)\\]");
        Matcher totalMatcher = totalPattern.matcher(result);
        if (totalMatcher.find()) {
            total = totalMatcher.group(1).trim();
        }

// Пояснение
        String note = "";
        Pattern notePattern = Pattern.compile("\\^(.*?)\\^");
        Matcher noteMatcher = notePattern.matcher(result);
        if (noteMatcher.find()) {
            note = noteMatcher.group(1).trim();
        }

// Число из итога
        String onlyNumber = "";
        if (noteMatcher.find()) { // повторный вызов для второго ^
            onlyNumber = noteMatcher.group(1).trim();
            editSum.setText(onlyNumber);
        }
        infoLayaut.setVisibility(View.VISIBLE);

        infoBtn.setVisibility(View.VISIBLE);
        String finalNote = note;
        infoBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Информация")
                    .setMessage(finalNote)
                    .setPositiveButton("ОК", null)
                    .show();
        });

        if (title.isEmpty()){
            nameText.setText("чек");
        } else {
            nameText.setText(title);
        }


        // СПИННЕР КАТЕГОРИЙ
        FileHelper fileHelper = new FileHelper(this);
        List<String> categories = fileHelper.getAllCategories();

        Spinner categorySpinner = findViewById(R.id.categorySpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        categorySpinner.setPopupBackgroundResource(R.drawable.spinner_background_cyan);
        categorySpinner.setBackgroundResource(R.drawable.spinner_background_cyan);
        categorySpinner.setSelection(categories.indexOf("прочее"));
        categorySpinner.setVisibility(View.VISIBLE);

        final int[] selectedCategoryId = new int[1];
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategoryId[0] = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // СПИННЕР ВАЛЮТЫ
        Spinner currencySpinner = findViewById(R.id.currencySpinner);
        String[] currencies = {"֏", "$", "₽", "元", "€", "¥", "₾"};
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        currencySpinner.setAdapter(currencyAdapter);
        currencySpinner.setPopupBackgroundResource(R.drawable.spinner_background_cyan);
        currencySpinner.setBackgroundResource(R.drawable.spinner_background_cyan);
        currencySpinner.setVisibility(View.VISIBLE);

        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        String currentCurrencySymbol = databaseIncome.getCurs();

        int defaultCurrencyPosition = 0;
        final String[] selectedSymbol = {""};
        switch (currentCurrencySymbol) {
            case "dollar":
                selectedSymbol[0] = "$";
                defaultCurrencyPosition = 1;
                break;
            case "rubli":
                selectedSymbol[0] = "₽";
                defaultCurrencyPosition = 2;
                break;
            case "yuan":
                selectedSymbol[0] = "元";
                defaultCurrencyPosition = 3;
                break;
            case "evro":
                selectedSymbol[0] = "€";
                defaultCurrencyPosition = 4;
                break;
            case "jen":
                selectedSymbol[0] = "¥";
                defaultCurrencyPosition = 5;
                break;
            case "lari":
                selectedSymbol[0] = "₾";
                defaultCurrencyPosition = 6;
                break;
            case "dram":
            default:
                selectedSymbol[0] = "֏";
                defaultCurrencyPosition = 0;
                break;
        }
        currencySpinner.setSelection(defaultCurrencyPosition);
        int finalDefaultCurrencyPosition = defaultCurrencyPosition;
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSymbol[0] = currencies[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedSymbol[0] = currencies[finalDefaultCurrencyPosition];
            }
        });

        editSum.setVisibility(View.VISIBLE);
        categorySpinner.setVisibility(View.VISIBLE);
        currencySpinner.setVisibility(View.VISIBLE);

        addButton.setVisibility(View.VISIBLE);
        addButton.setOnClickListener(v -> {
            DatabaseHelper databaseHelper = new DatabaseHelper(this);
            int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            String nameT = nameText.getText().toString();
            String spentDataStr = editSum.getText().toString().replaceAll("[^\\d.]", "");
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
            switch (selectedSymbol[0]) {
                case "֏": // Драм
                    finalAmount = valueInX / CursHelper.getToDram();
                    break;
                case "$": // Доллар
                    finalAmount = valueInX / CursHelper.getToDollar();
                    break;
                case "₽": // Рубль
                    finalAmount = valueInX / CursHelper.getToRub();
                    break;
                case "元": // Юань
                    finalAmount = valueInX / CursHelper.getToJuan();
                    break;
                case "€": // Евро
                    finalAmount = valueInX / CursHelper.getToEur();
                    break;
                case "¥": // Йена
                    finalAmount = valueInX / CursHelper.getToJen();
                    break;
                case "₾": // Лари
                    finalAmount = valueInX / CursHelper.getToLari();
                    break;
                default:
                    finalAmount = valueInX; // Если валюта неизвестна — оставить как есть
                    break;
            }

            finalAmount = Math.round(finalAmount * 100.0) / 100.0;


            if(selectedType[0] == 0) {
                databaseHelper.insertData(day, nameT, finalAmount, 0, true, selectedCategoryId[0],finalNote);
                databaseIncome.addSpent(finalAmount);
                Toast.makeText(this, "росход добавлен", Toast.LENGTH_SHORT).show();
            } else {
                databaseHelper.insertData(day, nameT, -1 * finalAmount, 0, true);
                databaseIncome.addIncome(finalAmount);
                Toast.makeText(this, "доход добавлен", Toast.LENGTH_SHORT).show();
            }
            Intent intentGoBack = new Intent(ScanReceiptActivity.this, StartActivity.class);
            startActivity(intentGoBack);
        });
    }

    private void recognizeTextFromImage(Uri imageUri) {
        try {
            // Загружаем изображение
            InputImage image = InputImage.fromFilePath(this, imageUri);


            // Создаем экземпляр распознающего клиента с параметрами
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            // Обрабатываем изображение
            recognizer.process(image)
                    .addOnSuccessListener(result -> {
                        List<Text.Line> allLines = new ArrayList<>();

                        // Проходим по всем блокам текста и собираем строки
                        for (Text.TextBlock block : result.getTextBlocks()) {
                            allLines.addAll(block.getLines());
                        }

                        // Сортировка строк по вертикали
                        Collections.sort(allLines, Comparator.comparingInt(line -> line.getBoundingBox().top));

                        List<String> cleanLines = new ArrayList<>();
                        for (Text.Line line : allLines) {
                            String text = line.getText().replace("*", "").trim();

                            // Убираем дублирующие заголовки
                            if (text.equalsIgnoreCase("Description") || text.equalsIgnoreCase("Price")) continue;

                            if (!text.isEmpty()) {
                                cleanLines.add(text);
                            }
                        }

                        // Формируем вывод
                        StringBuilder builder = new StringBuilder();
                        builder.append(String.format("%-25s %s\n", "Description", "Price"));

                        String lastItem = null;

                        // Формируем строки с данными о товаре и цене
                        for (int i = 0; i < cleanLines.size(); i++) {
                            String line = cleanLines.get(i);

                            if (isPrice(line)) {
                                // Если текущая строка — цена, а до этого была строка-товар
                                if (lastItem != null) {
                                    builder.append(String.format("%-25s %s\n", lastItem, line));
                                    lastItem = null;
                                } else {
                                    // Если до этого не было названия — просто цена
                                    builder.append(String.format("%-25s %s\n", "", line));
                                }
                            } else if (containsPrice(line)) {
                                // Строка уже содержит цену и текст
                                builder.append(formatLine(line)).append("\n");
                                lastItem = null;
                            } else {
                                // Просто название — сохраняем, может дальше пойдет цена
                                if (lastItem != null) {
                                    // Если прошлое название так и не получило цену
                                    builder.append(lastItem).append("\n");
                                }
                                lastItem = line;
                            }
                        }

                        // Если осталась последняя строка без цены
                        if (lastItem != null) {
                            builder.append(lastItem).append("\n");
                        }
                        String recognizedText = builder.toString();
                        analyzeWithGemini(recognizedText); // Отправляем текст для анализа
                    })
                    .addOnFailureListener(e -> {
                        // Обработка ошибки распознавания
                        nameText.setText("Ошибка при распознавании текста: " + e.getMessage());
                    });
        } catch (IOException e) {
            // Ошибка загрузки изображения
            nameText.setText("Не удалось загрузить изображение: " + e.getMessage());
        }
    }


    private void startFakeProgress() {
        currentProgress = 0;
        responseReceived = false;
        progressBar.setProgress(0);
        progressText.setText("0%");
        progressLayout.setVisibility(View.VISIBLE);

        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentProgress < 90 && !responseReceived) {
                    currentProgress++;
                    progressBar.setProgress(currentProgress);
                    progressText.setText(currentProgress + "%");
                    progressHandler.postDelayed(this, 20); // скорость загрузки
                }
            }
        };
        progressHandler.post(progressRunnable);
    }

    private void stopFakeProgress() {
        responseReceived = true;
        progressHandler.removeCallbacks(progressRunnable);

        // Дополним до 100%
        new Thread(() -> {
            for (int i = currentProgress + 1; i <= 100; i++) {
                int finalI = i;
                runOnUiThread(() -> {
                    progressBar.setProgress(finalI);
                    progressText.setText(finalI + "%");
                });
                try {
                    Thread.sleep(20); // скорость завершения
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Скрыть прогресс
            runOnUiThread(() -> progressLayout.setVisibility(View.GONE));
        }).start();
    }





    private boolean containsPrice(String text) {
        return text.matches(".*\\d+\\.\\d{1,2}.*");
    }

    private boolean isPrice(String text) {
        return text.matches("^\\d+\\.\\d{1,2}$");
    }

    private String formatLine(String line) {
        String[] parts = line.split("(?=\\d+\\.\\d{1,2})");
        if (parts.length == 2) {
            String name = parts[0].trim();
            String price = parts[1].trim();
            return String.format("%-25s %s", name, price);
        }
        return line;
    }




}
