package com.example.exampracticeclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class WelcomePage extends AppCompatActivity {

    private Button btnContinue;
    private EditText edtName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

        btnContinue = findViewById(R.id.btnContinue);
        edtName = findViewById(R.id.edtName);

        btnContinue.setOnClickListener(v -> {

            String name = edtName.getText().toString();

            Intent intent = new Intent(WelcomePage.this, Client.class);
            intent.putExtra("Name", name);  // Pass the name as extra data
            startActivity(intent);  // Start the second activity

        });

    }
}