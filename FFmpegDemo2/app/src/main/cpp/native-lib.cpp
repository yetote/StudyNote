#include <jni.h>
#include <string>
#include <android/log.h>

extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavutil/imgutils.h"
#include "libswscale/swscale.h"
}
#define LOG_TAG "VideoUtils"
#define LOGI(FORMAT, ...) __android_log_print(ANDROID_LOG_INFO,"wuhuannan",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"wuhuannan",FORMAT,##__VA_ARGS__);


extern "C" JNIEXPORT jstring JNICALL
Java_com_example_ffmpegdemo_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_ffmpegdemo_VideoUtils_decode(JNIEnv *env, jclass type, jstring inputPath_,
                                              jstring outputPath_) {
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
        } else {
            LOGE("未找到视频流，请查看视频信息");
            return;
        }
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
    LOGI("解码器的名称：%s", pCodec->name);

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
    AVFrame *pEncodeFrame;
    //为解码后的视频帧分配内存
    pEncodeFrame = av_frame_alloc();
    int frame_count = 0;
    //6.一帧一帧的读取压缩数据
    while (av_read_frame(pFormatContext, pPacket) >= 0) {
        //只对视频进行解码（根据流的索引位置判断）
        if (pPacket->stream_index == video_stream_idx) {
            //7.解码一帧视频压缩数据，得到视频像素数据
            ret = avcodec_send_packet(pCodecCtx, pPacket);
            while (ret >= 0) {
                //接受解码后的数据
                ret = avcodec_receive_frame(pCodecCtx, pEncodeFrame);
                if (ret == AVERROR(EAGAIN)) {
                    LOGE("%s", "读取解码数据失败");
                    return;
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
                sws_scale(sws_ctx, pEncodeFrame->data, pEncodeFrame->linesize, 0, pCodecCtx->height,
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

            }
            frame_count++;
            LOGI("解码第%d帧", frame_count);
        }
    }
    //释放资源
    av_packet_unref(pPacket);
    fclose(fp_yuv);

    av_frame_free(&pFrame);
    av_frame_free(&pEncodeFrame);
    avcodec_free_context(&pCodecCtx);
    avformat_free_context(pFormatContext);

    env->ReleaseStringUTFChars(inputPath_, inputPath);
    env->ReleaseStringUTFChars(outputPath_, outputPath);
}