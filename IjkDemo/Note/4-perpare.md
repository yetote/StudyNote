### 入口方法
**ijkplayer**的**prepare**共暴露出来两种方法，分别是```prepareAsync```和```_prepareAsync```。由于```prepareAsync```最终也是调用```_prepareAsync```，所以我们只看```_prepareAsync```就可以了。
```
ijkMediaPlayer._prepareAsync();
```
### 分析
该方法直接调用了**native**方法，故没有**java**层代码分析
#### Native
查询**jni映射表**，我们知道对应的**native**方法为```IjkMediaPlayer_prepareAsync```
- IjkMediaPlayer_prepareAsync
该方法在**ijkplayer_jni.c**中
```
static void IjkMediaPlayer_prepareAsync(JNIEnv *env, jobject thiz) {
    int retval = 0;
    IjkMediaPlayer *mp = jni_get_media_player(env, thiz);
    retval = ijkmp_prepare_async(mp);
}
```
```jni_get_media_player```我们之前分析过，所以这次就跳过该方法。
- ijkmp_prepare_async  
该方法定义在**ijkplayer.c**中
```
int ijkmp_prepare_async(IjkMediaPlayer *mp) {
    int retval = ijkmp_prepare_async_l(mp);
    return retval;
}
```
该方法调用了同一文件中的ijkmp_prepare_async_l方法
- ijkmp_prepare_async_l
```
static int ijkmp_prepare_async_l(IjkMediaPlayer *mp) {
     /*
     * 修改mp.state为PREPARING
     * 并将FFP_MSG_PLAYBACK_STATE_CHANGED入队
     * 这个状态对应的处理方法为空，也就表示在目前版本，这是个无效状态
     * */
    ijkmp_change_state_l(mp, MP_STATE_ASYNC_PREPARING);
    /*
     *该方法向queue中添加了一条消息
     * 该状态表示用于测试native和java层的关联
     * */
    msg_queue_start(&mp->ffplayer->msg_queue);

    // 引用计数+1
    ijkmp_inc_ref(mp);
    /*
     * 这里创建了一个新的线程
     * 该线程用于执行ijkmp_msg_loop方法
     * */
    mp->msg_thread = SDL_CreateThreadEx(&mp->_msg_thread, ijkmp_msg_loop, mp, "ff_msg_loop");
    
    int retval = ffp_prepare_async_l(mp->ffplayer, mp->data_source);
    if (retval < 0) {
        ijkmp_change_state_l(mp, MP_STATE_ERROR);
        return retval;
    }
    return 0;
}
```
调用```ijkmp_change_state_l```修改**mp**实例中的**state**状态为**MP_STATE_ASYNC_PREPARING**。至此，**ijkplayer**进入到**preparing**状态，引用计数+1。```msg_queue_start```也很简单，就是将**FFP_MSG_FLUSH**放入队列中。之后通过**sdl**的SDL_CreateThreadEx为**mp**中的**msg_thread**赋值。这里我们看一下```ijkmp_msg_loop```这个方法  
- ijkmp_msg_loop  
    该方法位于**ijkplayer.c**中
```
static int ijkmp_msg_loop(void *arg) {
    IjkMediaPlayer *mp = arg;
    //执行ijkplayer_jni.c/msg_loop方法
    int ret = mp->msg_loop(arg);
    return ret;
}
```
该方法会调用```msg_loop```方法，这个方法我们在之前的[消息队列机制](https://juejin.im/post/5db678caf265da4d2d1f5c8d)中阅读了，这里我们就省略掉。
我们回到```ijkmp_prepare_async_l```方法中，最后调用```ffp_prepare_async_l```方法
- ffp_prepare_async_l
该方法位于**ijkmedia/ijkplayer/ff_ffplay.c**中，该方法很长
```
int ffp_prepare_async_l(FFPlayer *ffp, const char *file_name) {
    //判断是否为rtmp或者rtsp直播流
    if (av_stristart(file_name, "rtmp", NULL) ||
        av_stristart(file_name, "rtsp", NULL)) {
        // There is total different meaning for 'timeout' option in rtmp
        av_dict_set(&ffp->format_opts, "timeout", NULL, 0);
    }

    /* there is a length limit in avformat */
    //判断url的长度
    if (strlen(file_name) + 1 > 1024) {
        if (avio_find_protocol_name("ijklongurl:")) {
            av_dict_set(&ffp->format_opts, "ijklongurl-url", file_name, 0);
            file_name = "ijklongurl:";
        }
    }
    //设置ffplayer的option
    av_opt_set_dict(ffp, &ffp->player_opts);
    if (!ffp->aout) {
        //打开音频输出
        ffp->aout = ffpipeline_open_audio_output(ffp->pipeline, ffp);
        if (!ffp->aout)
            return -1;
    }
    //滤镜部分忽略
    /*
     * 执行open_stream方法
     * 初始化了FrameQueue、PacketQueue
     * 执行了read_thread方法
     * 初始化了软硬解码器
     * */
    VideoState *is = stream_open(ffp, file_name, NULL);
    ffp->is = is;
    ffp->input_filename = av_strdup(file_name);
    return 0;
}

```
删除掉一些**log**的代码，还剩这么多。最开始的两个**if**简单明了，就是判断协议类型。之后通过**ffmpeg**的```av_opt_set_dictg```给**ffp**设置**option**。之后判断**ffp->aout**音频输出是否存在，不存在的话就调用```ffpipeline_open_audio_output```打开音频。
- ffpipeline_open_audio_output
该方法在**ff_ffpipeline.c**中
```
SDL_Aout *ffpipeline_open_audio_output(IJKFF_Pipeline *pipeline, FFPlayer *ffp)
{
    //打开音频输出，该方法定义在ijkmedia/ijkplayer/android/pipeline_android.c中
    return pipeline->func_open_audio_output(pipeline, ffp);
}
```
通过**IJKFF_Pipeline**的```func_open_audio_output```函数指针去打开音频输出。看下```func_open_audio_output```这个函数指针

- func_open_audio_output  
这个方法定义在**ijkmedia/ijkplayer/android/pipeline_android.c**中
```
static SDL_Aout *func_open_audio_output(IJKFF_Pipeline *pipeline, FFPlayer *ffp)
{
    SDL_Aout *aout = NULL;
    if (ffp->opensles) {
        aout = SDL_AoutAndroid_CreateForOpenSLES();
    } else {
        aout = SDL_AoutAndroid_CreateForAudioTrack();
    }
    if (aout)
        SDL_AoutSetStereoVolume(aout, pipeline->opaque->left_volume, pipeline->opaque->right_volume);
    return aout;
}
```
就是通过**sdl**内置方法去打开**opensles**或者**AudioTrack**，然后设置下声道。我们回到```ffp_prepare_async_l```中，继续阅读**CONFIG_AVFILTER**是滤镜选项，查看的时候发现该条件始终为假，所以我们忽略。回到```ffp_prepare_async_l```方法，接下来会调用stream_open函数
- stream_open
```
static VideoState *stream_open(FFPlayer *ffp, const char *filename, AVInputFormat *iformat) {
    VideoState *is;

    is = av_mallocz(sizeof(VideoState));
    //复制uri
    is->filename = av_strdup(filename);
    //这个参数为null
    is->iformat = iformat;
    is->ytop = 0;
    is->xleft = 0;
#if defined(__ANDROID__)
    if (ffp->soundtouch_enable) {
        //这个方法没找到定义
        is->handle = ijk_soundtouch_create();
    }
#endif
    /* start video display */
    //这里初始化了三个队列，跟别为视频、字幕、音频
    if (frame_queue_init(&is->pictq, &is->videoq, ffp->pictq_size, 1) < 0)
        goto fail;
    if (frame_queue_init(&is->subpq, &is->subtitleq, SUBPICTURE_QUEUE_SIZE, 0) < 0)
        goto fail;
    if (frame_queue_init(&is->sampq, &is->audioq, SAMPLE_QUEUE_SIZE, 1) < 0)
        goto fail;
    //初始化 Packet
    if (packet_queue_init(&is->videoq) < 0 ||
        packet_queue_init(&is->audioq) < 0 ||
        packet_queue_init(&is->subtitleq) < 0)
        goto fail;

    //设置内部时钟
    init_clock(&is->vidclk, &is->videoq.serial);
    init_clock(&is->audclk, &is->audioq.serial);
    init_clock(&is->extclk, &is->extclk.serial);

    is->audio_clock_serial = -1;
    //设置声音
    ffp->startup_volume = av_clip(ffp->startup_volume, 0, 100);
    ffp->startup_volume = av_clip(SDL_MIX_MAXVOLUME * ffp->startup_volume / 100, 0,
                                  SDL_MIX_MAXVOLUME);
    is->audio_volume = ffp->startup_volume;
    is->muted = 0;
    is->av_sync_type = ffp->av_sync_type;

    ffp->is = is;
    is->pause_req = !ffp->start_on_prepared;
    //这里调用了video_refresh_thread，这里因为show_mode的缘故，video_refresh方法不会执行
    is->video_refresh_tid = SDL_CreateThreadEx(&is->_video_refresh_tid, video_refresh_thread, ffp,
                                               "ff_vout");

    is->initialized_decoder = 0;
    //执行read_thread方法
    is->read_tid = SDL_CreateThreadEx(&is->_read_tid, read_thread, ffp, "ff_read");
    /*
     * async_init_decoder|是否异步初始化解码器|bool|
     * video_disable|是否禁用视频|bool|
     * video_mime_type|视频MIME_TYPE|string|
     * mediacodec_default_name|默认的硬解码器名|string|
     * mediacodec_all_videos|启用所有的视频(硬解)|bool|
     * mediacodec_avc|是否支持h264|bool|
     * mediacodec_hevc|是否支持h265|bool|
     * mediacodec_mpeg2|是否支持mp2|bool|
     * */
    if (ffp->async_init_decoder && !ffp->video_disable && ffp->video_mime_type &&
        strlen(ffp->video_mime_type) > 0
        && ffp->mediacodec_default_name && strlen(ffp->mediacodec_default_name) > 0) {
        if (ffp->mediacodec_all_videos || ffp->mediacodec_avc || ffp->mediacodec_hevc ||
            ffp->mediacodec_mpeg2) {
            //初始化解码器
            decoder_init(&is->viddec, NULL, &is->videoq, is->continue_read_thread);
            ffp->node_vdec = ffpipeline_init_video_decoder(ffp->pipeline, ffp);
        }
    }
    is->initialized_decoder = 1;

    return is;
}

```
最主要的就是这里通过```frame_queue_init```初始化了三个队列，视频、字幕、音频
- frame_queue_init
该方法定义在**ff_ffplayer.c**中
```
static int frame_queue_init(FrameQueue *f, PacketQueue *pktq, int max_size, int keep_last) {
    int i;
    memset(f, 0, sizeof(FrameQueue));
    f->pktq = pktq;
    //确定frameQueue的大小
    f->max_size = FFMIN(max_size, FRAME_QUEUE_SIZE);
    //？？
    f->keep_last = !!keep_last;
    //初始化队列中的AVFrame
    for (i = 0; i < f->max_size; i++)
        if (!(f->queue[i].frame = av_frame_alloc()))
            return AVERROR(ENOMEM);
    return 0;
}
```
该方法中主要对队列进行了初始化。  
我们回到```stream_open```中，接下来又是熟悉的代码
- packet_queue_init
该方法定义在**ff_ffplay.c**中
```
static int packet_queue_init(PacketQueue *q) {
    //初始化PacketQueue队列
    memset(q, 0, sizeof(PacketQueue));
    /*
     * 这里是一条中断请求
     * 至于是否会向MessageQueue中发送对应的消息，现在不得而知
     * */
    q->abort_request = 1;
    return 0;
}
```
这个方法有一个疑问，那就是**PacketQueue**中的**abort_request**会不会对**MessageQueue**的**abort_request影响？**[1]  

回到```open_stream```方法，接下来通过```init_clock```设置时钟，然后设置音量，这部分代码挺简单的，就直接忽略了。然后是通过```SDL_CreateThreadEx(&is->_video_refresh_tid, video_refresh_thread, ffp,"ff_vout")```去执行```video_refresh_thread```方法
- video_refresh_thread
```
static int video_refresh_thread(void *arg) {
    FFPlayer *ffp = arg;
    VideoState *is = ffp->is;
    double remaining_time = 0.0;
    while (!is->abort_request) {
        if (remaining_time > 0.0)
            av_usleep((int) (int64_t)(remaining_time * 1000000.0));
        remaining_time = REFRESH_RATE;
        /*
         * 在stream_open方法中show_mode为SHOW_MODE_NONE，所以在stream_open时，video_refresh不会执行
         * */
        if (is->show_mode != SHOW_MODE_NONE && (!is->paused || is->force_refresh))
            video_refresh(ffp, &remaining_time);
    }

    return 0;
}
```
既然```stream_open```方法不会执行```video_refresh```，所以我们暂时忽略掉该方法。接下来我们回到```stream_open```函数，下一行代码又创建了一个```read_thread```的线程，进入。进入个锤子，代码太复杂了，本篇先忽略。
```stream_open```下一环节的**if**条件很长，代码贴上来分析一下
```
if (ffp->async_init_decoder && !ffp->video_disable && ffp->video_mime_type &&
    strlen(ffp->video_mime_type) > 0
    && ffp->mediacodec_default_name && strlen(ffp->mediacodec_default_name) > 0) {
    if (ffp->mediacodec_all_videos || ffp->mediacodec_avc || ffp->mediacodec_hevc ||
        ffp->mediacodec_mpeg2) {
        decoder_init(&is->viddec, NULL, &is->videoq, is->continue_read_thread);
        ffp->node_vdec = ffpipeline_init_video_decoder(ffp->pipeline, ffp);
    }
}
```
先来确定下各个属性的含义
|属性名|含义|值类型
|----|----|----|
|async_init_decoder|是否异步初始化解码器|bool|
|video_disable|是否禁用视频|bool|
|video_mime_type|视频MIME_TYPE|string|
|mediacodec_default_name|默认的硬解码器名|string|
|mediacodec_all_videos|启用所有的视频(硬解)|bool|
|mediacodec_avc|是否支持h264|bool|
|mediacodec_hevc|是否支持h265|bool|
|mediacodec_mpeg2|是否支持mp2|bool|  
确定了属性的含义之后这个条件就很简单了。首先，先判断是否异步注册解码器、是否禁用视频、视频**MIME**是否存在、默认解码器是否存在；然后进行解码器支持的类型。如果这些都没问题的话，开始执行```decoder_init```方法注册解码器
- decoder_init
```
static void
decoder_init(Decoder *d, AVCodecContext *avctx, PacketQueue *queue, SDL_cond *empty_queue_cond) {
    memset(d, 0, sizeof(Decoder));
    d->avctx = avctx;
    d->queue = queue;
    d->empty_queue_cond = empty_queue_cond;
    d->start_pts = AV_NOPTS_VALUE;

    d->first_frame_decoded_time = SDL_GetTickHR();
    d->first_frame_decoded = 0;
    //不清楚具体是做什么的，看起来像是重置(简介？？？)
    SDL_ProfilerReset(&d->decode_profiler, -1);
}
```
大部分就是赋值运算，第一行初始化了传递进来的**Decoder**。该方法执行完之后会调用```ffpipeline_init_video_decoder```，这个方法用于注册解码器(硬解)
- ffpipeline_init_video_decoder
该方法定义在**ijkmedia/ijkplayer/android/pipeline/ffpipeline_android.c**中
```
IJKFF_Pipenode *ffpipeline_init_video_decoder(IJKFF_Pipeline *pipeline, FFPlayer *ffp) {
    //注册视频解码器(硬解)，该方法定义在ijkmedia/ijkplayer/android/pipeline_android.c中
    return pipeline->func_init_video_decoder(pipeline, ffp);
}
```
- func_init_video_decoder
该方法定义在**ijkmedia/ijkplayer/android/pipeline_android.c**中
```
static IJKFF_Pipenode *func_init_video_decoder(IJKFF_Pipeline *pipeline, FFPlayer *ffp)
{
    IJKFF_Pipeline_Opaque *opaque = pipeline->opaque;
    IJKFF_Pipenode        *node = NULL;

    if (ffp->mediacodec_all_videos || ffp->mediacodec_avc || ffp->mediacodec_hevc || ffp->mediacodec_mpeg2)
        node = ffpipenode_init_decoder_from_android_mediacodec(ffp, pipeline, opaque->weak_vout);

    return node;
}
```
- ffpipenode_init_decoder_from_android_mediacodec
该方法定义在**ijkmedia/ijkplayer/android/ffpipenode_android_mediacodec_vdec.c**中
```
IJKFF_Pipenode *ffpipenode_init_decoder_from_android_mediacodec(FFPlayer *ffp, IJKFF_Pipeline *pipeline, SDL_Vout *vout)
{
    if (SDL_Android_GetApiLevel() < IJK_API_16_JELLY_BEAN)
        return NULL;
    //分配内存
    IJKFF_Pipenode *node = ffpipenode_alloc(sizeof(IJKFF_Pipenode_Opaque));

    //传递函数指针
    node->func_destroy  = func_destroy;
    if (ffp->mediacodec_sync) {
        node->func_run_sync = func_run_sync_loop;
    } else {
        node->func_run_sync = func_run_sync;
    }
    node->func_flush    = func_flush;

    strcpy(opaque->mcc.codec_name, ffp->mediacodec_default_name);
    //没找到这个方法，看起来是通过解码器名去创建MediaCodec
    opaque->acodec = SDL_AMediaCodecJava_createByCodecName(env, ffp->mediacodec_default_name);

    return node;
}
```
这个方法最重要（大概）的是创建了**MediaCodec**了。至此，```stream_open```执行完毕，这个方法中初始化了**FrameQueue**、**PacketQueue**，执行了```read_thread```方法，最后初始化了软硬解码器
回到```ffp_prepare_async_l```方法，发现该方法也执行完毕了。返回上一级```ijkmp_prepare_async_l```中，该方法也执行完毕。至此，**prepare**流程
结束。
### 总结
在**prepare**流程中，启动了msg_loop方法；打开了音频输出；初始化了**FrameQueue**、**PacketQueue**；执行了read_thread方法；初始化了软硬解码器
### 疑问
- **PacketQueue**中的**abort_request**会不会对**MessageQueue**的**abort_request**影响？
- 在```video_refresh_thread```中，我们根据**VideoState**的**show_mode**判断不会去执行```video_refresh```方法，也就是不会去显示帧。那么在```read_thread```方法中，**show_mode**会不会更改？```video_refresh```是否会被执行