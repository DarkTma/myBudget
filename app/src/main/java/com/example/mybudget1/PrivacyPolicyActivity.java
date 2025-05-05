package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PrivacyPolicyActivity extends AppCompatActivity {
    private ImageButton buttonBackFromConf;
    private static final String PRIVACY_POLICY_URL = "https://www.dropbox.com/scl/fi/77noykv1p6a28ne5csxn6/.pdf?rlkey=dmhtmg8bb4y3s5ekxws243m2z&st=gzux4r6a&dl=0";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        buttonBackFromConf = findViewById(R.id.buttonBackFromConf);
        buttonBackFromConf.setOnClickListener(v -> {
            Intent intent = new Intent(PrivacyPolicyActivity.this, StartActivity.class);
            startActivity(intent);
            finish();
        });
        TextView link = findViewById(R.id.privacy_link);
        link.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL));
            startActivity(browserIntent);
        });
    }
}

