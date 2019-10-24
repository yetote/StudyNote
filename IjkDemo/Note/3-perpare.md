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
    MPST_RET_IF_EQ(mp->mp_state, MP_STATE_IDLE);
    // MPST_RET_IF_EQ(mp->mp_state, MP_STATE_INITIALIZED);
    MPST_RET_IF_EQ(mp->mp_state, MP_STATE_ASYNC_PREPARING);
    MPST_RET_IF_EQ(mp->mp_state, MP_STATE_PREPARED);
    MPST_RET_IF_EQ(mp->mp_state, MP_STATE_STARTED);
    MPST_RET_IF_EQ(mp->mp_state, MP_STATE_PAUSED);
    MPST_RET_IF_EQ(mp->mp_state, MP_STATE_COMPLETED);
    // MPST_RET_IF_EQ(mp->mp_state, MP_STATE_STOPPED);
    MPST_RET_IF_EQ(mp->mp_state, MP_STATE_ERROR);
    MPST_RET_IF_EQ(mp->mp_state, MP_STATE_END);
    ijkmp_change_state_l(mp, MP_STATE_ASYNC_PREPARING);
    msg_queue_start(&mp->ffplayer->msg_queue);
    ijkmp_inc_ref(mp);
    mp->msg_thread = SDL_CreateThreadEx(&mp->_msg_thread, ijkmp_msg_loop, mp, "ff_msg_loop");
    int retval = ffp_prepare_async_l(mp->ffplayer, mp->data_source);
    if (retval < 0) {
        ijkmp_change_state_l(mp, MP_STATE_ERROR);
        return retval;
    }
    return 0;
}
```
仍然是一堆宏定义，这些宏定义貌似是用来判断状态的，接下来调用ijkmp_change_state_l修改状态为MP_STATE_ASYNC_PREPARING进preparing状态，引用计数+1。msg_queue_start也很简单，就是将FFP_MSG_FLUSH放入队列中。之后通过sdl的SDL_CreateThreadEx为mp中的msg_thread赋值，这里我们看一下ijkmp_msg_loop这个方法  
- ijkmp_msg_loop  
    该方法位于ijkplayer.c中
```
static int ijkmp_msg_loop(void *arg) {
    IjkMediaPlayer *mp = arg;
    int ret = mp->msg_loop(arg);
    return ret;
}
```
该方法会调用msg_loop方法
- msg_loop
    这是个函数指针，让我们跟踪一下发现他是在IjkMediaPlayer_native_setup函数中传递过去的，而这个函数的原型位于ijkplayer_jni.c中
    ```
    static int message_loop(void *arg) {
        IjkMediaPlayer *mp = (IjkMediaPlayer *) arg;
        message_loop_n(env, mp);
        return 0;
    }
    ```  
    去掉无用的代码后  我们发现他调用了message_loop_n方法
- message_loop_n
    该方法位于ijkplayer.c中
    ```
    static void message_loop_n(JNIEnv *env,IjkMediaPlayer *mp) {
        jobject weak_thiz = (jobject) ijkmp_get_weak_thiz(mp);
        while (1) {
            AVMessage msg;
            int retval = ijkmp_get_msg(mp, &msg, 1);   
            switch (msg.what) {
                case FFP_MSG_FLUSH:
                    post_event(env, weak_thiz, MEDIA_NOP,0, 0);
                    break;
                case FFP_MSG_ERROR:
                    post_event(env, weak_thiz, MEDIA_ERROR,MEDIA_ERROR_IJK_PLAYER, msg.arg1);
                    break;
                case FFP_MSG_PREPARED:
                    post_event(env, weak_thiz,MEDIA_PREPARED, 0, 0);
                    break;
                case FFP_MSG_COMPLETED:
                    post_event(env, weak_thiz,MEDIA_PLAYBACK_COMPLETE, 0, 0);
                    break;
                case FFP_MSG_VIDEO_SIZE_CHANGED:
                    post_event(env, weak_thiz,MEDIA_SET_VIDEO_SIZE, msg.arg1,msg.arg2);
                    break;
                case FFP_MSG_SAR_CHANGED:
                    post_event(env, weak_thiz,MEDIA_SET_VIDEO_SAR, msg.arg1,msg.arg2);
                    break;
                case FFP_MSG_VIDEO_RENDERING_START:
                    post_event(env, weak_thiz, MEDIA_INFO,MEDIA_INFO_VIDEO_RENDERING_START, 0);
                    break;
                case FFP_MSG_AUDIO_RENDERING_START:
                    post_event(env, weak_thiz, MEDIA_INFO,MEDIA_INFO_AUDIO_RENDERING_START, 0);
                    break;
                case FFP_MSG_VIDEO_ROTATION_CHANGED:
                    post_event(env, weak_thiz, MEDIA_INFO,MEDIA_INFO_VIDEO_ROTATION_CHANGED,msg.arg1);
                    break;
                case FFP_MSG_AUDIO_DECODED_START:
                    post_event(env, weak_thiz, MEDIA_INFO,MEDIA_INFO_AUDIO_DECODED_START, 0);
                    break;
                case FFP_MSG_VIDEO_DECODED_START:
                    post_event(env, weak_thiz, MEDIA_INFO,MEDIA_INFO_VIDEO_DECODED_START, 0);
                    break;
                case FFP_MSG_OPEN_INPUT:
                    post_event(env, weak_thiz, MEDIA_INFO,MEDIA_INFO_OPEN_INPUT, 0);
                    break;
                case FFP_MSG_FIND_STREAM_INFO:
                    post_event(env, weak_thiz, MEDIA_INFO,MEDIA_INFO_FIND_STREAM_INFO, 0);
                    break;
                case FFP_MSG_COMPONENT_OPEN:
                    post_event(env, weak_thiz, MEDIA_INFO,MEDIA_INFO_COMPONENT_OPEN, 0);
                    break;
                case FFP_MSG_BUFFERING_START:
                    post_event(env, weak_thiz, MEDIA_INFO,MEDIA_INFO_BUFFERING_START, msg.arg1);
                    break;
                case FFP_MSG_BUFFERING_END:
                    post_event(env, weak_thiz, MEDIA_INFO,MEDIA_INFO_BUFFERING_END, msg.arg1);
                    break;
                case FFP_MSG_BUFFERING_UPDATE:
                    post_event(env, weak_thiz,MEDIA_BUFFERING_UPDATE, msg.arg1,msg.arg2);
                    break;
                case FFP_MSG_BUFFERING_BYTES_UPDATE:
                    break;
                case FFP_MSG_BUFFERING_TIME_UPDATE:
                    break;
                case FFP_MSG_SEEK_COMPLETE:
                    post_event(env, weak_thiz,MEDIA_SEEK_COMPLETE, 0, 0);
                    break;
                case FFP_MSG_ACCURATE_SEEK_COMPLETE:
                    post_event(env, weak_thiz, MEDIA_INFO,MEDIA_INFO_MEDIA_ACCURATE_SEEK_COMPLETE,msg.arg1);
                    break;
                case FFP_MSG_PLAYBACK_STATE_CHANGED:
                    break;
                case FFP_MSG_TIMED_TEXT:
                    if (msg.obj) {
                        jstring text = (*env)->NewStringUTF(env, (char *) msg.obj);
                        post_event2(env, weak_thiz,MEDIA_TIMED_TEXT, 0, 0, text);
                        J4A_DeleteLocalRef__p(env, &text);
                    } else {
                        post_event2(env, weak_thiz,MEDIA_TIMED_TEXT, 0, 0, NULL);
                    }
                    break;
                case FFP_MSG_GET_IMG_STATE:
                    if (msg.obj) {
                        jstring file_name = (*env)->NewStringUTF(env, (char *)msg.obj);
                        post_event2(env, weak_thiz,MEDIA_GET_IMG_STATE, msg.arg1,msg.arg2, file_name);
                        J4A_DeleteLocalRef__p(env, &file_name);
                    } else {
                        post_event2(env, weak_thiz,MEDIA_GET_IMG_STATE, msg.arg1,msg.arg2, NULL);
                    }
                    break;
                case FFP_MSG_VIDEO_SEEK_RENDERING_START:
                    post_event(env, weak_thiz, MEDIA_INFO,MEDIA_INFO_VIDEO_SEEK_RENDERING_START,msg.arg1);
                    break;
                case FFP_MSG_AUDIO_SEEK_RENDERING_START:
                    post_event(env, weak_thiz, MEDIA_INFO,MEDIA_INFO_AUDIO_SEEK_RENDERING_START,msg.arg1);
                    break;
                default:
                    break;
            }
            msg_free_res(&msg);
        }
        LABEL_RETURN:;
    }
    ```
代码很长，但是并不复杂  
最后调用ffp_prepare_async_方法
- ffp_prepare_async_l
```
int ffp_prepare_async_l(FFPlayer *ffp, const char *file_name) {
    if (av_stristart(file_name, "rtmp", NULL) ||
        av_stristart(file_name, "rtsp", NULL)) {
        // There is total different meaning for 'timeout' option in rtmp
        av_log(ffp, AV_LOG_WARNING, "remove 'timeout' option for rtmp.\n");
        av_dict_set(&ffp->format_opts, "timeout", NULL, 0);
    }

    /* there is a length limit in avformat */
    if (strlen(file_name) + 1 > 1024) {
        av_log(ffp, AV_LOG_ERROR, "%s too long url\n", __func__);
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
就是通过sdl内置方法去打开opensles或者AudioTrack，然后设置下声道。我们回到ffp_prepare_async_l中，继续阅读CONFIG_AVFILTER是滤镜选项，查看的时候发现该条件始终为假。接下来会调用stream_open这个方法
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

    if (!(is->continue_read_thread = SDL_CreateCond())) {
        av_log(NULL, AV_LOG_FATAL, "SDL_CreateCond(): %s\n", SDL_GetError());
        goto fail;
    }

    if (!(is->video_accurate_seek_cond = SDL_CreateCond())) {
        av_log(NULL, AV_LOG_FATAL, "SDL_CreateCond(): %s\n", SDL_GetError());
        ffp->enable_accurate_seek = 0;
    }

    if (!(is->audio_accurate_seek_cond = SDL_CreateCond())) {
        av_log(NULL, AV_LOG_FATAL, "SDL_CreateCond(): %s\n", SDL_GetError());
        ffp->enable_accurate_seek = 0;
    }

    init_clock(&is->vidclk, &is->videoq.serial);
    init_clock(&is->audclk, &is->audioq.serial);
    init_clock(&is->extclk, &is->extclk.serial);
    is->audio_clock_serial = -1;
    if (ffp->startup_volume < 0)
        av_log(NULL, AV_LOG_WARNING, "-volume=%d < 0, setting to 0\n", ffp->startup_volume);
    if (ffp->startup_volume > 100)
        av_log(NULL, AV_LOG_WARNING, "-volume=%d > 100, setting to 100\n", ffp->startup_volume);
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
    f->max_size = FFMIN(max_size, FRAME_QUEUE_SIZE);
    f->keep_last = !!keep_last;
    for (i = 0; i < f->max_size; i++)
        if (!(f->queue[i].frame = av_frame_alloc()))
            return AVERROR(ENOMEM);
    return 0;
}
```
该方法中主要对队列进行了初始化。我们回到stream_open中，通过init_clock设置时钟。然后设置音量，接着是通过SDL_CreateThreadEx(&is->_video_refresh_tid, video_refresh_thread, ffp,"ff_vout");去执行video_refresh_thread方法
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
        if (is->show_mode != SHOW_MODE_NONE && (!is->paused || is->force_refresh))
            video_refresh(ffp, &remaining_time);
    }

    return 0;
}
```
