package com.example.cafefinder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class QrActivity extends AppCompatActivity {

    Button btnScan;

    private final ActivityResultLauncher<ScanOptions> scanLauncher =
            registerForActivityResult(new ScanContract(), result -> {

                if (result.getContents() == null) {
                    Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                    return;
                }

                String qrContent = result.getContents().trim();

                // ✅ Pastikan QR adalah URL
                if (Patterns.WEB_URL.matcher(qrContent).matches()) {

                    if (!qrContent.startsWith("http://") &&
                            !qrContent.startsWith("https://")) {
                        qrContent = "https://" + qrContent;
                    }

                    Intent intent = new Intent(
                            QrActivity.this,
                            MenuWebActivity.class
                    );
                    intent.putExtra("MENU_URL", qrContent);
                    startActivity(intent);

                } else {
                    Toast.makeText(
                            this,
                            "QR bukan link menu",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        btnScan = findViewById(R.id.btnScan);

        btnScan.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan QR Menu");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            options.setCaptureActivity(
                    com.journeyapps.barcodescanner.CaptureActivity.class
            );
            scanLauncher.launch(options);
        });
    }
}
