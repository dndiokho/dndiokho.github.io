package com.example.dora_weightapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin, btnCreateAccount;
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        // Initialize DBHelper
        db = new DBHelper(this);

        // Create Account button logic
        btnCreateAccount.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if(username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            } else if(db.checkUsernameExists(username)) {
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
            } else {
                boolean inserted = db.insertUser(username, password);
                if(inserted)
                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Error creating account", Toast.LENGTH_SHORT).show();
            }
        });

        // Login button logic
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if(db.checkUser(username, password)) {
                // Save username in SharedPreferences
                getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        .edit()
                        .putString("username", username)
                        .apply();

                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();

                // Navigate to GoalWeightActivity instead of SMS permission
                Intent intent = new Intent(MainActivity.this, GoalWeightActivity.class);
                startActivity(intent);
                finish(); // close login screen
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
