package com.example.cafefinder;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MenuWebActivity extends AppCompatActivity {

    WebView webViewMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_web);

        webViewMenu = findViewById(R.id.webViewMenu);

        String menuUrl = getIntent().getStringExtra("MENU_URL");

        if (menuUrl == null) {
            Toast.makeText(this, "Menu URL not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        WebSettings settings = webViewMenu.getSettings();
        settings.setJavaScriptEnabled(true);

        webViewMenu.setWebViewClient(new WebViewClient());
        webViewMenu.loadUrl(menuUrl);
    }
}