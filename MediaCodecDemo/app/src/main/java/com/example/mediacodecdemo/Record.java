package com.example.mediacodecdemo;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * @author yetote QQ:503779938
 * @name MediaCodecDemo
 * @class name：com.example.mediacodecdemo
 * @class describe
 * @time 2019/6/13 17:01
 * @change
 * @chang time
 * @class describe
 */
public class Record {

    private int channelCount;
    private String path;
    private AudioRecord audioRecord;
    private int sampleRate;
    private Context context;
    private int channelLayout;
    private Thread thread;
    private boolean isRecording;
    private byte[] audioData;
    private static final String TAG = "Record";
    private Codec codec;

    public Record(final int channelCount, String path, final int sampleRate, Context context) {
        this.channelCount = channelCount;
        this.path = path;
        this.sampleRate = sampleRate;
        this.context = context;
        switch (channelCount) {
            case 1:
                channelLayout = AudioFormat.CHANNEL_IN_MONO;
                break;
            case 2:
                channelLayout = AudioFormat.CHANNEL_IN_STEREO;
                break;
            default:
                break;
        }
        codec = new Codec(sampleRate, channelCount,path);
        audioData = new byte[sampleRate * channelCount];
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelLayout, AudioFormat.ENCODING_PCM_16BIT, sampleRate * channelCount);
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRecording) {
                    audioRecord.read(audioData, 0, sampleRate * channelCount);
                    codec.pushData(audioData);
                }
                audioRecord.stop();
                Log.e(TAG, "run: 录音结束");
            }
        });
    }

    public void startRecording() {
        isRecording = true;
        audioRecord.startRecording();
        thread.start();
        codec.startCodec();
    }

    public void stop() {
        isRecording = false;
        codec.stop();
    }
}
