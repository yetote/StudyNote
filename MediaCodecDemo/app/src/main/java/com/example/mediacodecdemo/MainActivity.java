package com.example.mediacodecdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Record record;
    public static final int PEMISSION_AUDIO_CODE = 1;
    private String path;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.tv);
        path = this.getExternalCacheDir().getPath() + "/res/test.aac";
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            record = new Record(2, path, 48000, this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PEMISSION_AUDIO_CODE);
        }
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record.startRecording();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PEMISSION_AUDIO_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    record = new Record(2, path, 48000, this);
                } else {
                    Toast.makeText(this, "必须开启权限", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
