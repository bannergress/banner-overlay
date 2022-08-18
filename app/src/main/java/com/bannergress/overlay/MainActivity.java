package com.bannergress.overlay;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> requestPermissionResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissionResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> startService(false)
        );

        startService(true);

        finish();
    }

    private void startService(boolean requestPermission) {
        if (Settings.canDrawOverlays(this)) {
            String data = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            Intent intent = new Intent(this, OverlayService.class);
            intent.putExtra(Intent.EXTRA_TEXT, data);
            startService(intent);
        } else if (requestPermission) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            requestPermissionResultLauncher.launch(intent);
        }
    }
}
