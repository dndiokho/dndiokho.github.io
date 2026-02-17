package com.example.dora_weightapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DBNAME = "weightTracker.db";
    private static final int VERSION = 2; // increment version so onUpgrade triggers
    private Context context;

    public DBHelper(Context context) {
        super(context, DBNAME, null, VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables with username linkage
        db.execSQL("CREATE TABLE users(username TEXT PRIMARY KEY, password TEXT)");
        db.execSQL("CREATE TABLE weights(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, date TEXT, weight REAL)");
        db.execSQL("CREATE TABLE goal(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, goalWeight REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS weights");
        db.execSQL("DROP TABLE IF EXISTS goal");
        onCreate(db);
    }

    // Get current logged-in username from SharedPreferences
    private String getCurrentUsername() {
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return prefs.getString("username", null);
    }

    // USER METHODS
    public boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        long result = db.insert("users", null, values);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username=? AND password=?", new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean checkUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username=?", new String[]{username});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // WEIGHT METHODS
    public boolean insertWeight(String username, String date, double weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("date", date);
        values.put("weight", weight);
        long result = db.insert("weights", null, values);
        return result != -1;
    }

    public Cursor getAllWeights(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM weights WHERE username=? ORDER BY id DESC", new String[]{username});
    }

    // GOAL METHODS
    public boolean setGoalWeight(double goalWeight) {
        SQLiteDatabase db = this.getWritableDatabase();
        String username = getCurrentUsername();

        // Delete old goal for this user
        db.delete("goal", "username=?", new String[]{username});

        // Insert new goal
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("goalWeight", goalWeight);
        long result = db.insert("goal", null, values);
        return result != -1;
    }

    // Delete a weight by its ID
    public boolean deleteWeight(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("weights", "id=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    // Get goal weight for a specific user
    public Double getGoalWeight(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT goalWeight FROM goal WHERE username=? LIMIT 1", new String[]{username});
        if (cursor.moveToFirst()) {
            double goal = cursor.getDouble(0);
            cursor.close();
            return goal;
        }
        cursor.close();
        return null;
    }

    // update weight
    public boolean updateWeight(int id, double newWeight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("weight", newWeight);
        int rows = db.update("weights", values, "id=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }
}
