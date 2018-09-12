#include <jni.h>
#include <android/log.h>

extern "C" {
#include <libswresample/swresample.h>
#include <libavformat/avformat.h>
#include <libavutil/imgutils.h>
#include <libswscale/swscale.h>
}

#define LOGI(FORMAT, ...) __android_log_print(ANDROID_LOG_INFO,"wuhuannan",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"wuhuannan",FORMAT,##__VA_ARGS__);
#define MAX_AUDIO_FRAME_SIZE 44100*4
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_ether_ndkdemo_MainActivity_add(JNIEnv *env, jobject instance, jint a, jint b) {
    int c = a + b;
    int d = a - b;
    int f = c + d;
    return f;

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_ether_ndkdemo_MainActivity_decodeVideo(JNIEnv *env, jobject instance,
                                                        jstring inputPath_, jstring outputPath_) {
    const char *inputPath = env->GetStringUTFChars(inputPath_, 0);
    const char *outputPath = env->GetStringUTFChars(outputPath_, 0);


    //1、初始化
    av_register_all();
    //2、封装格式上下文，统领全局的结构体，保存了视频文件封装格式的相关信息
    AVFormatContext *pFormatContext = avformat_alloc_context();
//    3、打开视频
    if (avformat_open_input(&pFormatContext, inputPath, NULL, NULL) != 0) {
        LOGE("无法打开输入视频文件");
        return;
    }
    //3.获取视频文件信息，例如得到视频的宽高
    //第二个参数是一个字典，表示你需要获取什么信息，比如视频的元数据
    if (avformat_find_stream_info(pFormatContext, NULL) < 0) {
        LOGE("%s", "无法获取视频文件信息");
        return;
    }
    //获取视频流的索引位置
    //遍历所有类型的流（音频流、视频流、字幕流），找到视频流
    int i = 0;
    int video_stream_idx = -1;
    for (; i < pFormatContext->nb_streams; ++i) {
        //流的类型
        if (pFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            video_stream_idx = i;
            break;
        }
    }
    if (video_stream_idx == -1) {
        LOGE("未找到视频流，请检查视频信息");
        return;
    }
    //只有知道视频的编码方式，才能够根据编码方式去找到解码器
    //获取视频流信息
    AVStream *pStream = pFormatContext->streams[video_stream_idx];
    //获取视频流信息中的编码信息id
    AVCodec *pCodec = avcodec_find_decoder(pStream->codecpar->codec_id);
    if (pCodec == NULL) {
        LOGE("找不到解码器");
        return;
    }
    //获取视频流中的编解码上下文
    AVCodecContext *pCodecCtx = avcodec_alloc_context3(pCodec);
    //将视频流信息复制到编解码的上下文
    avcodec_parameters_to_context(pCodecCtx, pStream->codecpar);
    //输出下视频信息
    LOGI("视频的文件格式：%s", pFormatContext->iformat->name);
    LOGI("视频时长：%lld", (pFormatContext->duration) / 1000000);
    LOGI("视频的宽高：%d,%d", pCodecCtx->width, pCodecCtx->height);
    LOGE("宽:%d,高:%d", pCodecCtx->width, pCodecCtx->height);
    LOGI("解码器的名称：%s", pCodec->name);
    if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
        LOGE("%s", "无法打开解码器");
        return;
    }
    //准备读取
    //AVPacket用于存储一帧一帧的压缩数据（H264）
    //缓冲区，开辟空间
    //将NULL指针转换为AVPacket类型指针
    //todo 分配内存，未释放
    AVPacket *pPacket = static_cast<AVPacket *>(av_malloc(sizeof(AVPacket)));
    //AVFrame用于存储解码后的像素数据(YUV)
    //内存分配
    //todo 分配内存，未释放
    AVFrame *pFrame = av_frame_alloc();
    //YUV420
    //todo 分配内存，未释放
    AVFrame *pFrameYUV = av_frame_alloc();
    //只有指定了AVFrame的像素格式、画面大小才能真正分配内存
    //缓冲区分配内存
    //todo 分配内存，未释放
    u_int8_t *output_buffer = static_cast<u_int8_t *>(av_malloc(
            av_image_get_buffer_size(AV_PIX_FMT_YUV420P, pCodecCtx->width, pCodecCtx->height, 1)));
    //向缓冲区装填数据
    av_image_fill_arrays(pFrame->data, pFrame->linesize, output_buffer, AV_PIX_FMT_YUV420P,
                         pCodecCtx->width, pCodecCtx->height, 1);
    //用于转码（缩放）的参数，转之前的宽高，转之后的宽高，格式等
    struct SwsContext *sws_ctx = sws_getContext(pCodecCtx->width,
                                                pCodecCtx->height,
                                                pCodecCtx->pix_fmt,
                                                pCodecCtx->width,
                                                pCodecCtx->height,
                                                AV_PIX_FMT_YUV420P,
                                                SWS_BICUBIC, NULL, NULL, NULL);

    int got_picture, ret, state;

    FILE *fp_yuv = fopen(outputPath, "wb+");
    int frame_count = 0;
    //6.一帧一帧的读取压缩数据
    while (av_read_frame(pFormatContext, pPacket) >= 0) {
        //只对视频进行解码（根据流的索引位置判断）
        if (pPacket->stream_index == video_stream_idx) {
            //7.解码一帧视频压缩数据，得到视频像素数据
            ret = avcodec_send_packet(pCodecCtx, pPacket);
            while (ret >= 0) {
                //接受解码后的数据
                ret = avcodec_receive_frame(pCodecCtx, pFrameYUV);
                LOGE("%d", ret);
                if (ret == AVERROR(EAGAIN)) {
                    LOGE("%s", "读取解码数据失败");
                    break;
                } else if (ret == AVERROR_EOF) {
                    LOGE("%s", "解码完成");
                    fclose(fp_yuv);
                    break;
                } else if (ret < 0) {
                    LOGE("%s", "解码出错");
                    break;
                }
                //AVFrame转为像素格式YUV420，宽高
                //2 6输入、输出数据
                //3 7输入、输出画面一行的数据的大小 AVFrame 转换是一行一行转换的
                //4 输入数据第一列要转码的位置 从0开始
                //5 输入画面的高度
                sws_scale(sws_ctx, pFrameYUV->data, pFrameYUV->linesize, 0, pCodecCtx->height,
                          pFrameYUV->data, pFrameYUV->linesize);
                //输出到YUV文件
                //AVFrame像素帧写入文件
                //data解码后的图像像素数据（音频采样数据）
                //Y 亮度 UV 色度（压缩了） 人对亮度更加敏感
                //U V 个数是Y的1/4
                int y_size = pCodecCtx->width * pCodecCtx->height;
                fwrite(pFrameYUV->data[0], 1, y_size, fp_yuv);
                fwrite(pFrameYUV->data[1], 1, y_size / 4, fp_yuv);
                fwrite(pFrameYUV->data[2], 1, y_size / 4, fp_yuv);
                frame_count++;
                LOGE("解码到%d帧", frame_count);
            }
        }
    }
    //释放资源
    av_packet_unref(pPacket);
    fclose(fp_yuv);

    av_frame_free(&pFrame);
    av_frame_free(&pFrameYUV);
    avcodec_free_context(&pCodecCtx);
    avformat_free_context(pFormatContext);

    env->ReleaseStringUTFChars(inputPath_, inputPath);
    env->ReleaseStringUTFChars(outputPath_, outputPath);
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_ether_ndkdemo_MainActivity_ioTest(JNIEnv *env, jobject instance, jstring ioPath_) {
    const char *ioPath = env->GetStringUTFChars(ioPath_, 0);

    FILE *file = fopen(ioPath, "wb");
    char data[] = "hello world";
    int i = fwrite(data, 1, sizeof(data), file);
    LOGE("%d", i);
    fclose(file);
    env->ReleaseStringUTFChars(ioPath_, ioPath);
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_ether_ndkdemo_MainActivity_decodeAudio(JNIEnv *env, jobject instance,
                                                        jstring inputPath_, jstring outputPath_) {
    const char *inputPath = env->GetStringUTFChars(inputPath_, 0);
    const char *outputPath = env->GetStringUTFChars(outputPath_, 0);
    //初始化组件
    av_register_all();

    //打开音视频文件信息
    AVFormatContext *pFormatCtx = avformat_alloc_context();

    if (avformat_open_input(&pFormatCtx, inputPath, NULL, NULL) != 0) {
        LOGE("无法打开音频文件");
        return;
    }
    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
        LOGE("无法获取输入文件信息");
    }
    int audio_stream_idx = -1;
    for (int i = 0; i < pFormatCtx->nb_streams; ++i) {
        if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
            audio_stream_idx = i;
            break;
        }
    }
    if (audio_stream_idx == -1) {
        LOGE("寻找音频流失败");
    }

    AVStream *pStream = pFormatCtx->streams[audio_stream_idx];
    AVCodec *pCodec = avcodec_find_decoder(pStream->codecpar->codec_id);
    if (pCodec == NULL) {
        LOGE("无法获取解码器");
        return;
    }
    AVCodecContext *pCodecCtx = avcodec_alloc_context3(pCodec);
    if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
        LOGE("无法打开解码器");
        return;
    }

    //分配内存
    AVPacket *pPacket = static_cast<AVPacket *>(av_malloc(sizeof(AVPacket)));
    AVFrame *pFrame = av_frame_alloc();
    SwrContext *pSwrCtx = swr_alloc();
    //采样格式
    enum AVSampleFormat inSampleFmt = pCodecCtx->sample_fmt;
    enum AVSampleFormat outSampleFmt = AV_SAMPLE_FMT_S16;
    //采样率
    int inSampleRate = pCodecCtx->sample_rate;
    int outSampleRate = 44100;
    //声道类别
    uint64_t inSampleChannel = pCodecCtx->channel_layout;
    uint64_t outSampleChannel = AV_CH_LAYOUT_STEREO;
    //添加配置
    swr_alloc_set_opts(pSwrCtx,
                       outSampleChannel,
                       outSampleFmt,
                       outSampleRate,
                       inSampleChannel,
                       inSampleFmt,
                       inSampleRate,
                       0,
                       NULL);
    swr_init(pSwrCtx);
    int outChannelNum = av_get_channel_layout_nb_channels(outSampleChannel);
    int ret = -1;
    int dataSize;
    FILE *outputFile = fopen(outputPath, "wb+");
    AVFrame *pEncodeFrame;
    pEncodeFrame = av_frame_alloc();
    uint8_t *outBuffer = static_cast<uint8_t *>(av_malloc(MAX_AUDIO_FRAME_SIZE));
    while (av_read_frame(pFormatCtx, pPacket) >= 0) {
        if (pPacket->stream_index == audio_stream_idx) {
            ret = avcodec_send_packet(pCodecCtx, pPacket);
            while (ret >= 0) {
                ret = avcodec_receive_frame(pCodecCtx, pEncodeFrame);
                LOGE("%d", ret);
                if (ret == AVERROR(EAGAIN)) {
                    LOGE("%s", "读取解码数据失败");
                    break;
                } else if (ret == AVERROR_EOF) {
                    LOGE("%s", "解码完成");
                    fclose(outputFile);
                    break;
                } else if (ret < 0) {
                    LOGE("%s", "解码出错");
                    break;
                }
//                swr_convert(pSwrCtx,
//                            &outBuffer,
//                            MAX_AUDIO_FRAME_SIZE,
//                           (pEncodeFrame->data),
//                            pEncodeFrame->nb_samples);
//                int outBufferSize = av_samples_get_buffer_size(NULL, outChannelNum,
//                                                               pEncodeFrame->nb_samples,
//                                                               outSampleFmt, 1);
//                fwrite(outBuffer, 1, outBufferSize, outputFile);
                dataSize = av_get_bytes_per_sample(pCodecCtx->sample_fmt);
                if (dataSize < 0) {
                    LOGE("获取数据大小失败");
                    break;
                }
                for (int i = 0; i < pEncodeFrame->nb_samples; ++i) {
                    for (int ch = 0; ch < pCodecCtx->channels; ++ch) {
                        fwrite(pEncodeFrame->data[ch] + dataSize * i, 1, dataSize, outputFile);
                    }
                }
            }
        }

    }
    //释放资源
    av_packet_unref(pPacket);
    fclose(outputFile);

    av_frame_free(&pFrame);
    av_frame_free(&pEncodeFrame);
    swr_free(&pSwrCtx);
    avcodec_free_context(&pCodecCtx);
    avformat_free_context(pFormatCtx);
    env->ReleaseStringUTFChars(inputPath_, inputPath);
    env->ReleaseStringUTFChars(outputPath_, outputPath);
}