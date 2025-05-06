package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PrivacyPolicyActivity extends AppCompatActivity {
    private ImageButton buttonBackFromConf;
    private static final String PRIVACY_POLICY_URL = "https://www.dropbox.com/scl/fi/77noykv1p6a28ne5csxn6/.pdf?rlkey=dmhtmg8bb4y3s5ekxws243m2z&st=83tke5wq&dl=0";

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

        Switch authSwitch = findViewById(R.id.switch_auth);

        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isAuthEnabled = preferences.getBoolean("auth_enabled", false);
        authSwitch.setChecked(isAuthEnabled);

        authSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("auth_enabled", isChecked);
            editor.apply();
        });

    }
}

