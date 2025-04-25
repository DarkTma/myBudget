package com.example.mybudget1;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

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
        findViewById(R.id.buttonAll).setOnClickListener(v -> sendCommand("Анализируй все мои траты"));

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

        List<String> categoryNames = new ArrayList<>();
        for (CategoryItem category : categories) {
            categoryNames.add(category.getName());
        }

        CategoryItem selectedCategory = categories.get(0);
        sendCommand(sendCategoryAnaliz(selectedCategory));

//        // Создаем диалог с выбором категории
//        new AlertDialog.Builder(this)
//                .setTitle("Выберите категорию")
//                .setItems(categoryNames.toArray(new String[0]), (dialog, which) -> {
//                    // Здесь обрабатываем выбор категории
//                    CategoryItem selectedCategory = categories.get(which);
//                    sendCommand(sendCategoryAnaliz(selectedCategory));
//                })
//                .setCancelable(true)
//                .show();
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

        return "Вот данные о расходах по категории '" + selectedCategory.getName() + "|" + "':\n\n" +
                userText + "\n\n" +
                "Пожалуйста, проанализируй эти данные и составь дружелюбный и полезный анализ.\n" +
                "Обрати внимание на те статьи, где трат больше всего. Есть ли возможность сэкономить или пересмотреть расходы? 😊\n" +
                "Если какие-то расходы кажутся слишком высокими, дай советы по оптимизации. Может быть, стоит попробовать что-то новое или уменьшить частоту покупок?\n" +
                "Не забывай отметить, если в категории есть положительные моменты — похвали, но легко и без преувеличений. ✨\n" +
                "Дай несколько разумных советов по улучшению управления расходами в этой категории. Включи несколько идей и предложений.\n" +
                "Стиль: немного мило, но понятно и по делу, как хороший помощник, который всегда рядом и готов помочь. 😊\n" +
                "В конце подытожь и дай лёгкий, но полезный совет на будущее.";

    }


    private void sendCommand(String commandText) {
        // Добавляем сообщение пользователя
        if(commandText.startsWith("Вот данные о расходах по категории")){
            chatMessages.add(new ChatMessage("Вот данные о расходах по категории", true));
        } else {
            chatMessages.add(new ChatMessage(commandText, true));
        }
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerViewChat.scrollToPosition(chatMessages.size() - 1);

        if (commandText.equals("Анализируй текущий месяц")) {
            getGeminiResponse(generateFriendlyPrompt(getFormatedMonthString()));
        }else if(commandText.startsWith("Вот данные о расходах по категории")){
            getGeminiResponse(commandText);
        }

    }


    private void testFakeProgress() {
        startFakeProcess();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            stopFakeProcess();
        }, 1000); // 1 секунда
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
        stopFakeProcess();
    }

    private String generateFriendlyPrompt(String userText) {
        return "Вот данные о доходах и тратах:\n\n" +
                userText + "\n\n" +
                "Пожалуйста, проанализируй эти данные и составь краткий, дружелюбный, немного милый, но без лишней мотивации анализ.\n" +
                "Обрати внимание, где трат больше всего, где есть возможности сэкономить. Если доходы хорошие — похвали легко и приятно.\n" +
                "Дай любые разумные советы по финансам на основе данных (можно несколько, не ограничивайся).\n" +
                "Пиши как заботливый помощник, но без чрезмерной эмоциональности. Стиль: чуть мило, но понятно и по делу.\n" +
                "В конце добавь лёгкое обобщение и мягкий совет на будущее.";
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

        // Добавляем траты по каждой категории
        for (CategoryItem category : categories) {
            result.append(category.getName()).append(" - ").append(category.getPrice()).append(curs.symbol).append(", ");
        }

        // Убираем последнюю запятую и пробел
        if (result.length() > 0 && result.charAt(result.length() - 1) == ' ') {
            result.delete(result.length() - 2, result.length());
        }

        return result.toString();
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


