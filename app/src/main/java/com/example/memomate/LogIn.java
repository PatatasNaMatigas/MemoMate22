package com.example.memomate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class LogIn extends AppCompatActivity {
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
            }
        }));
    }
}