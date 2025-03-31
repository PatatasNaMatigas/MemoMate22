package org.memomate.memomate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import org.memomate.notification.NotificationBuilder;

public class LogIn extends AppCompatActivity {

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        EditText userName = findViewById(R.id.userName);
        EditText passWord = findViewById(R.id.passWord);
        Button login = findViewById(R.id.logIn);
        login.setOnClickListener((k -> {
            if (!userName.getText().toString().isEmpty() && !passWord.getText().toString().isEmpty()) {
                startActivity(new Intent(this, Main.class));
                finish();
            }
        }));

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(this, "Notification Permission Granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Notification Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                });

        askNotificationPermission();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}