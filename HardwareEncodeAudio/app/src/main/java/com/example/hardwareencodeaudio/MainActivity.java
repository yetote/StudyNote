package com.example.hardwareencodeaudio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static android.media.AudioRecord.ERROR_DEAD_OBJECT;
import static android.media.AudioRecord.ERROR_INVALID_OPERATION;

public class MainActivity extends AppCompatActivity {
    private Button start;
    private AudioRecord audioRecord;
    private boolean isRecording;
    private Thread recordThread;
    private ByteBuffer audioBuffer;
    private static final String TAG = "MainActivity";
    public static final int PERMISSION_RECORD_CODE = 1;
    private String path;
    private WriteFile writeFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = findViewById(R.id.start);
        audioBuffer = ByteBuffer.allocate(48000 * 2).order(ByteOrder.nativeOrder());
        path = getExternalCacheDir().getPath() + "/res/test.pcm";
        writeFile = new WriteFile(path);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_RECORD_CODE);
                } else {
                    startRecord();

                }
            }
        });
        recordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] data = new byte[48000 * 2];
                while (isRecording) {
                    int resultCode = audioRecord.read(data, 0, 48000 * 2);
                    Log.e(TAG, "run: resultCode" + resultCode);
                    writeFile.write(data);
                }
            }
        });
    }

    private void startRecord() {
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 48000, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, 48000 * 2 * 2);

        if (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Toast.makeText(MainActivity.this, "audioRecord未初始化成功", Toast.LENGTH_SHORT).show();
        } else {
            if (!isRecording) {
                isRecording = true;
                audioRecord.startRecording();
                recordThread.start();
            } else {
                isRecording = false;
                audioRecord.stop();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_RECORD_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecord();
                } else {
                    Toast.makeText(this, "请允许录音权限", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
