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
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageButton btnInfoGemini = findViewById(R.id.btnInfoGemini);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageButton btnHelpGemini = findViewById(R.id.btnHelpGemini);

        btnInfoGemini.setOnClickListener(v -> sendCommand("/info"));
        btnHelpGemini.setOnClickListener(v -> sendCommand("/help"));

        findViewById(R.id.buttonMonth).setOnClickListener(v -> sendCommand("Анализируй текущий месяц"));
        findViewById(R.id.buttonWeek).setOnClickListener(v -> getCategoriesMessage());
        findViewById(R.id.buttonAll).setOnClickListener(v -> showSelectStartEndDayDialog());

        textStatus = findViewById(R.id.textStatus);
        checkConnectionStatus();

        DatabaseHelper db = new DatabaseHelper(this);
        chatMessages.addAll(db.getAllChatMessages());
        recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
        chatAdapter.notifyDataSetChanged();
        recyclerViewChat.scrollToPosition(chatMessages.size() - 1);



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
        List<CategoryItem> categories = fileHelper.getCategoriesWithPrices(this,1);

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
        List<ExpenseData> data = databaseHelper.getExpensesByCategory(selectedCategory.getId(), 1);

        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        CursData curs = CursHelper.getCursData(databaseIncome.getDefaultCurrency());

        StringBuilder userText = new StringBuilder();
        for (ExpenseData expense : data) {
            userText.append("- ")
                    .append(expense.getName()).append(": ")
                    .append(expense.getDate()).append(", ")
                    .append(expense.getAmount()).append(curs.symbol)
                    .append("\n");
        }

        return "Вот мои расходы по категории «" + selectedCategory.getName() + "» за последнее время:\n\n" +
                userText.toString() + "\n" +
                "Проанализируй, пожалуйста, эти данные:\n" +
                "- Укажи, какие покупки или дни выделяются, и были ли особенно крупные траты.\n" +
                "- Оцени, насколько эти расходы выглядят уместными для данной категории.\n" +
                "- Не нужно предлагать экономию, если расходы выглядят разумно — просто оцени со стороны.\n" +
                "- Дай советы только при необходимости: где можно было бы немного сократить или переосмыслить.\n" +
                "- Отметь положительное, если видно, что категория используется аккуратно или разумно.\n\n" +
                "Формат ответа: дружелюбный, простой, заботливый, без излишней мотивации.\n" +
                "Заверши ответ лёгким обобщением или небольшим полезным советом.\n" +
                "Максимум — 3000 символов.";
    }



    private void sendCommand(String commandText) {
        if(commandText.startsWith("Вот мои расходы по категории")){
            chatMessages.add(new ChatMessage("Вот мои расходы по категории", true));
            DatabaseHelper db = new DatabaseHelper(this);
            db.insertChatMessage(new ChatMessage("Вот мои расходы по категории", true));
        } else if (commandText.startsWith("Вот данные о завершённых действиях")) {
            chatMessages.add(new ChatMessage("Вот данные о завершённых действиях", true));
            DatabaseHelper db = new DatabaseHelper(this);
            db.insertChatMessage(new ChatMessage("Вот данные о завершённых действиях", true));
        } else {
            chatMessages.add(new ChatMessage(commandText, true));
            DatabaseHelper db = new DatabaseHelper(this);
            db.insertChatMessage(new ChatMessage(commandText, true));
        }
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerViewChat.scrollToPosition(chatMessages.size() - 1);

        if (commandText.equals("Анализируй текущий месяц")) {
            getGeminiResponse(generateFriendlyPrompt(getFormatedMonthString()));
        }else if(commandText.startsWith("Вот данные о расходах по категории")){
            getGeminiResponse(commandText);
        }else if(commandText.startsWith("Вот данные о завершённых действиях")){
            getGeminiResponse(commandText);
        }else if (commandText.equals("/info")){
            getInfo();
        }else if (commandText.equals("/help")){
            getHelp();
        }

    }

    private void getHelp() {
        String info = "Анализ бюджета\n" +
                "В приложении доступны три режима анализа ваших трат и доходов:\n" +
                "\n" +
                "1. Анализ месяца\n" +
                "Показывает полную картину ваших доходов и расходов за текущий месяц.\n" +
                "Все операции группируются по категориям, кроме категории \"Прочее\", — траты из этой категории отображаются отдельно, без объединения.\n" +
                "\n" +
                "2. Анализ категории\n" +
                "Позволяет выбрать конкретную категорию и посмотреть все связанные с ней траты и доходы.\n" +
                "Удобно, если вы хотите проанализировать, например, только питание или транспорт.\n" +
                "\n" +
                "3. Анализ промежутка\n" +
                "Выберите даты (максимум 7 дней) и получите список всех трат за этот период.\n" +
                "Здесь каждая трата отображается отдельно, без группировки по категориям, чтобы вы могли видеть все детали.";
        chatMessages.add(new ChatMessage(info, false));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
    }

    private void getInfo() {
        String info = "Информация о работе Асистента \"MyBudget\"\n" +
                "\n" +
                "Это приложение использует передовые технологии обработки естественного языка, разработанные Google (Gemini), для анализа ваших финансовых данных и предоставления вам полезной информации и прогнозов.\n" +
                "\n" +
                "Как это работает:\n" +
                "\n" +
                "Приложение автоматически анализирует ваши записи о доходах и расходах.\n" +
                "На основе этого анализа формируются внутренние запросы к языковой модели Gemini.\n" +
                "Gemini обрабатывает эти запросы и генерирует для вас:\n" +
                "Сводную информацию о вашем бюджете.\n" +
                "Прогнозы и тенденции ваших расходов и доходов.\n" +
                "Рекомендации по оптимизации вашего бюджета (если применимо).\n" +
                "Важно знать:\n" +
                "\n" +
                "Все запросы генерируются автоматически приложением на основе ваших финансовых данных, введенных в самом приложении.\n" +
                "Приложение не отправляет никаких ваших личных данных или введенной информации напрямую в Google или третьим лицам для генерации ответов. Взаимодействие с Gemini происходит внутри приложения для обработки и анализа уже имеющихся данных.\n" +
                "Цель использования этой технологии — предоставить вам более глубокое понимание вашего финансового состояния и помочь в управлении бюджетом.\n" +
                "Пожалуйста, помните, что прогнозы и рекомендации, сгенерированные приложением, основаны на статистическом анализе и не являются финансовым советом. Принимайте решения, основываясь на собственном анализе и, при необходимости, консультируйтесь с финансовыми специалистами.\n" +
                "При наличии любых вопросов/советов напишите мне в телеграм - @Temnashka" +
                "Мы постоянно работаем над улучшением работы приложения и повышением точности предоставляемой информации. Благодарим вас за использование \"MyBudget\"!";
        chatMessages.add(new ChatMessage(info, false));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
    }


    private void testFakeProgress() {
        startFakeProcess();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            stopFakeProcess();
        }, 1000);
    }


    private void getGeminiResponse(String userText) {
        startFakeProcess();
        String apiKey = "AIzaSyDbDjzTS9neanGArHGyI2ey7fR5zyTshcg";  // Твой API-ключ
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
                        ChatMessage message = new ChatMessage(result, false);
                        chatMessages.add(message);
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        recyclerViewChat.scrollToPosition(chatMessages.size() - 1);

                        DatabaseHelper dbHelper = new DatabaseHelper(GeminiChatActivity.this);
                        dbHelper.insertChatMessage(message);
                    });
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        stopFakeProcess();
                        Log.e("Gemini Error", errorBody);

                        runOnUiThread(() -> {
                            chatMessages.add(new ChatMessage("Ошибка Gemini: " + "сейчас функция недоступно(обратитесь в тг @Temnashka)", false));
                            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                            recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
                            DatabaseHelper dbHelper = new DatabaseHelper(GeminiChatActivity.this);
                            dbHelper.insertChatMessage(new ChatMessage("Ошибка Gemini: " + "сейчас функция недоступно(обратитесь в тг @Temnashka)", false));
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
                    chatMessages.add(new ChatMessage("Ошибка Gemini: " + "сейчас функция недоступно(обратитесь в тг @Temnashka)", false));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
                    DatabaseHelper dbHelper = new DatabaseHelper(GeminiChatActivity.this);
                    dbHelper.insertChatMessage(new ChatMessage("Ошибка Gemini: " + "сейчас функция недоступно(обратитесь в тг @Temnashka)", false));
                });
            }
        });
    }

    private String generateFriendlyPrompt(String userText) {
        return "Ниже приведены мои данные о доходах и расходах за период:\n\n" +
                userText + "\n\n" +
                "Проанализируй, пожалуйста, эти финансовые данные и составь краткий, содержательный и дружелюбный отчёт.\n" +
                "- Укажи, какие траты особенно выделяются и в каких категориях они были.\n" +
                "- Если есть заметные дисбалансы — укажи их, но деликатно.\n" +
                "- Если всё выглядит разумно — похвали и поддержи меня легко и искренне.\n" +
                "- Дай разумные советы по улучшению финансов, но не стремись всегда предлагать экономию — она уместна только если расходы реально чрезмерны.\n" +
                "- Подчеркни положительные моменты, если они есть.\n" +
                "- Стиль ответа: понятный, мягкий, немного милый, как от заботливого помощника, без чрезмерной мотивации или драматизма.\n\n" +
                "В завершение добавь лёгкое, спокойное обобщение с мягким советом на будущее.\n" +
                "Максимальная длина ответа: 3000 символов.";
    }






    public String getFormattedExpenseString(int i) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        FileHelper fileHelper = new FileHelper(this);
        CursData curs = CursHelper.getCursData(databaseIncome.getDefaultCurrency());
        List<CategoryItem> categories = fileHelper.getCategoriesWithPrices(this,i);

        // Получаем сегодняшнее число месяца
        Calendar calendar = Calendar.getInstance();
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Формируем строку с сегодняшней датой
        StringBuilder result = new StringBuilder();
        result.append("Сегодня: ").append(dayOfMonth).append(" число месяца, записанные траты за это время: ");

        for (CategoryItem category : categories) {
            result.append(category.getName()).append(" - ").append(category.getPrice()).append(curs.symbol).append(", ");
        }

        List<ExpenseData> data = databaseHelper.getExpensesByCategory(1 , 1);

        result.append(", под \"прочее\" подрозумеваются траты: ");
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




    public String getFormatedWeekString(int startDay, int endDay) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        Cursor cursor = databaseHelper.getDoneSpentsCursor(startDay, endDay);

        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        double totalIncome = databaseHelper.getCurrentIncomesTotal();
        double budget = databaseIncome.getBudget();
        CursData curs = CursHelper.getCursData(databaseIncome.getDefaultCurrency());

        if (cursor == null || !cursor.moveToFirst()) {
            return "Нет завершённых действий за выбранный период.";
        }

        StringBuilder userText = new StringBuilder();
        userText.append("Доходы за месяц: ").append(totalIncome).append(curs.symbol).append("\n");
        userText.append("Бюджет сейчас: ").append(budget).append(curs.symbol).append("\n\n");

        double totalSpent = 0;

        do {
            int day = cursor.getInt(cursor.getColumnIndexOrThrow("day"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            double spent = cursor.getDouble(cursor.getColumnIndexOrThrow("spent"));

            if (spent > 0) {
                userText.append("День ").append(day).append(": ")
                        .append(name).append(" — ")
                        .append(spent).append(curs.symbol).append("\n");
                totalSpent += spent;
            }
        } while (cursor.moveToNext());

        cursor.close();

        int maxLength = 200;
        String formattedUserText = userText.length() > maxLength
                ? userText.substring(0, maxLength) + "..."
                : userText.toString();

        return "Вот данные о завершённых действиях за период с " + startDay + " по " + endDay + " число (до 7 дней):\n\n" +
                formattedUserText + "\n" +
                "Суммарные расходы за период: " + totalSpent + curs.symbol + ".\n\n" +
                "Пожалуйста, проанализируй эти данные:\n" +
                "- Что можно отметить по тратам?\n" +
                "- Есть ли очевидные перекосы или интересные особенности?\n" +
                "- Дай мягкие рекомендации, если что-то можно улучшить, но не критикуй без причины.\n" +
                "- Если всё в порядке — поддержи, выдели положительное.\n\n" +
                "Ответ должен быть дружелюбным, понятным и до 3000 символов.";
    }



    public String getFormatedMonthString() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        FileHelper fileHelper = new FileHelper(this);
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        CursData curs = CursHelper.getCursData(databaseIncome.getDefaultCurrency());

        String oldpart = getFormattedExpenseString(1);
        StringBuilder incomePart = new StringBuilder("Доходы за это время — ");

        List<MonthDetailData> incomeList = databaseHelper.getCorrentIncomes();
        boolean hasIncome = false;

        for (MonthDetailData item : incomeList) {
            incomePart.append(item.getName())
                    .append(": ")
                    .append(item.getAmount())
                    .append(curs.symbol)
                    .append(" , ");
            hasIncome = true;
        }

        if (hasIncome) {
            int last = incomePart.length();
            incomePart.replace(last - 2, last, ".");
        } else {
            incomePart.append("отсутствуют.");
        }

        return oldpart + incomePart.toString();
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


