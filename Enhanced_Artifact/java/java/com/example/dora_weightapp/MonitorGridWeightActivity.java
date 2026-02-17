package com.example.dora_weightapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.telephony.SmsManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MonitorGridWeightActivity extends AppCompatActivity {

    ListView weightListView;
    Button addWeightButton, btnSetGoal;
    DBHelper db;
    ArrayList<WeightItem> weightList;
    WeightAdapter adapter;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_gridweight);

        db = new DBHelper(this);

        weightListView = findViewById(R.id.weightListView);
        addWeightButton = findViewById(R.id.addWeightButton);
        btnSetGoal = findViewById(R.id.btnSetGoal);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        username = prefs.getString("username", null);

        if (username == null) {
            Toast.makeText(this, "No user found, please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        weightList = new ArrayList<>();
        adapter = new WeightAdapter(this, weightList);
        weightListView.setAdapter(adapter);

        loadWeights();

        Toast.makeText(this, "Long press a weight to update or delete it.", Toast.LENGTH_LONG).show();

        addWeightButton.setOnClickListener(v -> showAddWeightDialog());
        btnSetGoal.setOnClickListener(v -> startActivity(new Intent(this, GoalWeightActivity.class)));

        weightListView.setOnItemLongClickListener((parent, view, position, id) -> {
            WeightItem item = weightList.get(position);

            CharSequence[] options = {"Update", "Delete", "Cancel"};
            new AlertDialog.Builder(this)
                    .setTitle("Choose Action")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) showUpdateWeightDialog(item);
                        else if (which == 1) confirmDelete(item);
                        else dialog.dismiss();
                    }).show();

            return true;
        });
    }

    private void loadWeights() {
        weightList.clear();
        var cursor = db.getAllWeights(username);

        if (cursor != null) {
            int idIndex = cursor.getColumnIndex("id");
            int dateIndex = cursor.getColumnIndex("date");
            int weightIndex = cursor.getColumnIndex("weight");

            while (cursor.moveToNext()) {
                int id = cursor.getInt(idIndex);
                String date = cursor.getString(dateIndex);
                double weight = cursor.getDouble(weightIndex);
                weightList.add(new WeightItem(id, date, weight));
            }
            cursor.close();
        }

        adapter.notifyDataSetChanged();
        analyzeWeightTrend(); // Algorithm runs here
    }

    /**
     * ALGORITHM: Analyze weight trend using the two most recent entries
     */
    private void analyzeWeightTrend() {
        if (weightList.size() < 2) {
            return; // Not enough data to analyze
        }

        double latestWeight = weightList.get(0).weight;
        double previousWeight = weightList.get(1).weight;

        if (latestWeight < previousWeight) {
            Toast.makeText(this, " Great job! You're losing weight.", Toast.LENGTH_SHORT).show();
        } else if (latestWeight > previousWeight) {
            Toast.makeText(this, " Weight increased. Stay consistent!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, " No change in weight.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddWeightDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Daily Weight");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter today's weight");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String value = input.getText().toString();
            if (!value.isEmpty()) {
                double weight = Double.parseDouble(value);
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                if (db.insertWeight(username, date, weight)) {
                    Toast.makeText(this, "Weight added!", Toast.LENGTH_SHORT).show();
                    checkGoal(weight);
                    loadWeights();
                } else {
                    Toast.makeText(this, "Error adding weight", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showUpdateWeightDialog(WeightItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Weight");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf(item.weight));
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            double newWeight = Double.parseDouble(input.getText().toString());
            if (db.updateWeight(item.id, newWeight)) {
                Toast.makeText(this, "Weight updated!", Toast.LENGTH_SHORT).show();
                loadWeights();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void confirmDelete(WeightItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Weight")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (db.deleteWeight(item.id)) {
                        Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        loadWeights();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkGoal(double weight) {
        Double goal = db.getGoalWeight(username);
        if (goal != null && weight <= goal) {
            Toast.makeText(this, "🎉 You reached your goal weight!", Toast.LENGTH_LONG).show();

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(
                            "YOUR_PHONE_NUMBER",
                            null,
                            "Congrats! You reached your goal weight of " + goal + "!",
                            null,
                            null
                    );
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to send SMS", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}