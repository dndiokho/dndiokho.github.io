package com.example.dora_weightapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SmsPermissionActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 100;
    TextView tvStatus;
    Button btnEnableSms;

    Button btnSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permission);

        tvStatus = findViewById(R.id.tvStatus);
        btnEnableSms = findViewById(R.id.btnEnableSms);
        btnSkip = findViewById(R.id.btnSkip); // <-- new

        btnEnableSms.setOnClickListener(v -> requestSmsPermission());

        // Skip button behavior
        btnSkip.setOnClickListener(v -> goToGridScreen());
    }

    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            tvStatus.setText("Permission status: Granted");
            Toast.makeText(this, "SMS permission already granted!", Toast.LENGTH_SHORT).show();
            goToGridScreen();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tvStatus.setText("Permission status: Granted");
                Toast.makeText(this, "SMS permission granted!", Toast.LENGTH_SHORT).show();

                // Navigate to the weight grid after granting permission
                Intent intent = new Intent(SmsPermissionActivity.this, MonitorGridWeightActivity.class);
                startActivity(intent);
                finish();
            } else {
                tvStatus.setText("Permission status: Denied");
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();

                // Still continue to grid screen even if permission denied
                Intent intent = new Intent(SmsPermissionActivity.this, MonitorGridWeightActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }


    private void goToGridScreen() {
        Intent intent = new Intent(SmsPermissionActivity.this, MonitorGridWeightActivity.class);
        startActivity(intent);
        finish(); // close this screen so it doesn’t return here
    }
}
