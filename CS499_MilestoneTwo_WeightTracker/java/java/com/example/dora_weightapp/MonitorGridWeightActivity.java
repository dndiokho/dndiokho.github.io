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

        // Initialize views
        weightListView = findViewById(R.id.weightListView);
        addWeightButton = findViewById(R.id.addWeightButton);
        btnSetGoal = findViewById(R.id.btnSetGoal);

        // Get current logged-in username
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        username = prefs.getString("username", null);

        if (username == null) {
            Toast.makeText(this, "No user found, please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Initialize weight list and adapter
        weightList = new ArrayList<>();
        adapter = new WeightAdapter(this, weightList);
        weightListView.setAdapter(adapter);

        loadWeights(); // load current user's weights

        // Inform users about long press
        Toast.makeText(this, "Long press a weight to update or delete it.", Toast.LENGTH_LONG).show();

        // Add weight button
        addWeightButton.setOnClickListener(v -> showAddWeightDialog());

        // Set goal button
        btnSetGoal.setOnClickListener(v -> startActivity(new Intent(this, GoalWeightActivity.class)));

        // Long press to Update or Delete weight
        weightListView.setOnItemLongClickListener((parent, view, position, id) -> {
            WeightItem item = weightList.get(position);

            CharSequence[] options = {"Update", "Delete", "Cancel"};
            new AlertDialog.Builder(this)
                    .setTitle("Choose Action")
                    .setItems(options, (dialog, which) -> {
                        switch (which) {
                            case 0: // Update
                                showUpdateWeightDialog(item);
                                break;
                            case 1: // Delete
                                confirmDelete(item);
                                break;
                            case 2: // Cancel
                                dialog.dismiss();
                                break;
                        }
                    }).show();

            return true; // consume long press
        });
    }

    /** Load all weights for the current user into the list */
    private void loadWeights() {
        weightList.clear();
        var cursor = db.getAllWeights(username);

        if (cursor != null) {
            int idIndex = cursor.getColumnIndex("id");
            int dateIndex = cursor.getColumnIndex("date");
            int weightIndex = cursor.getColumnIndex("weight");

            while (cursor.moveToNext()) {
                int id = idIndex != -1 ? cursor.getInt(idIndex) : 0;
                String date = dateIndex != -1 ? cursor.getString(dateIndex) : "Unknown date";
                double weight = weightIndex != -1 ? cursor.getDouble(weightIndex) : 0;
                weightList.add(new WeightItem(id, date, weight));
            }
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    /** Show dialog to add a new weight */
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

                boolean inserted = db.insertWeight(username, date, weight);
                if (inserted) {
                    Toast.makeText(this, "Weight added!", Toast.LENGTH_SHORT).show();
                    checkGoal(weight);
                    loadWeights();
                } else {
                    Toast.makeText(this, "Error adding weight", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter a weight", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /** Show dialog to update a selected weight */
    private void showUpdateWeightDialog(WeightItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Weight");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf(item.weight));
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String value = input.getText().toString();
            if (!value.isEmpty()) {
                double newWeight = Double.parseDouble(value);
                boolean updated = db.updateWeight(item.id, newWeight);
                if (updated) {
                    Toast.makeText(this, "Weight updated!", Toast.LENGTH_SHORT).show();
                    loadWeights();
                } else {
                    Toast.makeText(this, "Failed to update weight", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter a weight", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /** Confirm deletion of a weight */
    private void confirmDelete(WeightItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Weight")
                .setMessage("Are you sure you want to delete this entry?\n" + item.toString())
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean deleted = db.deleteWeight(item.id);
                    if (deleted) {
                        Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        loadWeights();
                    } else {
                        Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Check if the new weight reaches the goal */
    private void checkGoal(double weight) {
        Double goal = db.getGoalWeight(username); // DBHelper method that takes username
        if (goal != null && weight <= goal) {
            Toast.makeText(this, "🎉 You reached your goal weight!", Toast.LENGTH_LONG).show();

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    String phoneNumber = "YOUR_PHONE_NUMBER"; // Replace with actual if needed
                    String message = "Congrats! You reached your goal weight of " + goal + " kg!";
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to send SMS", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
