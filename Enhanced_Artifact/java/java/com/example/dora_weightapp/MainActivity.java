package com.example.dora_weightapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnCreateAccount;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        // Initialize database helper
        db = new DBHelper(this);

        // Create Account button logic
        btnCreateAccount.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!isInputValid(username, password)) {
                return;
            }

            try {
                if (db.checkUsernameExists(username)) {
                    Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
                } else {
                    boolean inserted = db.insertUser(username, password);
                    if (inserted) {
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error creating account", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(this, "An unexpected error occurred. Please try again.", Toast.LENGTH_LONG).show();
            }
        });

        // Login button logic
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!isInputValid(username, password)) {
                return;
            }

            try {
                if (db.checkUser(username, password)) {
                    getSharedPreferences("UserPrefs", MODE_PRIVATE)
                            .edit()
                            .putString("username", username)
                            .apply();

                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(MainActivity.this, GoalWeightActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "An unexpected error occurred. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Enhancement: Centralized input validation
    private boolean isInputValid(String username, String password) {

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username and password cannot be empty", Toast.LENGTH_LONG).show();
            return false;
        }

        if (username.length() < 4) {
            Toast.makeText(this, "Username must be at least 4 characters", Toast.LENGTH_LONG).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
}
