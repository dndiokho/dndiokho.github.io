package com.example.dora_weightapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class GoalWeightActivity extends AppCompatActivity {

    private EditText etGoalWeight;
    private Button btnSaveGoal;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_weight);

        etGoalWeight = findViewById(R.id.etGoalWeight);
        btnSaveGoal = findViewById(R.id.btnSaveGoal);
        db = new DBHelper(this);

        btnSaveGoal.setOnClickListener(v -> {
            String input = etGoalWeight.getText().toString().trim();

            // UI-level validation
            if (input.isEmpty()) {
                Toast.makeText(this, "Please enter your goal weight", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double goal = Double.parseDouble(input);

                // Range validation (design decision)
                if (goal <= 0 || goal > 1000) {
                    Toast.makeText(this, "Please enter a realistic goal weight", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean saved = db.setGoalWeight(goal);

                if (saved) {
                    Toast.makeText(this, "Goal weight saved successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(GoalWeightActivity.this, SmsPermissionActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Unable to save goal. Please try again.", Toast.LENGTH_SHORT).show();
                }

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
            }
        });
    }
}