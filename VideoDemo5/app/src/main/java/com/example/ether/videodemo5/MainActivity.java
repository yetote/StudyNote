package com.example.ether.videodemo5;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String parentPath = this.getExternalCacheDir().getPath();
        String testPath = parentPath + "/test.mp4";
        String audioPath = parentPath + "/Audio.mp4";
        String videoPath = parentPath + "/video.mp4";

        File file = new File(testPath);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdir();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        combineTwoVideos(audioPath, 0, videoPath, file);

    }

    private static void combineTwoVideos(String audioPath, long audioStartTime, String frameVideoPath, File combinedVideoOutFile) {
        int mainAudioExtractorTrackIndex = -1; //提供音频的视频的音频轨（有点拗口）
        int mainAudioMuxerTrackIndex = -1; //合成后的视频的音频轨
        int mainAudioMaxInputSize = 0; //能获取的音频的最大值

        MediaExtractor frameVideoExtractor = new MediaExtractor();
        MediaExtractor audioVideoExtractor = new MediaExtractor();
        int frameExtractorTrackIndex = -1; //视频轨
        int frameMuxerTrackIndex = -1; //合成后的视频的视频轨
        int frameMaxInputSize = 0; //能获取的视频的最大值
        int frameRate = 0; //视频的帧率
        long frameDuration = 0;

        MediaMuxer muxer = null; //用于合成音频与视频
        try {
            muxer = new MediaMuxer(combinedVideoOutFile.getPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            audioVideoExtractor.setDataSource(audioPath);
            int audioTrackCount = audioVideoExtractor.getTrackCount();
            for (int i = 0; i < audioTrackCount; i++) {
                MediaFormat mediaFormat = audioVideoExtractor.getTrackFormat(i);
                String mimeType = mediaFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith("audio/")) {
                    mainAudioExtractorTrackIndex = i;
                    mainAudioMuxerTrackIndex = muxer.addTrack(mediaFormat);
                    mainAudioMaxInputSize = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                }
            }

            frameVideoExtractor.setDataSource(frameVideoPath);
            int trackCount = frameVideoExtractor.getTrackCount();
            for (int i = 0; i < trackCount; i++) {
                MediaFormat format = frameVideoExtractor.getTrackFormat(i);
                String mimeType = format.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith("video/")) {
                    frameExtractorTrackIndex = i;
                    frameMuxerTrackIndex = muxer.addTrack(format);
                    frameMaxInputSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                    frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE); //获取视频的帧率
                    frameDuration = format.getLong(MediaFormat.KEY_DURATION); //获取视频时长
                }
            }
            muxer.start();

            audioVideoExtractor.selectTrack(mainAudioExtractorTrackIndex);
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
            ByteBuffer buffer = ByteBuffer.allocate(mainAudioMaxInputSize);
            for (; ; ) {
                int readSampleSize = audioVideoExtractor.readSampleData(buffer, 0);
                if (readSampleSize < 0) {
                    audioVideoExtractor.unselectTrack(mainAudioExtractorTrackIndex);
                    break;
                }
                long sampleTime = audioVideoExtractor.getSampleTime();
                if (sampleTime < audioStartTime) {
                    audioVideoExtractor.advance();
                    continue;
                }
                if (sampleTime > audioStartTime + frameDuration) {
                    break;
                }
                audioBufferInfo.size = readSampleSize;
                audioBufferInfo.offset = 0;
                audioBufferInfo.flags = audioVideoExtractor.getSampleFlags();
                audioBufferInfo.presentationTimeUs = sampleTime - audioStartTime;
                muxer.writeSampleData(mainAudioMuxerTrackIndex, buffer, audioBufferInfo);
                audioVideoExtractor.advance();

            }

            frameVideoExtractor.selectTrack(frameExtractorTrackIndex);
            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            ByteBuffer videoBuffer = ByteBuffer.allocate(frameMaxInputSize);
            for (; ; ) {
                int readSampleSize = frameVideoExtractor.readSampleData(videoBuffer, 0);
                if (readSampleSize < 0) {
                    frameVideoExtractor.unselectTrack(frameExtractorTrackIndex);
                    break;
                }
                videoBufferInfo.size = readSampleSize;
                videoBufferInfo.offset = 0;
                videoBufferInfo.flags = frameVideoExtractor.getSampleFlags();
                videoBufferInfo.presentationTimeUs += 1000 * 1000 / frameRate;

                muxer.writeSampleData(frameMuxerTrackIndex, videoBuffer, videoBufferInfo);
                frameVideoExtractor.advance();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            audioVideoExtractor.release();
            frameVideoExtractor.release();
            if (muxer != null) {
                muxer.stop();
                muxer.release();
            }
            Log.e(TAG, "combineTwoVideos: " + "finish");
        }

    }

}
