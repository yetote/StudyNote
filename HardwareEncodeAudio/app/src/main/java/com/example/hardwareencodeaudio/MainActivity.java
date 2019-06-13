package com.example.hardwareencodeaudio;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static android.media.MediaCodec.CONFIGURE_FLAG_ENCODE;
import static android.media.MediaFormat.MIMETYPE_AUDIO_AAC;

public class MainActivity extends AppCompatActivity {
    private Button start;
    private AudioRecord audioRecord;
    private boolean isRecording;
    private Thread recordThread, encodeThread;
    private ByteBuffer audioBuffer;
    private static final String TAG = "MainActivity";
    public static final int PERMISSION_RECORD_CODE = 1;
    private String path;
    private WriteFile writeFile;
    private BlockingQueue<byte[]> blockingQueue;
    private MediaCodec mediaCodec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = findViewById(R.id.start);
        audioBuffer = ByteBuffer.allocate(48000 * 2).order(ByteOrder.nativeOrder());
        path = getExternalCacheDir().getPath() + "/res/test.aac";
        writeFile = new WriteFile(path);
        blockingQueue = new LinkedBlockingDeque<>();

        initMediaCodec();

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
                    try {
                        blockingQueue.put(data);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.e(TAG, "run: resultCode" + resultCode);
                }
            }
        });

        encodeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                while (true) {
                    if (!isRecording && blockingQueue.isEmpty()) {
                        Log.e(TAG, "run: 编码完成");
                        break;
                    }
                    int inputIndex = mediaCodec.dequeueInputBuffer(-1);
                    Log.e(TAG, "run: 输入区缓冲索引" + inputIndex);
                    if (inputIndex >= 0) {
                        ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputIndex);
                        if (inputBuffer != null) {
                            try {
                                byte[] data = blockingQueue.take();
                                inputBuffer.clear();
                                inputBuffer.put(data);
                                mediaCodec.queueInputBuffer(inputIndex, 0, data.length, 0, 0);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }


                    int outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
                    while (outputIndex >= 0) {
                        Log.e(TAG, "run: 进入编码循环");
                        ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputIndex);
                        if (outputBuffer != null) {
                            outputBuffer.position(bufferInfo.offset);
                            outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                            byte[] outData = new byte[bufferInfo.size + 7];
                            outputBuffer.get(outData, 7, bufferInfo.size);
                            addADTStoPacket(outData, bufferInfo.size + 7);
                            writeFile.write(outData);
                        }
                        mediaCodec.releaseOutputBuffer(outputIndex, false);
                        Log.e(TAG, "run: outputIndex" + outputIndex);
                        outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
                    }
                }
                Log.e(TAG, "run: ?????");
            }
        });
    }

    private void initMediaCodec() {
        MediaFormat mediaFormat = MediaFormat.createAudioFormat(MIMETYPE_AUDIO_AAC, 48000, 2);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000);
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 512 * 1024);
        try {
            mediaCodec = MediaCodec.createEncoderByType(MIMETYPE_AUDIO_AAC);
            mediaCodec.configure(mediaFormat, null, null, CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                mediaCodec.start();
                encodeThread.start();
            } else {
                isRecording = false;
                audioRecord.stop();
            }
        }
    }

    public static void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = 3; // 44.1KHz
        int chanCfg = 2; // CPE

        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF1;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) | 0x1F);
        packet[6] = (byte) 0xFC;
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
