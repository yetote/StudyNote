package com.example.ether.videodemo8;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    public static final int SAMPLE_RATE = 44100;
    public static final int CHANNEL_COUNT = 2;
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize;
    private AudioRecord audioRecord;
    public static final String MIME = "audio/mp4a-latm";
    public static final int RATE = 256000;
    private MediaCodec mediaCodec;
    private Button start, stop, encode;
    public static final int PERMISSION_AUDIO_RECORD_CODE = 1;
    private boolean isRecord = false;
    private String path;
    private FileWrite write;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_AUDIO_RECORD_CODE);
                } else {
                    isRecord = true;
                    audioRecord.startRecording();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (isRecord) {
                                startEncode();
                            }
                        }
                    }).start();
                }
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecord = false;
            }
        });
    }

    private void init() {
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        encode = findViewById(R.id.encode);
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * 2;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);
        path = this.getExternalCacheDir().getPath() + "/test.aac";
        write = new FileWrite(bufferSize, path);
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
    }


    void startEncode() {
        int index = mediaCodec.dequeueInputBuffer(-1);//获取可使用缓冲区位置索引
        if (index >= 0) {
            final ByteBuffer buffer = mediaCodec.getInputBuffer(index);//获取可用的缓冲区
            buffer.clear();
            int length = audioRecord.read(buffer, bufferSize);
            if (length > 0) {
                mediaCodec.queueInputBuffer(index, 0, length, System.nanoTime() / 1000, 0);//输入流如队列
            } else {
                Log.e("wuwang", "length-->" + length);
            }
        }
        MediaCodec.BufferInfo mInfo = new MediaCodec.BufferInfo();
        int outIndex;
        do {
            outIndex = mediaCodec.dequeueOutputBuffer(mInfo, 0);//获取可用的输出区缓冲索引
            Log.e("wuwang", "audio flag---->" + mInfo.flags + "/" + outIndex);
            if (outIndex >= 0) {
                ByteBuffer buffer = mediaCodec.getOutputBuffer(outIndex);
                buffer.position(mInfo.offset);
                byte[] temp = new byte[mInfo.size + 7];
                buffer.get(temp, 7, mInfo.size);
                addADTStoPacket(temp, temp.length);
                write.writeData(temp);
                mediaCodec.releaseOutputBuffer(outIndex, false);
            } else if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {

            } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

            }
        } while (outIndex >= 0);
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
