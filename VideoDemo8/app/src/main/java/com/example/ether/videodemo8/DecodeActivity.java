package com.example.ether.videodemo8;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.nio.ByteBuffer;

public class DecodeActivity extends AppCompatActivity {
    private Button start, play;
    public static final String MIME = "audio/mp4a-latm";
    private AudioTrack audioTrack;
    private String path;
    private String pcmPath;
    private MediaExtractor mediaExtractor;
    private MediaCodec mediaCodec;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_decode);
        init();
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void init() {
        start = findViewById(R.id.start_decode);
        play = findViewById(R.id.play);
        mediaExtractor = new MediaExtractor();


        audioTrack = new AudioTrack.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setAudioFormat(new AudioFormat.Builder().setEncoding());
    }

    class AudioDecodeThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                mediaExtractor.setDataSource(path);
                int trackCount = mediaExtractor.getTrackCount();
                for (int i = 0; i < trackCount; i++) {
                    MediaFormat format = mediaExtractor.getTrackFormat(i);
                    String mimeType = format.getString(MediaFormat.KEY_MIME);
                    if (mimeType.startsWith("audio/")) {
                        mediaExtractor.selectTrack(i);
                        mediaCodec = MediaCodec.createDecoderByType(mimeType);
                        mediaCodec.configure(format, null, null, 0);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            startDecode();

        }
    }

    private void startDecode() {
        int index = mediaCodec.dequeueInputBuffer(-1);
        if (index >= 0) {
            ByteBuffer buffer = mediaCodec.getInputBuffer(index);
            buffer.clear();
            int sampleSize = mediaExtractor.readSampleData(buffer, 0);
            if (sampleSize < 0) {
                Log.e(TAG, "startDecode: " + "数据全部读完");
                return;
            } else {
                mediaCodec.queueInputBuffer(index, 0, sampleSize, System.nanoTime() / 1000, 0);
                mediaExtractor.advance();
            }
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outIndex;
        ByteBuffer buffer;
        do {
            outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            buffer=mediaCodec.getOutputBuffer(outIndex);

        } while (outIndex >= 0);
    }
}
