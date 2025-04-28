package com.example.mybudget1;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GeminiChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private TextView textStatus;
    private FrameLayout progressOverlay;
    private ImageView animatedSpinner;
    private ObjectAnimator rotationAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gemini_chat);

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                Intent intentGoBack = new Intent(GeminiChatActivity.this, StartActivity.class);
                startActivity(intentGoBack);
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageButton btnBack = findViewById(R.id.buttonBackFromAnaliz);
        btnBack.setOnClickListener(v -> {
            Intent intentGoBack = new Intent(GeminiChatActivity.this, StartActivity.class);
            startActivity(intentGoBack);
        });

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this , chatMessages);
        recyclerViewChat.setAdapter(chatAdapter);

        findViewById(R.id.buttonMonth).setOnClickListener(v -> sendCommand("Анализируй текущий месяц"));
        findViewById(R.id.buttonWeek).setOnClickListener(v -> getCategoriesMessage());
        findViewById(R.id.buttonAll).setOnClickListener(v -> showSelectStartEndDayDialog());

        textStatus = findViewById(R.id.textStatus);
        checkConnectionStatus();

        initFakeProgress();
    }

    private void checkConnectionStatus() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            textStatus.setText("Онлайн");
            textStatus.setTextColor(Color.parseColor("#4CAF50")); // зелёный
        } else {
            textStatus.setText("Оффлайн");
            textStatus.setTextColor(Color.parseColor("#F44336")); // красный
        }
    }

    public void getCategoriesMessage() {
        // Получаем список категорий через FileHelper
        FileHelper fileHelper = new FileHelper(this);
        List<CategoryItem> categories = fileHelper.getCategoriesWithPrices(1);

        if (categories.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Ошибка")
                    .setMessage("Категории не найдены.")
                    .setPositiveButton("Ок", null)
                    .show();
            return;
        }

        List<String> categoryNames = new ArrayList<>();
        for (CategoryItem category : categories) {
            categoryNames.add(category.getName());
        }

        // Показываем диалог выбора категории
        new AlertDialog.Builder(this)
                .setTitle("Выберите категорию")
                .setItems(categoryNames.toArray(new String[0]), (dialog, which) -> {
                    CategoryItem selectedCategory = categories.get(which);
                    sendCommand(sendCategoryAnaliz(selectedCategory));
                })
                .setCancelable(true)
                .show();
    }


    private String sendCategoryAnaliz(CategoryItem selectedCategory) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        List<ExpenseData> data = databaseHelper.getExpensesByCategory(selectedCategory.getId() , 1);

        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        FileHelper fileHelper = new FileHelper(this);
        CursData curs = CursHelper.getCursData(databaseIncome.getDefaultCurrency());

        String userText = "";
        for (ExpenseData expense : data) {
            userText += "- " + expense.getName() + ": " + expense.getDate() + " , " + expense.getAmount() + curs.symbol + "\n";
        }

        return "Вот данные о расходах по категории '" + selectedCategory.getName() + "':\n\n" +
                userText + "\n\n" +
                "Проанализируй эти расходы дружелюбно и понятно.\n" +
                "Обрати внимание, где траты самые большие, и предложи способы оптимизации. 😊\n" +
                "Если есть, отметь положительные моменты. ✨\n" +
                "Дай несколько практичных советов по экономии и управлению расходами.\n" +
                "Стиль: лёгкий, заботливый, но без лишней эмоциональности.\n" +
                "В конце подытожь коротким советом на будущее.\n" +
                "ответ не должен быть бпльше 3000 имволов";

    }


    private void sendCommand(String commandText) {
        // Добавляем сообщение пользователя
        if(commandText.startsWith("Вот данные о расходах по категории")){
            chatMessages.add(new ChatMessage("Вот данные о расходах по категории", true));
        } else if (commandText.startsWith("Вот данные о завершённых расходах")) {
            chatMessages.add(new ChatMessage("Вот данные о завершённых расходах", true));
        } else {
            chatMessages.add(new ChatMessage(commandText, true));
        }
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerViewChat.scrollToPosition(chatMessages.size() - 1);

        if (commandText.equals("Анализируй текущий месяц")) {
            getGeminiResponse(generateFriendlyPrompt(getFormatedMonthString()));
        }else if(commandText.startsWith("Вот данные о расходах по категории")){
            getGeminiResponse(commandText);
        }else if(commandText.startsWith("Вот данные о завершённых расходах")){
            getGeminiResponse(commandText);
        }

    }


    private void testFakeProgress() {
        startFakeProcess();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            stopFakeProcess();
        }, 1000);
    }


    private void getGeminiResponse(String userText) {
        startFakeProcess();
        String apiKey = "AIzaSyDoiw93HSlRXadNii78dlZDXCSIwcmcjOc";  // Твой API-ключ
        String promptText = userText;

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
                    stopFakeProcess();
                    runOnUiThread(() -> {
                        chatMessages.add(new ChatMessage(result, false));
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
                    });
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        stopFakeProcess();
                        Log.e("Gemini Error", errorBody);

                        runOnUiThread(() -> {
                            chatMessages.add(new ChatMessage("Ошибка Gemini: " + errorBody, false));
                            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                            recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                stopFakeProcess();
                t.printStackTrace();

                runOnUiThread(() -> {
                    chatMessages.add(new ChatMessage("Ошибка Gemini: " + t.getMessage(), false));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
                });
            }
        });
    }

    private String generateFriendlyPrompt(String userText) {
        return "Вот данные о доходах и тратах:\n\n" +
                userText + "\n\n" +
                "Пожалуйста, проанализируй эти данные и составь краткий, дружелюбный, немного милый, но без лишней мотивации анализ.\n" +
                "Обрати внимание, где трат больше всего, где есть возможности сэкономить. Если доходы хорошие — похвали легко и приятно.\n" +
                "Дай любые разумные советы по финансам на основе данных (можно несколько, не ограничивайся).\n" +
                "Пиши как заботливый помощник, но без чрезмерной эмоциональности. Стиль: чуть мило, но понятно и по делу.\n" +
                "В конце добавь лёгкое обобщение и мягкий совет на будущее.\n" +
                "ответ не должен быть бпльше 3000 имволов";
    }





    public String getFormattedExpenseString(int i) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        FileHelper fileHelper = new FileHelper(this);
        CursData curs = CursHelper.getCursData(databaseIncome.getDefaultCurrency());
        List<CategoryItem> categories = fileHelper.getCategoriesWithPrices(i);

        // Получаем сегодняшнее число месяца
        Calendar calendar = Calendar.getInstance();
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Формируем строку с сегодняшней датой
        StringBuilder result = new StringBuilder();
        result.append("Сегодня: ").append(dayOfMonth).append(" число месяца, записанные траты за это время: ");

        for (CategoryItem category : categories) {
            result.append(category.getName()).append(" - ").append(category.getPrice()).append(curs.symbol).append(", ");
        }

        List<ExpenseData> data = databaseHelper.getExpensesByCategory(0 , 1);

        result.append(", под other подрозумеваются траты: ");
        for (ExpenseData expense : data) {
            result.append(" - ").append(expense.getName()).append(": ").append(expense.getDate()).append(" , ").append(expense.getAmount()).append(curs.symbol).append("\n");
        }

        // Убираем последнюю запятую и пробел
        if (result.length() > 0 && result.charAt(result.length() - 1) == ' ') {
            result.delete(result.length() - 2, result.length());
        }

        return result.toString();
    }

    public void showSelectStartEndDayDialog() {
        // Получаем максимум дней в этом месяце
        Calendar calendar = Calendar.getInstance();
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);


        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);


        int startDayValue = currentDay - 2;
        int endDayValue = currentDay;


        if (startDayValue < 1) {
            startDayValue = 1;
        }

        final int[] startDay = {1};
        final int[] endDay = {1};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите начальный и конечный день");

        // Первое поле: Выбор старта
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        TextView startLabel = new TextView(this);
        startLabel.setText("Начальный день:");

        NumberPicker startPicker = new NumberPicker(this);
        startPicker.setMinValue(1);
        startPicker.setMaxValue(maxDay);
        startPicker.setValue(startDayValue);

        TextView endLabel = new TextView(this);
        endLabel.setText("Конечный день:");

        NumberPicker endPicker = new NumberPicker(this);
        endPicker.setMinValue(1);
        endPicker.setMaxValue(maxDay);
        endPicker.setValue(endDayValue);

        layout.addView(startLabel);
        layout.addView(startPicker);
        layout.addView(endLabel);
        layout.addView(endPicker);

        builder.setView(layout);

        builder.setPositiveButton("Выбрать", (dialog, which) -> {
            startDay[0] = startPicker.getValue();
            endDay[0] = endPicker.getValue();

            if (startDay[0] > endDay[0]) {
                Toast.makeText(this, "Начальный день не может быть позже конечного!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (endDay[0] - startDay[0] > 7) {
                Toast.makeText(this, "Промежуток не должен превышать 7 дней!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Здесь можно использовать выбранные startDay и endDay
            String text = getFormatedWeekString(startDay[0], endDay[0]);
            sendCommand(text);

        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }




    public String getFormatedWeekString(int startDay , int endDay){
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        Cursor cursor = databaseHelper.getDoneSpentsCursor(startDay, endDay);

        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        CursData curs = CursHelper.getCursData(databaseIncome.getDefaultCurrency());

        if (cursor == null || !cursor.moveToFirst()) {
            return "Нет завершённых трат за выбранный период.";
        }

        StringBuilder userText = new StringBuilder();
        double totalSpent = 0;



        do {
            int day = cursor.getInt(cursor.getColumnIndexOrThrow("day"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            double spent = cursor.getDouble(cursor.getColumnIndexOrThrow("spent"));

            userText.append("День ").append(day).append(": ")
                    .append(name).append(" - ")
                    .append(spent).append(curs.symbol).append("\n");

            totalSpent += spent;
        } while (cursor.moveToNext());

        cursor.close();

        int maxLength = 200;  // Максимальная длина текста

        String formattedUserText = userText.length() > maxLength ? userText.substring(0, maxLength) + "..." : userText.toString();

        return "Вот данные о завершённых расходах с " + startDay + " по " + endDay + " число:\n\n" +
                formattedUserText  + "\n" +
                "Общая сумма расходов: " + totalSpent + curs.symbol + ".\n\n" +
                "Проанализируй, пожалуйста, эти расходы. 😊\n" +
                "Есть ли траты, которые можно сократить? Какие покупки выделяются как особенно крупные?\n" +
                "Дай советы по оптимизации и отметь положительные моменты, если они есть. ✨\n" +
                "Стиль ответа: дружелюбный и полезный." +
                "ответ не должен быть бпльше 3000 имволов";

    }

    public String getFormatedMonthString(){
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        FileHelper fileHelper = new FileHelper(this);
        CursData curs = CursHelper.getCursData(databaseIncome.getDefaultCurrency());
        String oldpart = getFormattedExpenseString(1);
        List<MonthDetailData> detailList;
        detailList = databaseHelper.getIncomesAndMonthlySpents();
        StringBuilder incomePart = new StringBuilder("Доходы за это время — ");
        StringBuilder spentPart = new StringBuilder("Ежемесячные траты за это время — ");

        boolean hasIncome = false;
        boolean hasSpent = false;

        for (MonthDetailData item : detailList) {
            if ("Income".equals(item.getType())) {
                incomePart.append(item.getName())
                        .append(": ")
                        .append((int)item.getAmount())
                        .append(curs.symbol)
                        .append(" , ");
                hasIncome = true;
            } else if ("MSpent".equals(item.getType())) {
                spentPart.append(item.getName())
                        .append(": ")
                        .append((int)item.getAmount())
                        .append(curs.symbol)
                        .append(" , ");
                hasSpent = true;
            }
        }

        if (hasIncome) {
            int last = incomePart.length();
            incomePart.replace(last - 2, last, ".");
        } else {
            incomePart.append("отсутствуют.");
        }

        if (hasSpent) {
            int last = spentPart.length();
            spentPart.replace(last - 2, last, ".");
        } else {
            spentPart.append("отсутствуют.");
        }

        return oldpart + incomePart.toString() + " " + spentPart.toString();
    }


    private void initFakeProgress() {
        progressOverlay = findViewById(R.id.progressOverlay);
        animatedSpinner = findViewById(R.id.animatedSpinner);
    }

    private void startFakeProcess() {
        progressOverlay.setAlpha(0f);
        progressOverlay.setVisibility(View.VISIBLE);
        progressOverlay.animate().alpha(1f).setDuration(250).start();

        rotationAnimator = ObjectAnimator.ofFloat(animatedSpinner, "rotation", 0f, 360f);
        rotationAnimator.setDuration(1000);
        rotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotationAnimator.setInterpolator(new LinearInterpolator());
        rotationAnimator.start();
    }

    private void stopFakeProcess() {
        progressOverlay.animate().alpha(0f).setDuration(250).withEndAction(() -> {
            progressOverlay.setVisibility(View.GONE);
        }).start();

        if (rotationAnimator != null && rotationAnimator.isRunning()) {
            rotationAnimator.cancel();
        }
    }

}


