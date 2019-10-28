### 入口方法
ijkplayer的prepare共暴露出来两种方法，分别是prepareAsync和_prepareAsync。由于prepareAsync最终也是调用_prepareAsync，所以我们只看_prepareAsync就可以了。
```
ijkMediaPlayer._prepareAsync();
```
### 分析
该方法直接调用了native方法，故没有java层代码分析
#### Native
查询jni映射表，我们知道对应的native方法为IjkMediaPlayer_prepareAsync
- IjkMediaPlayer_prepareAsync
该方法在ijkplayer_jni.c中
```
static void IjkMediaPlayer_prepareAsync(JNIEnv *env, jobject thiz) {
    int retval = 0;
    IjkMediaPlayer *mp = jni_get_media_player(env, thiz);
    retval = ijkmp_prepare_async(mp);
}
```
jni_get_media_player我们之前分析过，所以这次就跳过该方法。
- ijkmp_prepare_async  
该方法定义在ijkplayer.c中
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
调用ijkmp_change_state_l修改mp实例中的state状态为MP_STATE_ASYNC_PREPARING。至此，ijkplayer进入到preparing状态，引用计数+1。msg_queue_start也很简单，就是将FFP_MSG_FLUSH放入队列中。之后通过sdl的SDL_CreateThreadEx为mp中的msg_thread赋值。这里我们看一下ijkmp_msg_loop这个方法  
- ijkmp_msg_loop  
    该方法位于ijkplayer.c中
```
static int ijkmp_msg_loop(void *arg) {
    IjkMediaPlayer *mp = arg;
    //执行ijkplayer_jni.c/msg_loop方法
    int ret = mp->msg_loop(arg);
    return ret;
}
```
该方法会调用msg_loop方法，这个方法我们在之前的[消息队列机制](https://juejin.im/post/5db678caf265da4d2d1f5c8d)中阅读了，这里我们就省略掉。
我们回到ijkmp_prepare_async_l方法中，最后调用ffp_prepare_async_方法
- ffp_prepare_async_l
该方法位于ijkmedia/ijkplayer/ff_ffplay.c中，该方法很长
```
int ffp_prepare_async_l(FFPlayer *ffp, const char *file_name) {
    //这个和下一个if分别判断是否为直播流和路径长度，然后将对应的key-value分别放入到AVDictionary
    if (av_stristart(file_name, "rtmp", NULL) ||
        av_stristart(file_name, "rtsp", NULL)) {
        av_dict_set(&ffp->format_opts, "timeout", NULL, 0);
    }

    if (strlen(file_name) + 1 > 1024) {
        if (avio_find_protocol_name("ijklongurl:")) {
            av_dict_set(&ffp->format_opts, "ijklongurl-url", file_name, 0);
            file_name = "ijklongurl:";
        }
    }

    av_opt_set_dict(ffp, &ffp->player_opts);
    if (!ffp->aout) {
        ffp->aout = ffpipeline_open_audio_output(ffp->pipeline, ffp);
        if (!ffp->aout)
            return -1;
    }

#if CONFIG_AVFILTER
    if (ffp->vfilter0) {
        GROW_ARRAY(ffp->vfilters_list, ffp->nb_vfilters);
        ffp->vfilters_list[ffp->nb_vfilters - 1] = ffp->vfilter0;
    }
#endif
    VideoState *is = stream_open(ffp, file_name, NULL);
    if (!is) {
        av_log(NULL, AV_LOG_WARNING, "ffp_prepare_async_l: stream_open failed OOM");
        return EIJK_OUT_OF_MEMORY;
    }
    ffp->is = is;
    ffp->input_filename = av_strdup(file_name);
    return 0;
}
```
删除掉一些log的代码，还剩这么多。最开始的两个if简单明了，就是判断协议类型。之后通过ffmpeg的av_opt_set_dictg给ffp设置option。之后判断ffp->aout音频输出是否存在，不存在的话就调用ffpipeline_open_audio_output打开音频。
- ffpipeline_open_audio_output
该方法在ff_ffpipeline.c中
```
SDL_Aout *ffpipeline_open_audio_output(IJKFF_Pipeline *pipeline, FFPlayer *ffp)
{
    //打开音频输出，该方法定义在ijkmedia/ijkplayer/android/pipeline_android.c中
    return pipeline->func_open_audio_output(pipeline, ffp);
}
```
通过IJKFF_Pipeline的func_open_audio_output函数指针去打开音频输出。看下func_open_audio_output这个函数指针

- func_open_audio_output  
这个方法定义在ijkmedia/ijkplayer/android/pipeline_android.c中
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
就是通过sdl内置方法去打开opensles或者AudioTrack，然后设置下声道。我们回到ffp_prepare_async_l中，继续阅读CONFIG_AVFILTER是滤镜选项，查看的时候发现该条件始终为假，所以我们忽略。回到ffp_prepare_async_l方法，接下来会调用stream_open函数
- stream_open
```
static VideoState *stream_open(FFPlayer *ffp, const char *filename, AVInputFormat *iformat) {
    VideoState *is;

    is = av_mallocz(sizeof(VideoState));
    is->filename = av_strdup(filename);
    is->iformat = iformat;
    is->ytop = 0;
    is->xleft = 0;
#if defined(__ANDROID__)
    if (ffp->soundtouch_enable) {
        is->handle = ijk_soundtouch_create();
    }
#endif

    /* start video display */
    if (frame_queue_init(&is->pictq, &is->videoq, ffp->pictq_size, 1) < 0)
        goto fail;
    if (frame_queue_init(&is->subpq, &is->subtitleq, SUBPICTURE_QUEUE_SIZE, 0) < 0)
        goto fail;
    if (frame_queue_init(&is->sampq, &is->audioq, SAMPLE_QUEUE_SIZE, 1) < 0)
        goto fail;

    if (packet_queue_init(&is->videoq) < 0 ||
        packet_queue_init(&is->audioq) < 0 ||
        packet_queue_init(&is->subtitleq) < 0)
        goto fail;

    init_clock(&is->vidclk, &is->videoq.serial);
    init_clock(&is->audclk, &is->audioq.serial);
    init_clock(&is->extclk, &is->extclk.serial);
    is->audio_clock_serial = -1;
    ffp->startup_volume = av_clip(ffp->startup_volume, 0, 100);
    ffp->startup_volume = av_clip(SDL_MIX_MAXVOLUME * ffp->startup_volume / 100, 0,
                                  SDL_MIX_MAXVOLUME);
    is->audio_volume = ffp->startup_volume;
    is->muted = 0;
    is->av_sync_type = ffp->av_sync_type;

    is->play_mutex = SDL_CreateMutex();
    is->accurate_seek_mutex = SDL_CreateMutex();
    ffp->is = is;
    is->pause_req = !ffp->start_on_prepared;

    is->video_refresh_tid = SDL_CreateThreadEx(&is->_video_refresh_tid, video_refresh_thread, ffp,
                                               "ff_vout");

    is->initialized_decoder = 0;
    is->read_tid = SDL_CreateThreadEx(&is->_read_tid, read_thread, ffp, "ff_read");
    if (ffp->async_init_decoder && !ffp->video_disable && ffp->video_mime_type &&
        strlen(ffp->video_mime_type) > 0
        && ffp->mediacodec_default_name && strlen(ffp->mediacodec_default_name) > 0) {
        if (ffp->mediacodec_all_videos || ffp->mediacodec_avc || ffp->mediacodec_hevc ||
            ffp->mediacodec_mpeg2) {
            decoder_init(&is->viddec, NULL, &is->videoq, is->continue_read_thread);
            ffp->node_vdec = ffpipeline_init_video_decoder(ffp->pipeline, ffp);
        }
    }
    is->initialized_decoder = 1;

    return is;
}

```
最主要的就是这里通过frame_queue_init初始化了三个队列，视频、字幕、音频
- frame_queue_init
该方法定义在ff_ffplayer.c中
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
我们回到stream_open中，接下来又是熟悉的代码
- packet_queue_init
该方法定义在ff_ffplay.c中
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
这个方法有一个疑问，那就是PacketQueue中的abort_request会不会对MessageQueue的abort_request影响？[1]  

回到open_stream方法，接下来通过init_clock设置时钟，然后设置音量，这部分代码挺简单的，就直接忽略了。然后是通过SDL_CreateThreadEx(&is->_video_refresh_tid, video_refresh_thread, ffp,"ff_vout")去执行video_refresh_thread方法
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
既然stream_open方法不会执行video_refresh，所以我们暂时忽略掉该方法。接下来我们回到stream_open函数
### 疑问
- PacketQueue中的abort_request会不会对MessageQueue的abort_request影响？