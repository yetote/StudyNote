package com.example.mediacodecdemo;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
public class Codec {
    private MediaFormat mediaFormat;
    private MediaCodec mediaCodec;
    private int sampleRate, channelCount;
    private BlockingQueue<byte[]> audioQueue;
    private static final String TAG = "Codec";
    //    private MediaCodec.BufferInfo bufferInfo;
    private WriteFile writeFile;
    private boolean isRecording;

    public Codec(final int sampleRate, final int channelCount, String path) {
        this.sampleRate = sampleRate;
        this.channelCount = channelCount;
        writeFile = new WriteFile(path);
//        bufferInfo = new MediaCodec.BufferInfo();
        mediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channelCount);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, sampleRate * channelCount);
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 512 * 1024);
        try {
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
        } catch (IOException e) {
            e.printStackTrace();
        }
        audioQueue = new LinkedBlockingQueue<>();
        mediaCodec.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                Log.e(TAG, "onInputBufferAvailable: " + Thread.currentThread().getName());
                if (index >= 0) {
                    ByteBuffer inputBuffer = codec.getInputBuffer(index);
                    if (inputBuffer != null) {
                        inputBuffer.clear();
                        try {
                            inputBuffer.put(audioQueue.take());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (!isRecording && audioQueue.isEmpty()) {
                            Log.e(TAG, "onInputBufferAvailable: 最后一帧");
                            codec.queueInputBuffer(index, 0, sampleRate * channelCount, System.currentTimeMillis(), MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        } else {
                            Log.e(TAG, "onInputBufferAvailable: 发送数据");
                            codec.queueInputBuffer(index, 0, sampleRate * channelCount, System.currentTimeMillis(), 0);
                        }
                    }
                }
            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
//                Log.e(TAG, "onOutputBufferAvailable: " + Thread.currentThread().getName());
                Log.e(TAG, "onOutputBufferAvailable: 接受数据");
                if (index >= 0) {
                    ByteBuffer outputBuffer = codec.getOutputBuffer(index);
                    if (outputBuffer != null) {
                        Log.e(TAG, "onOutputBufferAvailable: size" + outputBuffer.limit());
                        outputBuffer.position(info.offset);
                        outputBuffer.limit(info.offset + info.size);
//                        Log.e(TAG, "onOutputBufferAvailable: "+info );
                        byte[] audioData = new byte[info.size + 7];
                        outputBuffer.get(audioData, 7, info.size);
                        addADTStoPacket(audioData, info.size + 7);
                        writeFile.write(audioData);
                    }

                }
                codec.releaseOutputBuffer(index, false);
                if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    mediaCodec.stop();
                    mediaCodec.release();
                    Log.e(TAG, "onOutputBufferAvailable: 结束");
                }
            }

            @Override
            public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

            }
        });
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

    }

    public void pushData(byte[] audioData) {
        Log.e(TAG, "pushData: 放入数据" + audioQueue.size());
        try {
            audioQueue.put(audioData);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startCodec() {

        mediaCodec.start();
        isRecording = true;
    }

    public void stop() {
        isRecording = false;
    }

    private static void addADTStoPacket(byte[] packet, int packetLen) {
        // AAC LC
        int profile = 2;
        // 48.0KHz
        int freqIdx = 3;
        // CPE
        int chanCfg = 2;

        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF1;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) | 0x1F);
        packet[6] = (byte) 0xFC;
    }
}
