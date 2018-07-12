package com.example.ether.videodemo3;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.util.Arrays;

import static android.media.AudioFormat.CHANNEL_IN_STEREO;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;
import static android.media.AudioTrack.STATE_UNINITIALIZED;


public class MainActivity extends AppCompatActivity {
    public static boolean isPlaying;
    private AudioRecord audioRecord;
    private Button start, stop, play;
    public static final int BUFFER_SIZE = 44100 * 20 / 1000 * 5 * 2 * 2;
    private WavWrite wavWrite;
    private String path;
    private static final String TAG = "MainActivity";
    public static final int AUDIO_RECORD_PERMISSION_CODE = 1;
    private boolean isRecording;
    private AudioTrack audioTrack;
    private Read readWav;
    private int offset = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        onClick();
    }

    private void onClick() {
        start.setOnClickListener(v -> {
            isRecording = true;
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_RECORD_PERMISSION_CODE);
            } else {
                startAudio();
            }
        });
        stop.setOnClickListener(v -> {
            isRecording = false;
            isPlaying = false;
            wavWrite.writeDataSize();
//            wavWrite.close();
        });
        play.setOnClickListener(v -> {
            readWav = new Read(path, BUFFER_SIZE);
            isPlaying = true;
            startPlay();
        });
    }

    private void startPlay() {
        readWav.readHeader();
        new Thread(() -> {
            while (isPlaying) {
                byte[] data = readWav.readData();
                if (data != null) {
                    audioTrack.write(data, 0, data.length);
                    audioTrack.play();
                } else {
                    isPlaying = false;
                }
//                isPlaying = false;
//                Log.e(TAG, "startPlay: " + 1);

            }
        }).start();
    }

    private void init() {
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        play = findViewById(R.id.play);
        path = this.getExternalCacheDir() + "/wavCache/test.wav";
        Log.e(TAG, "init: " + path);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioTrack = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(44100)
                            .setChannelMask(AudioFormat.CHANNEL_IN_STEREO)
                            .build())
                    .setBufferSizeInBytes(BUFFER_SIZE)
                    .build();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AUDIO_RECORD_PERMISSION_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startAudio();
                } else {
                    Log.e(TAG, "onRequestPermissionsResult: " + "申请权限失败");
                }
                break;
        }
    }

    private void startAudio() {

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, CHANNEL_IN_STEREO, ENCODING_PCM_16BIT, BUFFER_SIZE);
        wavWrite = new WavWrite(BUFFER_SIZE, path);
        if (audioRecord.getState() == STATE_UNINITIALIZED) {
            Toast.makeText(this, "录音器未初始化", Toast.LENGTH_SHORT).show();
        } else {
            audioRecord.startRecording();
            wavWrite.writeHeader(2, 44100);
            new Thread(() -> {
                byte[] buffer = new byte[BUFFER_SIZE];
                while (isRecording) {
                    int resultCode = audioRecord.read(buffer, 0, BUFFER_SIZE);
                    Log.e(TAG, "onClick: " + resultCode);
                    wavWrite.writeData(buffer);
                }
            }).start();

        }
    }
}
