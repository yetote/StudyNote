package com.example.ether.videodemo9;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SecondActivity extends AppCompatActivity {
    private MediaCodec mediaCodec;
    private String path;
    private Button btn;
    private SurfaceView surfaceView;
    private boolean isDecoding = false;
    private FileRead read;
    private static final String TAG = "SecondActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        init();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDecoding = true;
                DecodeThread thread = new DecodeThread();
                thread.start();
            }
        });
    }

    private void init() {
        btn = findViewById(R.id.start);
        surfaceView = findViewById(R.id.view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        path = this.getExternalCacheDir().getPath() + "/test.h264";
        read = new FileRead(path, 800 * 600 * 2 / 3);
        try {
            mediaCodec = MediaCodec.createDecoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }

        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                MediaFormat format = MediaFormat.createVideoFormat("video/avc", 800, 600);
                byte[] header_sps = {0, 0, 0, 1, 103, 66, 0, 42, (byte) 149, (byte) 168, 30, 0, (byte) 137, (byte) 249, 102, (byte) 224, 32, 32, 32, 64};
                byte[] header_pps = {0, 0, 0, 1, 104, (byte) 206, 60, (byte) 128, 0, 0, 0, 1, 6, (byte) 229, 1, (byte) 151, (byte) 128};
                format.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
                format.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
                format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
                mediaCodec.configure(format, surfaceHolder.getSurface(), null, 0);
                mediaCodec.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

    }

    class DecodeThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (isDecoding) {
                int index = mediaCodec.dequeueInputBuffer(-1);
                if (index >= 0) {
                    ByteBuffer buffer = mediaCodec.getInputBuffer(index);
                    buffer.clear();
                    byte[] temp = new byte[600 * 800 * 2 / 3];
                    int result = read.readData(temp);
                    if (result == -1) {
                        Log.e(TAG, "run: 读取完成" );
                        break;
                    }
                    Log.e(TAG, "run: "+result );
                    buffer.put(temp);
                    mediaCodec.queueInputBuffer(index, 0, temp.length, System.nanoTime() / 1000, 0);
                }

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
//                bufferInfo.size = 600 * 800 * 2 / 3;
//                bufferInfo.offset = 0;
//                bufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
//                bufferInfo.presentationTimeUs += 1000 * 1000 / 30;
                int outIndex;
                do {
                    outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                    if (outIndex >= 0) {
                        mediaCodec.releaseOutputBuffer(outIndex, true);
                        Log.e(TAG, "run: " + outIndex);
                    }
                } while (outIndex >= 0);
            }
        }
    }
}
