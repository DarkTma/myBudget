package com.example.mybudget1;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GeminiChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private TextView textStatus;

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

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this , chatMessages);
        recyclerViewChat.setAdapter(chatAdapter);

        findViewById(R.id.buttonMonth).setOnClickListener(v -> sendCommand("Анализируй текущий месяц"));
        findViewById(R.id.buttonWeek).setOnClickListener(v -> sendCommand("Анализируй текущую неделю"));
        findViewById(R.id.buttonAll).setOnClickListener(v -> sendCommand("Анализируй все мои траты"));

        textStatus = findViewById(R.id.textStatus);
        checkConnectionStatus();
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

    private void sendCommand(String commandText) {
        // Добавляем сообщение пользователя
        chatMessages.add(new ChatMessage(commandText, true));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerViewChat.scrollToPosition(chatMessages.size() - 1);

        // Подключаем ИИ — симуляция
        getGeminiResponse(commandText);
    }

    private void getGeminiResponse(String userText) {
        // Тут должен быть вызов Gemini, а пока просто ответ-заглушка:
        new Handler().postDelayed(() -> {
            String fakeResponse = "Вот анализ: " + userText + " (ответ от ИИ)";
            chatMessages.add(new ChatMessage(fakeResponse, false));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
        }, 1000);
    }
}


