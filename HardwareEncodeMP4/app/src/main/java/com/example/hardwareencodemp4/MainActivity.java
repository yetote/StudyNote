package com.example.hardwareencodemp4;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private MediaMuxer mediaMuxer;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        path = getExternalCacheDir().getPath() + "/res/test.mp4";
        initMediaMuxer();

    }

    private void initMediaMuxer() {
        MediaFormat audioFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 48000, 2);
        MediaFormat videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 1280, 640);
        try {
            mediaMuxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
