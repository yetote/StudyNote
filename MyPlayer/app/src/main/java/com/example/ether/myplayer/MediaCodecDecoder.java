package com.example.ether.myplayer;

import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.opengl.GLES20;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author yetote QQ:503779938
 * @name MyPlayer
 * @class name：com.example.ether.myplayer
 * @class 解码器
 * @time 2018/8/30 11:25
 * @change
 * @chang time
 * @class describe
 */
public class MediaCodecDecoder implements SurfaceTexture.OnFrameAvailableListener {
    private static final int ERROR_OK = 0;
    private static final int ERROR_EOF = 1;
    private static final int ERROR_UNUSUAL = 3;
    private MediaCodec.BufferInfo bufferInfo;
    private boolean isOpen = false;
    private static final String TAG = "MediaCodecDecoder";
    private MediaExtractor mediaExtractor;
    private int videoTrackIndex = -1;
    private MediaFormat mediaFormat;
    private long duration;
    private SurfaceTexture surfaceTexture;
    private boolean isFrameAvailable = false;
    private final Object frameSyncObject = new Object();
    private Surface surface;
    private MediaCodec mediaCodec;
    private boolean isCodecStart;
    private ByteBuffer[] mediaCodecInputBuffer;
    private boolean isInputBufferQueued = false;
    private static final String VIDEO_MIME_TYPE = "video/avc";
    private boolean isSawInputEOS = false;
    private int pendingInputFrameCount;
    private static final int ERROR_FAIL = 2;
    private boolean isSawOutputEOS = false;
    private long timestampOfLastDecodedFrame;
    private long timestampOfCurTexFrame;
    private int deadDecoderCounter;

    MediaCodecDecoder() {
        bufferInfo = new MediaCodec.BufferInfo();
    }

    /**
     * 打开文件
     *
     * @param path      文件路径
     * @param textureId 纹理id
     * @return 是否成功
     */
    public boolean openFile(String path, int textureId) {
        if (!isOpen) {
            Log.e(TAG, "openFile: " + "您不能多次打开文件");
        }
        try {
            mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(path);
        } catch (IOException e) {
            Log.e(TAG, "openFile:添加资源失败 " + e);
            closeFile();
            return false;
        }

        /*寻找视频轨*/
        int numTracks = mediaExtractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = mediaExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("/video")) {
                videoTrackIndex = i;
                break;
            }
        }
        if (videoTrackIndex < 0) {
            Log.e(TAG, "chooseVideo: 未找到视频轨，请检查资源");
            closeFile();
            return false;
        }
        mediaExtractor.selectTrack(videoTrackIndex);
        mediaFormat = mediaExtractor.getTrackFormat(videoTrackIndex);
        duration = mediaFormat.getLong(MediaFormat.KEY_DURATION);
        String mime = mediaFormat.getString(MediaFormat.KEY_MIME);

        /*将视频轨绑定到纹理中*/
        surfaceTexture = new SurfaceTexture(textureId);
        surfaceTexture.setOnFrameAvailableListener(this);
        surface = new Surface(surfaceTexture);

        if (setupDecoder(mime)) {
            closeFile();
            return false;
        }
        return true;
    }

    /**
     * 配置解码器
     *
     * @param mime 数据类型
     * @return 配置信息
     */
    private boolean setupDecoder(String mime) {
        try {

            mediaCodec = MediaCodec.createDecoderByType(mime);
            mediaCodec.configure(mediaFormat, surface, null, 0);
            mediaCodec.start();
            isCodecStart = true;

            // TODO: 2018/8/30 方法过时，修改
            mediaCodecInputBuffer = mediaCodec.getInputBuffers();

        } catch (IOException e) {
            Log.e(TAG, "setupDecoder: 解码器配置失败" + e);
            cleanUpDecoder();
            return true;
        }
        return false;
    }


    /**
     * 关闭文件
     */
    private void closeFile() {
        cleanUpDecoder();
        if (surface != null) {
            surface.release();
            surface = null;
        }
        if (surfaceTexture != null) {
            surfaceTexture.release();
            surfaceTexture = null;
        }
        if (mediaExtractor != null) {
            mediaExtractor.release();
            mediaExtractor = null;
            videoTrackIndex = -1;
            mediaFormat = null;
            duration = 0;
        }

    }

    /**
     * 清理解码器配置
     */
    void cleanUpDecoder() {
        if (mediaCodec != null) {
            if (isCodecStart) {
                if (isInputBufferQueued) {
                    mediaCodec.flush();
                    isInputBufferQueued = false;
                }
                mediaCodec.stop();
                isCodecStart = false;
                mediaCodecInputBuffer = null;
            }
            mediaCodec.release();
            mediaCodec = null;
        }
        // TODO: 2018/8/30 一大堆莫名其妙的数据
        timestampOfLastDecodedFrame = Long.MIN_VALUE;
        timestampOfCurTexFrame = Long.MIN_VALUE;
//        firstPlaybackTexFrameUnconsumed = false;
        pendingInputFrameCount = 0;
        isSawInputEOS = false;
        isSawOutputEOS = false;
    }

    static boolean isInAndroidHardwareBlacklist() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        Log.e(TAG, manufacturer);
        Log.e(TAG, model);

        // too slow
        if ((manufacturer.compareTo("Meizu") == 0) && (model.compareTo("m2") == 0)) {
            return true;
        }

        if ((manufacturer.compareTo("Meizu") == 0) && (model.compareTo("M351") == 0)) {
            return true;
        }

        if ((manufacturer.compareTo("HUAWEI") == 0) && (model.compareTo("SUR-TL01H") == 0)) {
            return true;
        }

        if ((manufacturer.compareTo("Xiaomi") == 0) && (model.compareTo("MI 4W") == 0)) {
            return true;
        }
        //Mali-T720
        if ((manufacturer.compareTo("HUAWEI") == 0) && (model.compareTo("HUAWEI TAG-AL00") == 0)) {
            return true;
        }
        //Mali-T760
        if ((manufacturer.compareTo("samsung") == 0) && (model.compareTo("SM-G9250") == 0)) {
            return true;
        }
        //Vivante GC2000
        if ((manufacturer.compareTo("Coolpad") == 0) && (model.compareTo("Coolpad 8720L") == 0)) {
            return true;
        }
        //PowerVR SGX 544MP
        if ((manufacturer.compareTo("samsung") == 0) && (model.compareTo("GT-I9500") == 0)) {
            return true;
        }
        //Mali-450 MP
        return (manufacturer.compareTo("BBK") == 0) && (model.compareTo("vivo X5L") == 0);

    }

    /**
     * 通过底层创建解码器
     *
     * @param width   宽度
     * @param height  高度
     * @param texId   纹理id
     * @param sps     sps帧是数据
     * @param spsSize sps数据大小
     * @param pps     pps帧数据
     * @param ppsSize pps数据大小
     * @return 创建是否成功
     */
    boolean createVideoDecoder(int width, int height, int texId, byte[] sps, int spsSize, byte[] pps, int ppsSize) {
        mediaFormat = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, width, height);

        /*添加sps，pps信息*/
        mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(sps));
        mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(pps));

        surfaceTexture = new SurfaceTexture(texId);
        surfaceTexture.setOnFrameAvailableListener(this);
        surface = new Surface(surfaceTexture);
        if (setupDecoder(VIDEO_MIME_TYPE)) {
            closeFile();
            return false;
        }

        return true;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (frameSyncObject) {
            if (isFrameAvailable) {
                Log.e(TAG, "onFrameAvailable: " + "帧数据已被取出");
            }
            isFrameAvailable = true;
            frameSyncObject.notify();
        }
    }

    /**
     * 当需要解码一个packet(compressedData)的时候调用这个方法
     *
     * @param frameData 帧数据
     * @param inputSize 数据大小
     * @param timeStamp 时间戳
     * @return 解码数据大小
     */
    int decodeFrame(byte[] frameData, int inputSize, long timeStamp) {
        if (mediaCodecInputBuffer == null) {
            return 0;
        }
        boolean bUnusual = false;

        /*向解码队列里面添加数据*/
        final int inputBufferCount = mediaCodecInputBuffer.length;
        final int pendingInputBufferThreshold = Math.max(inputBufferCount / 3, 2);
        final int timeoutUsec = 4000;
        if (!isSawInputEOS) {
            int indexInputBuffer = mediaCodec.dequeueInputBuffer(-1);
            if (indexInputBuffer >= 0) {
                ByteBuffer buffer = mediaCodec.getInputBuffer(indexInputBuffer);
                if (inputSize == 0) {
                    /*输入数据为0的时候意味着解码完成，向解码器中添加一个空帧作为结束帧*/
                    mediaCodec.queueInputBuffer(indexInputBuffer, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    isSawInputEOS = true;
                } else {
                    assert buffer != null;
                    buffer.clear();
                    buffer.put(frameData, 0, inputSize);
                    mediaCodec.queueInputBuffer(indexInputBuffer, 0, inputSize, timeStamp, 0);
                    isInputBufferQueued = true;
                    ++pendingInputFrameCount;
                }
            } else {
                bUnusual = true;
            }
        }
        int dequeueTimeoutUs;
        if (pendingInputFrameCount > pendingInputBufferThreshold || isSawInputEOS) {
            dequeueTimeoutUs = timeoutUsec;
        } else {
            // NOTE: Too few input frames has been queued and the decoder has
            // not yet seen input EOS
            // wait dequeue for too long in this case is simply wasting time.
            // 看了下注释，大致的意思是为了防止帧数据太少而队列一直等待浪费资源，所以这里讲队列的等待的时间设置为0
            dequeueTimeoutUs = 0;
        }
        int decoderStatus = mediaCodec.dequeueOutputBuffer(bufferInfo, dequeueTimeoutUs);
        if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
            // No output available yet
            Log.d(TAG, "No output from decoder available");
        } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            // Not important for us, since we're using Surface
            Log.d(TAG, "Decoder output buffers changed");
        } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            MediaFormat newFormat = mediaCodec.getOutputFormat();
            Log.d(TAG, "Decoder output format changed: " + newFormat);
        } else if (decoderStatus < 0) {
            Log.e(TAG, "Unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus);
            return ERROR_FAIL;
        } else { // decoderStatus >= 0
            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d(TAG, "Output EOS");
                isSawOutputEOS = true;
            }
            boolean doRender = false;
            if (!isSawOutputEOS) {
                //时间戳
                timestampOfLastDecodedFrame = bufferInfo.presentationTimeUs;
                --pendingInputFrameCount;
                doRender = true;
            }
            mediaCodec.releaseOutputBuffer(decoderStatus, doRender);
            if (doRender) {
                if (awaitNewImage()) {
                    timestampOfCurTexFrame = bufferInfo.presentationTimeUs;
                    Log.d(TAG, "Surface texture updated, pts=" + timestampOfCurTexFrame);
                    return ERROR_OK;
                } else {
                    Log.e(TAG, "Render decoded frame to surface texture failed!");
                    return ERROR_FAIL;
                }
            } else {
                return ERROR_EOF;
            }
        }
        if (bUnusual) {
            return ERROR_UNUSUAL;
        } else {
            return ERROR_FAIL;
        }

    }

    private boolean awaitNewImage() {
        synchronized (frameSyncObject) {
            while (isFrameAvailable) {
                try {
                    frameSyncObject.wait();
                    if (isFrameAvailable) {
                        Log.e(TAG, "帧数据等待超时");
                        return false;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e(TAG, "" + e.getMessage());
                    e.printStackTrace();
                    return false;
                }
            }
            isFrameAvailable = false;
        }
        final int glErr = GLES20.glGetError();
        if (glErr != GLES20.GL_NO_ERROR) {
            Log.e(TAG, "Before updateTexImage(): glError " + glErr);
        }
        Log.d(TAG, "frame is available, need updateTexImage");
        return true;
    }

    long updateTextureImage() {
        surfaceTexture.updateTexImage();
        return timestampOfCurTexFrame;
    }
}
