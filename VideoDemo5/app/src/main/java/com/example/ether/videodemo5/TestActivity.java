package com.example.ether.videodemo5;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class TestActivity extends AppCompatActivity {
    private MediaExtractor audioExtractor, videoExtractor;
    private MediaMuxer mediaMuxer;
    private String audioPath, videoPath, syntheticPath;
    private int audioTrackIndex, videoTrackIndex;
    private int audioMuxerIndex, videoMuxerIndex;
    private int maxAudioSize, maxVideoSize;
    private Button btn;
    private static final String TAG = "TestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        init();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syntheticAudioAndVideo();
            }
        });
    }

    private void init() {
        btn = findViewById(R.id.btn);
        String parentPath = this.getExternalCacheDir().getPath();
        audioPath = parentPath + "/audio.mp4";
        videoPath = parentPath + "/video.mp4";
        syntheticPath = parentPath + "/synthetic.mp4";
        File file = new File(syntheticPath);
        try {
            if (!file.exists()) {
                file.getParentFile().mkdir();
                file.createNewFile();
            }
            mediaMuxer = new MediaMuxer(syntheticPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void syntheticAudioAndVideo() {
        // TODO: 2018/7/17 该方法会导致线程阻塞
        AudioTrackThread audioTrackThread = new AudioTrackThread();
        audioTrackThread.start();
        VideoTrackThread videoTrackThread = new VideoTrackThread();
        videoTrackThread.start();
        try {
            audioTrackThread.join();
            videoTrackThread.join();
            mediaMuxer.start();
            SyntheticThread syntheticThread = new SyntheticThread();
            syntheticThread.start();
            syntheticThread.join();
            close();
            Toast.makeText(this, "合成完毕", Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void close() {
        audioExtractor.release();
        videoExtractor.release();
        if (mediaMuxer != null) {
            mediaMuxer.stop();
            mediaMuxer.release();
        }
    }

    class AudioTrackThread extends Thread {
        @Override
        public void run() {
            super.run();
            audioExtractor = new MediaExtractor();
            try {
                audioExtractor.setDataSource(audioPath);
                int audioTrackCount = audioExtractor.getTrackCount();
                for (int i = 0; i < audioTrackCount; i++) {
                    MediaFormat format = audioExtractor.getTrackFormat(i);
                    String mimeType = format.getString(MediaFormat.KEY_MIME);
                    if (mimeType.startsWith("audio/")) {
                        audioTrackIndex = i;
                        audioMuxerIndex = mediaMuxer.addTrack(format);
                        maxAudioSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    class VideoTrackThread extends Thread {
        @Override
        public void run() {
            super.run();
            videoExtractor = new MediaExtractor();
            try {
                videoExtractor.setDataSource(videoPath);
                int videoTrackCount = videoExtractor.getTrackCount();
                for (int i = 0; i < videoTrackCount; i++) {
                    MediaFormat format = videoExtractor.getTrackFormat(i);
                    String mimeType = format.getString(MediaFormat.KEY_MIME);
                    if (mimeType.startsWith("video/")) {
                        videoTrackIndex = i;
                        videoMuxerIndex = mediaMuxer.addTrack(format);
                        maxVideoSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class SyntheticThread extends Thread {
        @Override
        public void run() {
            super.run();
            audioExtractor.selectTrack(audioTrackIndex);
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
            ByteBuffer audioBuffer = ByteBuffer.allocate(maxAudioSize);
            while (audioBuffer.hasRemaining()) {
                int audioSampleSize = audioExtractor.readSampleData(audioBuffer, 0);
                if (audioSampleSize < 0) {
                    audioExtractor.unselectTrack(audioTrackIndex);
                    break;
                }
                long sampleTime = audioExtractor.getSampleTime();
                if (sampleTime < 0) {
                    break;
                }
                audioBufferInfo.offset = 0;
                audioBufferInfo.size = audioSampleSize;
                audioBufferInfo.flags = audioExtractor.getSampleFlags();
                audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                Log.e(TAG, "run: " + audioBufferInfo.offset + "\n" + audioBufferInfo.size + "\n" + audioBufferInfo.flags + "\n" + audioBufferInfo.presentationTimeUs);
                mediaMuxer.writeSampleData(audioMuxerIndex, audioBuffer, audioBufferInfo);
                audioExtractor.advance();
            }

            videoExtractor.selectTrack(videoTrackIndex);
            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            ByteBuffer videoBuffer = ByteBuffer.allocate(maxVideoSize);
            while (videoBuffer.hasRemaining()) {
                int videoSampleSize = videoExtractor.readSampleData(videoBuffer, 0);
                if (videoSampleSize < 0) {
                    videoExtractor.unselectTrack(videoTrackIndex);
                    break;
                }
                long sampleTime = videoExtractor.getSampleTime();
                if (sampleTime < 0) {
                    break;
                }
                videoBufferInfo.offset = 0;
                videoBufferInfo.size = videoSampleSize;
                videoBufferInfo.flags = videoExtractor.getSampleFlags();
                videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                mediaMuxer.writeSampleData(videoMuxerIndex, videoBuffer, videoBufferInfo);
                videoExtractor.advance();
            }
        }
    }
}


