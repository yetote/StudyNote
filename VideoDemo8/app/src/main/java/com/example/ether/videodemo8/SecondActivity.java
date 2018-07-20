package com.example.ether.videodemo8;

import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SecondActivity extends AppCompatActivity {
    private Button start, play;
    public static final String MIME = "audio/mp4a-latm";
    private AudioTrack audioTrack;
    private String path;
    private String pcmPath;
    private MediaExtractor mediaExtractor;
    private MediaCodec mediaCodec;
    private boolean isDecoding = false;
    private static final String TAG = "SecondActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        init();
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDecoding = true;
                AudioDecodeThread audioDecodeThread = new AudioDecodeThread();
                audioDecodeThread.start();
            }
        });
    }

    private void init() {
        start = findViewById(R.id.start_decode);
        play = findViewById(R.id.play);
        mediaExtractor = new MediaExtractor();
        pcmPath = this.getExternalCacheDir().getPath() + "/test.pcm";
        path = this.getExternalCacheDir().getPath() + "/test.aac";
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
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaCodec.start();
            while (isDecoding) {
                startDecode();
            }
        }
    }

    private void startDecode() {
        int index = mediaCodec.dequeueInputBuffer(-1);
        ByteBuffer inputBuffer;
        if (index >= 0) {
            inputBuffer = mediaCodec.getInputBuffer(index);
            inputBuffer.clear();
            int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);
            if (sampleSize < 0) {
                Log.e(TAG, "startDecode: " + "数据全部读完");
                isDecoding = false;
                return;
            } else {
                mediaCodec.queueInputBuffer(index, 0, sampleSize, System.nanoTime() / 1000, 0);
                mediaExtractor.advance();
            }
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outIndex;
        do {
            outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            Log.e(TAG, "startDecode: " + outIndex);
            if (outIndex >= 0) {
                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outIndex);
                FileWrite write = new FileWrite(bufferInfo.size, pcmPath);
                byte[] outputPcm = new byte[bufferInfo.size];
//                Log.e(TAG, "startDecode: "+outputBuffer.get(outputPcm, 0, bufferInfo.size) );
                outputBuffer.position(bufferInfo.offset);
                outputBuffer.get(outputPcm, 0, bufferInfo.size);
                write.writeData(outputPcm);
                mediaCodec.releaseOutputBuffer(outIndex, false);
            }
        } while (outIndex >= 0);
    }
}
