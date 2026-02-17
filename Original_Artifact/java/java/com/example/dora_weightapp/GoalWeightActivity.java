package com.example.dora_weightapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class GoalWeightActivity extends AppCompatActivity {

    EditText etGoalWeight;
    Button btnSaveGoal;
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_weight);

        etGoalWeight = findViewById(R.id.etGoalWeight);
        btnSaveGoal = findViewById(R.id.btnSaveGoal);
        db = new DBHelper(this);

        btnSaveGoal.setOnClickListener(v -> {
            String input = etGoalWeight.getText().toString().trim();

            if (input.isEmpty()) {
                Toast.makeText(this, "Please enter a number", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double goal = Double.parseDouble(input);
                boolean saved = db.setGoalWeight(goal);

                if (saved) {
                    Toast.makeText(this, "Goal weight saved!", Toast.LENGTH_SHORT).show();

                    // Navigate to SMS Permission screen
                    Intent intent = new Intent(GoalWeightActivity.this, SmsPermissionActivity.class);
                    startActivity(intent);
                    finish(); // close this screen so user can’t go back
                } else {
                    Toast.makeText(this, "Error saving goal", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
