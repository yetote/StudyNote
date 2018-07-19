package com.example.ether.videodemo8;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TestActivity extends AppCompatActivity {
    private String path;
    public static final int PERMISSION_RECORD_CODE = 1;
    private MediaCodec mediaCodec;
    private AudioRecord audioRecord;
    public static final int SAMPLE_RATE = 44100;
    public static final int RATE = 256000;
    private int bufferSize;
    public static final String MIME = "audio/mp4a-latm";
    public static final int CHANNEL_COUNT = 2;
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private Button start, stop, encode;
    private boolean isRecoding = false;
    private ByteBuffer buffer;
    private FileWrite write;
    private static final String TAG = "TestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecoding = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (isRecoding) {
                            startRecode();
                        }
                    }
                }).start();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecoding = false;
            }
        });
    }

    private void startRecode() {
        audioRecord.startRecording();
        int index = mediaCodec.dequeueInputBuffer(-1);
        if (index >= 0) {
            ByteBuffer buffer = mediaCodec.getInputBuffer(index);//取出队列中的缓冲区(空盒子)
            buffer.clear();
            int length = audioRecord.read(buffer, bufferSize);//将数据存入到空盒子中
            Log.e(TAG, "startRecode: " + length);
            if (length > 0) {
                mediaCodec.queueInputBuffer(index, 0, length, System.nanoTime() / 1000, 0);//将装满数据的盒子放到队列中
            }
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outIndex;
        do {
            outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            Log.e(TAG, "startRecode: " + outIndex);
            if (outIndex >= 0) {
                ByteBuffer buffer = mediaCodec.getOutputBuffer(outIndex);
                buffer.position(bufferInfo.offset);
                byte[] temp = new byte[bufferInfo.size + 7];
                buffer.get(temp, 7, bufferInfo.size);
                addADTStoPacket(temp, temp.length);
                write.writeData(temp);
                mediaCodec.releaseOutputBuffer(outIndex, false);
            }
        } while (outIndex >= 0);
    }

    private void init() {
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        encode = findViewById(R.id.encode);

        MediaFormat format = MediaFormat.createAudioFormat(MIME, SAMPLE_RATE, CHANNEL_COUNT);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_BIT_RATE, RATE);
        try {
            mediaCodec = MediaCodec.createEncoderByType(MIME);
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * 2;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);
        buffer = ByteBuffer.allocate(bufferSize);
        path = this.getExternalCacheDir().getPath() + "/test.aac";
        write = new FileWrite(bufferSize, path);
    }

    private void addADTStoPacket(byte[] packet, int size) {
        int profile = 2;
        int freqIdx = 4;
        int chanCfg = 2;
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (size >> 11));
        packet[4] = (byte) ((size & 0x7FF) >> 3);
        packet[5] = (byte) (((size & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }
}
