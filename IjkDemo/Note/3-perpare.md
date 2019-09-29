### 入口方法
ijkplayer的prepare共暴露出来两种方法，分别是prepareAsync和_prepareAsync。由于prepareAsync最终也是调用_prepareAsync，所以我们只看_prepareAsync就可以了。
ijkMediaPlayer._prepareAsync();
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
仍然是一堆宏定义，这些宏定义貌似是用来判断状态的，接下来调用ijkmp_change_state_l修改状态为MP_STATE_ASYNC_PREPARING进preparing状态，引用计数+1。msg_queue_start也很简单，就是将FFP_MSG_FLUSH放入队列中。之后通过sdl的SDL_CreateThreadEx为mp中的msg_thread赋值，最后调用ffp_prepare_async_方法
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
删除掉一些log的代码，还剩这么多。最开始的两个if简单明了，就是判断协议类型。