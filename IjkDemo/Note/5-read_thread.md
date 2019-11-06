### 开篇
read_thread方法主要负责数据的读取，解封装之类的i/o操作，由于read_thread方法是对ffplay的同名方法做了修改，所以本篇也会同时阅读ffplay
### 版本
- ijkplayer好久没更新了目前版本为0.8.8
- ffmpeg 4.2.1(更严谨的做法应该是比对ijk采用的ffmpeg版本)
### 入口方法
- ijkplayer
```
static VideoState *stream_open(FFPlayer *ffp, const char *filename, AVInputFormat *iformat) {
    ...
    is->read_tid = SDL_CreateThreadEx(&is->_read_tid, read_thread, ffp, "ff_read");
    ...
}
```
- ffplay
```
static VideoState *stream_open(const char *filename, AVInputFormat *iformat) {
    ...
    is->read_tid = SDL_CreateThread(read_thread, "read_thread", is);
    ...
}
```
在入口方法上，二者并没有体现多大的区别，SDL_CreateThreadEx和SDL_CreateThread有什么区别我也不知道。传递的参数虽然看起来不一样，但是ijkplayer传递ffp中包含了VideoState。
### read_thread
由于read_thread方法比较长，这里我们按功能对其进行拆分，方便阅读
#### 变量
```
    FFPlayer *ffp = arg;
    VideoState *is = ffp->is;
    AVFormatContext *ic = NULL;
    int err, i, ret __unused;
    int st_index[AVMEDIA_TYPE_NB];
    AVPacket pkt1, *pkt = &pkt1;
    int64_t stream_start_time;
    int completed = 0;
    int pkt_in_play_range = 0;
    AVDictionaryEntry *t;
    SDL_mutex *wait_mutex = SDL_CreateMutex();
    int scan_all_pmts_set = 0;
    int64_t pkt_ts;
    int last_error = 0;
    int64_t prev_io_tick_counter = 0;
    int64_t io_tick_counter = 0;
    int init_ijkmeta = 0;
```
变量这一段区别只是在于ijk多增加了一些用于控制流程的变量，这不是关注的重点
#### 打开文件
##### ffplay
```
    //初始化st_index数组
    memset(st_index, -1, sizeof(st_index));
    //分配AVFmtCtx
    ic = avformat_alloc_context();
    /*
     * 好了，interrupt_callback用于执行耗时操作时检查用户是否决定中止，防止响应不及时
     * 回调的方法为decode_interrupt_cb
     * */
    ic->interrupt_callback.callback = decode_interrupt_cb;
    ic->interrupt_callback.opaque = is;
    //这里是过去format_opts字典中的scan_all_pmts，最后一个参数通过查询得知为检索方式
    if (!av_dict_get(format_opts, "scan_all_pmts", NULL, AV_DICT_MATCH_CASE)) {
        //不存在的话重新设置下value，为1
        av_dict_set(&format_opts, "scan_all_pmts", "1", AV_DICT_DONT_OVERWRITE);
        scan_all_pmts_set = 1;
    }
    //这里为打开文件或者网络流
    err = avformat_open_input(&ic, is->filename, is->iformat, &format_opts);
```
执行到了avformat_open_input，文件被打开
- decode_interrupt_cb
```
static int decode_interrupt_cb(void *ctx) {
    VideoState *is = ctx;
    return is->abort_request;
}
```
这个回调方法也很简单，就是通过VideoState的abort_request属性决定是否继续执行
##### ijkplayer
```
        //分配数组
    memset(st_index, -1, sizeof(st_index));
    //分配AVFormatCtx
    ic = avformat_alloc_context();
    //io终断回调
    ic->interrupt_callback.callback = decode_interrupt_cb;
    ic->interrupt_callback.opaque = is;
    //设置一些AVDictionary属性
    if (!av_dict_get(ffp->format_opts, "scan_all_pmts", NULL, AV_DICT_MATCH_CASE)) {
        av_dict_set(&ffp->format_opts, "scan_all_pmts", "1", AV_DICT_DONT_OVERWRITE);
        scan_all_pmts_set = 1;
    }
    if (av_stristart(is->filename, "rtmp", NULL) ||
        av_stristart(is->filename, "rtsp", NULL)) {
        av_dict_set(&ffp->format_opts, "timeout", NULL, 0);
    }

    if (ffp->skip_calc_frame_rate) {
        av_dict_set_int(&ic->metadata, "skip-calc-frame-rate", ffp->skip_calc_frame_rate, 0);
        av_dict_set_int(&ffp->format_opts, "skip-calc-frame-rate", ffp->skip_calc_frame_rate, 0);
    }
    /*
     * 获取封装格式参数及信息
     * 这么做是为了优化avformat_open_input探测解封装格式的时间
     * */
    if (ffp->iformat_name)
        is->iformat = av_find_input_format(ffp->iformat_name);
    //打开文件并获取AVFmtCtx
    err = avformat_open_input(&ic, is->filename, is->iformat, &ffp->format_opts);
```
~~去掉一些相似代码~~因为我们无法准确的得知具体是因为ffmpeg决定删除或增加代码还是ijkplayer对于ffplay的优化，所以还是不去掉了。ijkplayer增加了一步判断是否为直播流的条件，并修改了字典中超时(timeout)的value，av_find_input_format寻找到AVInputFmt，emmm，大概这么做能快一点？
#### 寻找流信息
##### ffplay
```
    //初始化st_index数组
    memset(st_index, -1, sizeof(st_index));
    //初始化VideoState中的流索引为-1
    is->last_video_stream = is->video_stream = -1;
    is->last_audio_stream = is->audio_stream = -1;
    is->last_subtitle_stream = is->subtitle_stream = -1;
    is->eof = 0;

    if (scan_all_pmts_set)
        av_dict_set(&format_opts, "scan_all_pmts", NULL, AV_DICT_MATCH_CASE);

    //这里将VideoState的AVFormatContext复制过来，不过，不应该用avformat_copy呢？
    is->ic = ic;

    if (genpts)
        ic->flags |= AVFMT_FLAG_GENPTS;
    //这个方法表示将VideoState放入到每一个packet中
    av_format_inject_global_side_data(ic);

    if (find_stream_info) {
        //为ic中的每个流创建一个字典，字典中包含codec_opts
        AVDictionary **opts = setup_find_stream_info_opts(ic, codec_opts);
        int orig_nb_streams = ic->nb_streams;
        //找流
        err = avformat_find_stream_info(ic, opts);

        for (i = 0; i < orig_nb_streams; i++)
            //释放字典？？没干啥就释放了？emmm，字典被用于avformat_find_stream_info了
            av_dict_free(&opts[i]);
        av_freep(&opts);
    }
    for (i = 0; i < ic->nb_streams; i++) {
        AVStream *st = ic->streams[i];
        enum AVMediaType type = st->codecpar->codec_type;
        st->discard = AVDISCARD_ALL;
        if (type >= 0 && wanted_stream_spec[type] && st_index[type] == -1)
            /*
             * avformat_match_stream_specifier这个方法是用来检测fmtCtx中的stream是否包含第三个参数
             * 这里的作用猜测为判断流的类别（雾）
             * 这里是根据流的类型匹配流索引
             * */
            if (avformat_match_stream_specifier(ic, st, wanted_stream_spec[type]) > 0)
                st_index[type] = i;
    }
    for (i = 0; i < AVMEDIA_TYPE_NB; i++) {
        if (wanted_stream_spec[i] && st_index[i] == -1) {
            st_index[i] = INT_MAX;
        }
    }
    if (!video_disable)
        //寻找视频流索引
        st_index[AVMEDIA_TYPE_VIDEO] =av_find_best_stream(ic, AVMEDIA_TYPE_VIDEO,st_index[AVMEDIA_TYPE_VIDEO], -1, NULL, 0);
    if (!audio_disable)
        st_index[AVMEDIA_TYPE_AUDIO] =av_find_best_stream(ic, AVMEDIA_TYPE_AUDIO,st_index[AVMEDIA_TYPE_AUDIO],st_index[AVMEDIA_TYPE_VIDEO],NULL, 0);
    if (!video_disable && !subtitle_disable)

        st_index[AVMEDIA_TYPE_SUBTITLE] =
                av_find_best_stream(ic, AVMEDIA_TYPE_SUBTITLE,st_index[AVMEDIA_TYPE_SUBTITLE],(st_index[AVMEDIA_TYPE_AUDIO] >= 0 ?st_index[AVMEDIA_TYPE_AUDIO] :st_index[AVMEDIA_TYPE_VIDEO]),NULL, 0);

    is->show_mode = show_mode;
```
代码看起来很长，但是仔细分析一下，发现重复的代码较多。第一步，对各个流的索引进行了初始化，接下来在字典中添加scan_all_pmts，接下就开始寻找流信息了，剩下的就是确定各个流的索引了。这一环节结束后st_index数组中就存储了流索引了。
##### ijkplayer
```
    //分配数组
    memset(st_index, -1, sizeof(st_index));
    //初始化流索引
    is->last_video_stream = is->video_stream = -1;
    is->last_audio_stream = is->audio_stream = -1;
    is->last_subtitle_stream = is->subtitle_stream = -1;
    is->eof = 0;
       /*
     *这条msg在msg_loop中被处理
     * 对应的java事件暂无
     * 表示在当前版本该信息无响应
     * */
    ffp_notify_msg1(ffp, FFP_MSG_OPEN_INPUT);

    if (scan_all_pmts_set)
        av_dict_set(&ffp->format_opts, "scan_all_pmts", NULL, AV_DICT_MATCH_CASE);
    is->ic = ic;

    if (ffp->genpts)
        ic->flags |= AVFMT_FLAG_GENPTS;
    //这个方法不清楚，目的是把AVFormatCtx注入到每一个AVPacket中
    av_format_inject_global_side_data(ic);

    if (ffp->find_stream_info) {
        AVDictionary **opts = setup_find_stream_info_opts(ic, ffp->codec_opts);
        int orig_nb_streams = ic->nb_streams;

        do {
            if (av_stristart(is->filename, "data:", NULL) && orig_nb_streams > 0) {
                //这个for循环用于检测流信息是否存在
                for (i = 0; i < orig_nb_streams; i++) {
                    if (!ic->streams[i] || !ic->streams[i]->codecpar ||
                        ic->streams[i]->codecpar->profile == FF_PROFILE_UNKNOWN) {
                        break;
                    }
                }

                if (i == orig_nb_streams) {
                    break;
                }
            }
            err = avformat_find_stream_info(ic, opts);
        } while (0);
        /*
         * 这条消息在msg_loop中处理
         * 对应的java响应方式为null
         * */
        ffp_notify_msg1(ffp, FFP_MSG_FIND_STREAM_INFO);

        for (i = 0; i < orig_nb_streams; i++)
            av_dict_free(&opts[i]);
        av_freep(&opts);
    }
    int video_stream_count = 0;
    int h264_stream_count = 0;
    int first_h264_stream = -1;
    for (i = 0; i < ic->nb_streams; i++) {
        AVStream *st = ic->streams[i];
        enum AVMediaType type = st->codecpar->codec_type;
        st->discard = AVDISCARD_ALL;
        if (type >= 0 && ffp->wanted_stream_spec[type] && st_index[type] == -1)
            if (avformat_match_stream_specifier(ic, st, ffp->wanted_stream_spec[type]) > 0)
                st_index[type] = i;

        // choose first h264
        //选择视频流
        if (type == AVMEDIA_TYPE_VIDEO) {
            enum AVCodecID codec_id = st->codecpar->codec_id;
            video_stream_count++;
            if (codec_id == AV_CODEC_ID_H264) {
                h264_stream_count++;
                if (first_h264_stream < 0)
                    first_h264_stream = i;
            }
        }
    }
    /*
     * 这一段看起来是确定了h264的索引，也就是h264可以省略掉寻找视频流的过程，
     * 然而下面的代码仍然进行了寻找视频流这一步，也不清楚意义何在
     * */
    if (video_stream_count > 1 && st_index[AVMEDIA_TYPE_VIDEO] < 0) {
        st_index[AVMEDIA_TYPE_VIDEO] = first_h264_stream;
        av_log(NULL, AV_LOG_WARNING, "multiple video stream found, prefer first h264 stream: %d\n",
               first_h264_stream);
    }
    //找流
    if (!ffp->video_disable)
        st_index[AVMEDIA_TYPE_VIDEO] =
                av_find_best_stream(ic, AVMEDIA_TYPE_VIDEO,
                                    st_index[AVMEDIA_TYPE_VIDEO], -1, NULL, 0);
    if (!ffp->audio_disable)
        st_index[AVMEDIA_TYPE_AUDIO] =
                av_find_best_stream(ic, AVMEDIA_TYPE_AUDIO,
                                    st_index[AVMEDIA_TYPE_AUDIO],
                                    st_index[AVMEDIA_TYPE_VIDEO],
                                    NULL, 0);
    if (!ffp->video_disable && !ffp->subtitle_disable)
        st_index[AVMEDIA_TYPE_SUBTITLE] =
                av_find_best_stream(ic, AVMEDIA_TYPE_SUBTITLE,
                                    st_index[AVMEDIA_TYPE_SUBTITLE],
                                    (st_index[AVMEDIA_TYPE_AUDIO] >= 0 ?
                                     st_index[AVMEDIA_TYPE_AUDIO] :
                                     st_index[AVMEDIA_TYPE_VIDEO]),
                                    NULL, 0);

    is->show_mode = ffp->show_mode;
```
这段代码只是寻找了流索引，找到了之后自然是打开了
#### 打开流
##### 
##### ijkplayer
```
 //打开流
if (st_index[AVMEDIA_TYPE_AUDIO] >= 0) {
    //打开指定的流
    stream_component_open(ffp, st_index[AVMEDIA_TYPE_AUDIO]);
} else {
    //如果音频流不存在，则同步方式以视频为主
    ffp->av_sync_type = AV_SYNC_VIDEO_MASTER;
    is->av_sync_type = ffp->av_sync_type;
}
ret = -1;
if (st_index[AVMEDIA_TYPE_VIDEO] >= 0) {
    ret = stream_component_open(ffp, st_index[AVMEDIA_TYPE_VIDEO]);
}
if (is->show_mode == SHOW_MODE_NONE)
    is->show_mode = ret >= 0 ? SHOW_MODE_VIDEO : SHOW_MODE_RDFT;
if (st_index[AVMEDIA_TYPE_SUBTITLE] >= 0) {
    stream_component_open(ffp, st_index[AVMEDIA_TYPE_SUBTITLE]);
}
//该函数在ijkplayer_jni中处理，对应的java事件为null
ffp_notify_msg1(ffp, FFP_MSG_COMPONENT_OPEN);
```
这段代码用于打开流，需要注意地方为ijkplayer在找不到音频是，同步方式变为视频为主。stream_component_open则是打开流的具体方法，参数为ffplay实例和对应的流索引
- stream_component_open
```
static int stream_component_open(FFPlayer *ffp, int stream_index) {
    ...
    //分配AVCodecCtx
    avctx = avcodec_alloc_context3(NULL);
    //copy编解码器属性
    ret = avcodec_parameters_to_context(avctx, ic->streams[stream_index]->codecpar);
    //设置时间基准
    av_codec_set_pkt_timebase(avctx, ic->streams[stream_index]->time_base);
    //寻找解码器
    codec = avcodec_find_decoder(avctx->codec_id);
    //设置流索引和codec_name
    switch (avctx->codec_type) {
        case AVMEDIA_TYPE_AUDIO   :
            is->last_audio_stream = stream_index;
            forced_codec_name = ffp->audio_codec_name;
            break;
        case AVMEDIA_TYPE_SUBTITLE:
            is->last_subtitle_stream = stream_index;
            forced_codec_name = ffp->subtitle_codec_name;
            break;
        case AVMEDIA_TYPE_VIDEO   :
            is->last_video_stream = stream_index;
            forced_codec_name = ffp->video_codec_name;
            break;
        default:
            break;
    }
    //寻找解码器
    if (forced_codec_name)
        codec = avcodec_find_decoder_by_name(forced_codec_name);
    avctx->codec_id = codec->id;
    //不清楚stream_lowres具体是什么
    if (stream_lowres > av_codec_get_max_lowres(codec)) {
        stream_lowres = av_codec_get_max_lowres(codec);
    }
    av_codec_set_lowres(avctx, stream_lowres);
    if (ffp->fast)
        avctx->flags2 |= AV_CODEC_FLAG2_FAST;
    //这段代码是设置解码器参数并寻找解码器
    opts = filter_codec_opts(ffp->codec_opts, avctx->codec_id, ic, ic->streams[stream_index],
                             codec);
    if (!av_dict_get(opts, "threads", NULL, 0))
        av_dict_set(&opts, "threads", "auto", 0);
    if (stream_lowres)
        av_dict_set_int(&opts, "lowres", stream_lowres, 0);
    if (avctx->codec_type == AVMEDIA_TYPE_VIDEO || avctx->codec_type == AVMEDIA_TYPE_AUDIO)
        av_dict_set(&opts, "refcounted_frames", "1", 0);
    //打开解码器
    if ((ret = avcodec_open2(avctx, codec, &opts)) < 0) {
        goto fail;
    }
    if ((t = av_dict_get(opts, "", NULL, AV_DICT_IGNORE_SUFFIX))) {
        av_log(NULL, AV_LOG_ERROR, "Option %s not found.\n", t->key);
    }
    is->eof = 0;
    ic->streams[stream_index]->discard = AVDISCARD_DEFAULT;
    ...
}
```
这段代码主要是对解码器进行操作，接下来看下stream_component_open中不同流的不同操作
- 音频
```
switch (avctx->codec_type) {
    case AVMEDIA_TYPE_AUDIO:
        //设置采样率，声道数、声道布局
        sample_rate = avctx->sample_rate;
        nb_channels = avctx->channels;
        channel_layout = avctx->channel_layout;
        /* prepare audio output */
        //打开音频输出设备，并且设置下音频的参数，逐利并没有进行音频的解码与播放
        if ((ret = audio_open(ffp, channel_layout, nb_channels, sample_rate, &is->audio_tgt)) <0)
            goto fail;
        //设置音频的解码器参数
        ffp_set_audio_codec_info(ffp, AVCODEC_MODULE_NAME, avcodec_get_name(avctx->codec_id));
        is->audio_hw_buf_size = ret;
        is->audio_src = is->audio_tgt;
        is->audio_buf_size = 0;
        is->audio_buf_index = 0;
        /* init averaging filter */
        is->audio_diff_avg_coef = exp(log(0.01) / AUDIO_DIFF_AVG_NB);
        is->audio_diff_avg_count = 0;
        /* since we do not have a precise anough audio FIFO fullness,
           we correct audio sync only if larger than this threshold */
        is->audio_diff_threshold = 2.0 * is->audio_hw_buf_size / is->audio_tgt.bytes_per_sec;
        is->audio_stream = stream_index;
        is->audio_st = ic->streams[stream_index];
        /*
         * 注册解码器
         * 两个sdl方法也找不到相关资料
         * */
        decoder_init(&is->auddec, avctx, &is->audioq, is->continue_read_thread);
        if ((is->ic->iformat->flags &(AVFMT_NOBINSEARCH | AVFMT_NOGENSEARCH | AVFMT_NO_BYTE_SEEK)) &&!is->ic->iformat->read_seek) {
            is->auddec.start_pts = is->audio_st->start_time;
            is->auddec.start_pts_tb = is->audio_st->time_base;
        }
        //decoder_start中第二个方法为线程方法
        if ((ret = decoder_start(&is->auddec, audio_thread, ffp, "ff_audio_dec")) < 0)
            goto out;
        SDL_AoutPauseAudio(ffp->aout, 0);
        break;
```
这段代码包括了audio_open、ffp_set_audio_codec_info、decoder_init、decoder_start这四个方法，去除掉第一个音频输出方法,第二个方法用于设置编码格式外，剩下的两个个方法都与解码器有关，我们进入看一下
```
static void decoder_init(Decoder *d, AVCodecContext *avctx, PacketQueue *queue, SDL_cond *empty_queue_cond) {
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
很简单，不详细解释了，来看下decoder_start方法
```
static int decoder_start(Decoder *d, int (*fn)(void *), void *arg, const char *name) {

    //队列启动
    packet_queue_start(d->queue);
    d->decoder_tid = SDL_CreateThreadEx(&d->_decoder_tid, fn, arg, name);
    return 0;
}
```
启动队列(AudioPacketQueue)，执行线程方法(audio_thread)。
```
static int audio_thread(void *arg) {
    AVFrame *frame = av_frame_alloc();
    do {
        //这个方法用于设置缓存中的时间差
        ffp_audio_statistic_l(ffp);
        //decoder_decode_frame用于解码数据，解码后的数据放到frame中
        if ((got_frame = decoder_decode_frame(ffp, &is->auddec, frame, NULL)) < 0)
            goto the_end;

        if (got_frame) {
            if (!(af = frame_queue_peek_writable(&is->sampq)))
                goto the_end;

            af->pts = (frame->pts == AV_NOPTS_VALUE) ? NAN : frame->pts * av_q2d(tb);
            af->pos = frame->pkt_pos;
            af->serial = is->auddec.pkt_serial;
            af->duration = av_q2d((AVRational) {frame->nb_samples, frame->sample_rate});

            av_frame_move_ref(af->frame, frame);
            frame_queue_push(&is->sampq);
        }
    } while (ret >= 0 || ret == AVERROR(EAGAIN) || ret == AVERROR_EOF);
}
```
去掉seek代码后就变得很简洁了。seek操作的话后面在说吧。这段代码中包括了ffp_audio_statistic_l、decoder_decode_frame、frame_queue_peek_writable、frame_queue_push这四个方法。
```
static int decoder_decode_frame(FFPlayer *ffp, Decoder *d, AVFrame *frame, AVSubtitle *sub) {
    for (;;) {
        AVPacket pkt;
        if (d->queue->serial == d->pkt_serial) {
            do {
                if (d->queue->abort_request)
                    return -1;
                switch (d->avctx->codec_type) {
                    case AVMEDIA_TYPE_VIDEO:
                        //接受解码后的帧(原始数据) 第一次这个frame是null的，因为没有进行send_packet
                        ret = avcodec_receive_frame(d->avctx, frame);
                        if (ret >= 0) {
                            ffp->stat.vdps = SDL_SpeedSamplerAdd(&ffp->vdps_sampler,
                                                                 FFP_SHOW_VDPS_AVCODEC,
                                                                 "vdps[avcodec]");
                            if (ffp->decoder_reorder_pts == -1) {
                                frame->pts = frame->best_effort_timestamp;
                            } else if (!ffp->decoder_reorder_pts) {
                                frame->pts = frame->pkt_dts;
                            }
                        }
                        break;
                    case AVMEDIA_TYPE_AUDIO:
                        ret = avcodec_receive_frame(d->avctx, frame);
                        if (ret >= 0) {
                            //确定时间戳
                            AVRational tb = (AVRational) {1, frame->sample_rate};
                            if (frame->pts != AV_NOPTS_VALUE)
                                //重新确定音频时间戳
                                frame->pts = av_rescale_q(frame->pts,
                                                          av_codec_get_pkt_timebase(d->avctx), tb);
                            else if (d->next_pts != AV_NOPTS_VALUE)
                                frame->pts = av_rescale_q(d->next_pts, d->next_pts_tb, tb);
                            if (frame->pts != AV_NOPTS_VALUE) {
                                d->next_pts = frame->pts + frame->nb_samples;
                                d->next_pts_tb = tb;
                            }
                        }
                        break;
                    default:
                        break;
                }
                if (ret == AVERROR_EOF) {
                    d->finished = d->pkt_serial;
                    //清空解码器缓存
                    avcodec_flush_buffers(d->avctx);
                    return 0;
                }
                if (ret >= 0)
                    return 1;
            } while (ret != AVERROR(EAGAIN));
        }

        do {
            if (d->queue->nb_packets == 0)
                SDL_CondSignal(d->empty_queue_cond);
            if (d->packet_pending) {
                //move语义，用于移动packet
                av_packet_move_ref(&pkt, &d->pkt);
                d->packet_pending = 0;
            } else {
                //简单的来讲就是从queue中去取数据
                if (packet_queue_get_or_buffering(ffp, d->queue, &pkt, &d->pkt_serial, &d->finished) < 0)
                    return -1;
            }
        } while (d->queue->serial != d->pkt_serial);

        if (pkt.data == flush_pkt.data) {
            avcodec_flush_buffers(d->avctx);
            d->finished = 0;
            d->next_pts = d->start_pts;
            d->next_pts_tb = d->start_pts_tb;
        } else {
            if (d->avctx->codec_type == AVMEDIA_TYPE_SUBTITLE) {
                int got_frame = 0;
                //解码字幕
                ret = avcodec_decode_subtitle2(d->avctx, sub, &got_frame, &pkt);
                if (ret < 0) {
                    ret = AVERROR(EAGAIN);
                } else {
                    if (got_frame && !pkt.data) {
                        d->packet_pending = 1;
                        av_packet_move_ref(&d->pkt, &pkt);
                    }
                    ret = got_frame ? 0 : (pkt.data ? AVERROR(EAGAIN) : AVERROR_EOF);
                }
            } else {
                //这里开始向解码器发送数据包
                if (avcodec_send_packet(d->avctx, &pkt) == AVERROR(EAGAIN)) {
                    d->packet_pending = 1;
                    av_packet_move_ref(&d->pkt, &pkt);
                }
            }
            av_packet_unref(&pkt);
        }
    }
}
```
因为第一次进入的时候，frame为null并且没有进行sendpacket，所以第一个while循环不会执行，第二个while循环用于从对应的PacketQueue中去取packet。下一个环节if条件中的flush_pkt说明一下，当启动，seek一级packet.flag为AV_PKT_FLAG_DISCONTINUITY时会向队列中放入flush_pkt。当send_packet成功后，进入第一个循环接受解码后的帧(原始数据),之后音频确定下pts，视频进行下转换。执行完成之后返回1（EOF返回0，从packet中取数据失败返回-1）
之后通过frame_queue_peek_writable方法获取framequeue中的可用节点，获取节点后通过av_frame_move_ref进行frame的移动，最后通过frame_queue_push方法修改queue的数据个数与可读的索引（因为直接move，所以这个push方法并不是入队，而是入队后队列需要执行的操作）
- 视频
和音频相比，视频多了一个硬解的步骤
```
case AVMEDIA_TYPE_VIDEO:
    is->video_stream = stream_index;
    is->video_st = ic->streams[stream_index]
    if (ffp->async_init_decoder) {
        while (!is->initialized_decoder) {
            SDL_Delay(5);
        }
        if (ffp->node_vdec) {
            is->viddec.avctx = avctx;
            ret = ffpipeline_config_video_decoder(ffp->pipeline, ffp);
        }
        if (ret || !ffp->node_vdec) {
            decoder_init(&is->viddec, avctx, &is->videoq, is->continue_read_thread);
            ffp->node_vdec = ffpipeline_open_video_decoder(ffp->pipeline, ffp);
            if (!ffp->node_vdec)
                goto fail;
        }
    } else {
        decoder_init(&is->viddec, avctx, &is->videoq, is->continue_read_thread);
        ffp->node_vdec = ffpipeline_open_video_decoder(ffp->pipeline, ffp);
        if (!ffp->node_vdec)
            goto fail;
    }
    if ((ret = decoder_start(&is->viddec, video_thread, ffp, "ff_video_dec")) < 0)
        goto out
    is->queue_attachments_req = 1
    if (ffp->max_fps >= 0) {
        if (is->video_st->avg_frame_rate.den && is->video_st->avg_frame_rate.num) {
            double fps = av_q2d(is->video_st->avg_frame_rate);
            SDL_ProfilerReset(&is->viddec.decode_profiler, fps + 0.5);
            if (fps > ffp->max_fps && fps < 130.0) {
                is->is_video_high_fps = 1;
            }
        }
        if (is->video_st->r_frame_rate.den && is->video_st->r_frame_rate.num) {
            double tbr = av_q2d(is->video_st->r_frame_rate);
            if (tbr > ffp->max_fps && tbr < 130.0) {
                is->is_video_high_fps = 1;
            }
        }
    
    if (is->is_video_high_fps) {
        avctx->skip_frame = FFMAX(avctx->skip_frame, AVDISCARD_NONREF);
        avctx->skip_loop_filter = FFMAX(avctx->skip_loop_filter, AVDISCARD_NONREF);
        avctx->skip_idct = FFMAX(avctx->skip_loop_filter, AVDISCARD_NONREF);
break;
```
其中包含ffpipeline_config_video_decoder、ffpipeline_open_video_decoder、video_thread这三个方法
ffpipeline_config_video_decoder用于检测硬件编码设备，并配置了加码器所需要的宽高、surface以及csd-0和csd-1
ffpipeline_open_video_decoder则用于打开MediaCodec，如果各种原因导致打开失败，则打开AVCodec
video_thread中执行了ffpipenode_run_sync方法，而ffpipenode_run_sync最终指向了func_run_sync_loop和func_run_sync
```
if (ffp->mediacodec_sync) {
        node->func_run_sync = func_run_sync_loop;
    } else {
        node->func_run_sync = func_run_sync;
    }
```
经过全文搜索发现mediacodec_sync始终为false，所以来看一下func_run_sync方法
```
static int func_run_sync(IJKFF_Pipenode *node)
{
    if (!opaque->acodec) {
        return ffp_video_thread(ffp);
    }

    frame = av_frame_alloc();

    opaque->enqueue_thread = SDL_CreateThreadEx(&opaque->_enqueue_thread, enqueue_thread_func, node, "amediacodec_input_thread");

    while (!q->abort_request) {
        int64_t timeUs = opaque->acodec_first_dequeue_output_request ? 0 : AMC_OUTPUT_TIMEOUT_US;
        got_frame = 0;
        ret = drain_output_buffer(env, node, timeUs, &dequeue_count, frame, &got_frame);
        if (opaque->acodec_first_dequeue_output_request) {
            opaque->acodec_first_dequeue_output_request = false;
        }
        if (got_frame) {
            duration = (frame_rate.num && frame_rate.den ? av_q2d((AVRational){frame_rate.den, frame_rate.num}) : 0);
            pts = (frame->pts == AV_NOPTS_VALUE) ? NAN : frame->pts * av_q2d(tb);
            if (ffp->framedrop > 0 || (ffp->framedrop && ffp_get_master_sync_type(is) != AV_SYNC_VIDEO_MASTER)) {
                ffp->stat.decode_frame_count++;
                if (frame->pts != AV_NOPTS_VALUE) {
                    double dpts = pts;
                    double diff = dpts - ffp_get_master_clock(is);
                    if (!isnan(diff) && fabs(diff) < AV_NOSYNC_THRESHOLD &&
                        diff - is->frame_last_filter_delay < 0 &&
                        is->viddec.pkt_serial == is->vidclk.serial &&
                        is->videoq.nb_packets) {
                        is->frame_drops_early++;
                        is->continuous_frame_drops_early++;
                        if (is->continuous_frame_drops_early > ffp->framedrop) {
                            is->continuous_frame_drops_early = 0;
                        } else {
                            ffp->stat.drop_frame_count++;
                            ffp->stat.drop_frame_rate = (float)(ffp->stat.drop_frame_count) / (float)(ffp->stat.decode_frame_count);
                            if (frame->opaque) {
                                SDL_VoutAndroid_releaseBufferProxyP(opaque->weak_vout, (SDL_AMediaCodecBufferProxy **)&frame->opaque, false);
                            }
                            av_frame_unref(frame);
                            continue;
                        }
                    }
                }
            }
            ret = ffp_queue_picture(ffp, frame, pts, duration, av_frame_get_pkt_pos(frame), is->viddec.pkt_serial);
            if (ret) {
                if (frame->opaque)
                    SDL_VoutAndroid_releaseBufferProxyP(opaque->weak_vout, (SDL_AMediaCodecBufferProxy **)&frame->opaque, false);
            }
            av_frame_unref(frame);
        }
    }
}
```
删掉一些多余的代码。这代代码中会执行enqueue_thread_func线程方法、drain_output_buffer、ffp_queue_picture这三个方法
enqueue_thread_func从名字上我们可以看出是执行视频数据填充进解码器操作
```
static int enqueue_thread_func(void *arg)
{
    while (!q->abort_request && !opaque->abort) {
        ret = feed_input_buffer(env, node, AMC_INPUT_TIMEOUT_US, &dequeue_count);
        if (ret != 0) {
            goto fail;
        }
    }
    ret = 0;
}

```
emmm,feed_input_buffer方法
```
static int feed_input_buffer(JNIEnv *env, IJKFF_Pipenode *node, int64_t timeUs, int *enqueue_count)
{

    if (enqueue_count)
        *enqueue_count = 0;

    if (d->queue->abort_request) {
        ret = 0;
        goto fail;
    }

    if (!d->packet_pending || d->queue->serial != d->pkt_serial) {
        AVPacket pkt;
        do {
            if (d->queue->nb_packets == 0)
                SDL_CondSignal(d->empty_queue_cond);
            if (ffp_packet_queue_get_or_buffering(ffp, d->queue, &pkt, &d->pkt_serial, &d->finished) < 0) {
                ret = -1;
                goto fail;
            }
            if (ffp_is_flush_packet(&pkt) || opaque->acodec_flush_request) {
                opaque->acodec_flush_request = true;
                SDL_LockMutex(opaque->acodec_mutex);
                if (SDL_AMediaCodec_isStarted(opaque->acodec)) {
                    if (opaque->input_packet_count > 0) {
                        SDL_VoutAndroid_invalidateAllBuffers(opaque->weak_vout);
                        SDL_AMediaCodec_flush(opaque->acodec);
                        opaque->input_packet_count = 0;
                    }
                }
                opaque->acodec_flush_request = false;
                SDL_CondSignal(opaque->acodec_cond);
                SDL_UnlockMutex(opaque->acodec_mutex);
                d->finished = 0;
                d->next_pts = d->start_pts;
                d->next_pts_tb = d->start_pts_tb;
            }
        } while (ffp_is_flush_packet(&pkt) || d->queue->serial != d->pkt_serial);
        av_packet_split_side_data(&pkt);
        av_packet_unref(&d->pkt);
        d->pkt_temp = d->pkt = pkt;
        d->packet_pending = 1;

        if (opaque->ffp->mediacodec_handle_resolution_change &&
            opaque->codecpar->codec_id == AV_CODEC_ID_H264) {
            uint8_t  *size_data      = NULL;
            int       size_data_size = 0;
            AVPacket *avpkt          = &d->pkt_temp;
            size_data = av_packet_get_side_data(avpkt, AV_PKT_DATA_NEW_EXTRADATA, &size_data_size);
            if (size_data && size_data_size >= 7) {
                int             got_picture = 0;
                AVFrame        *frame      = av_frame_alloc();
                AVDictionary   *codec_opts = NULL;
                const AVCodec  *codec      = opaque->decoder->avctx->codec;
                AVCodecContext *new_avctx  = avcodec_alloc_context3(codec);
                int change_ret = 0;

                avcodec_parameters_to_context(new_avctx, opaque->codecpar);
                av_freep(&new_avctx->extradata);
                new_avctx->extradata = av_mallocz(size_data_size + AV_INPUT_BUFFER_PADDING_SIZE);
                memcpy(new_avctx->extradata, size_data, size_data_size);
                new_avctx->extradata_size = size_data_size;

                av_dict_set(&codec_opts, "threads", "1", 0);
                change_ret = avcodec_open2(new_avctx, codec, &codec_opts);
                av_dict_free(&codec_opts);

                change_ret = avcodec_decode_video2(new_avctx, frame, &got_picture, avpkt);
                if (change_ret < 0) {
                    avcodec_free_context(&new_avctx);
                    return change_ret;
                } else {
                    if (opaque->codecpar->width  != new_avctx->width &&
                        opaque->codecpar->height != new_avctx->height) {
                        ALOGW("AV_PKT_DATA_NEW_EXTRADATA: %d x %d\n", new_avctx->width, new_avctx->height);
                        avcodec_parameters_from_context(opaque->codecpar, new_avctx);
                        opaque->aformat_need_recreate = true;
                        ffpipeline_set_surface_need_reconfigure_l(pipeline, true);
                    }
                }

                av_frame_unref(frame);
                avcodec_free_context(&new_avctx);
            }
        }


        if (opaque->codecpar->codec_id == AV_CODEC_ID_H264 || opaque->codecpar->codec_id == AV_CODEC_ID_HEVC) {
            convert_h264_to_annexb(d->pkt_temp.data, d->pkt_temp.size, opaque->nal_size, &convert_state);
            int64_t time_stamp = d->pkt_temp.pts;
            if (!time_stamp && d->pkt_temp.dts)
                time_stamp = d->pkt_temp.dts;
            if (time_stamp > 0) {
                time_stamp = av_rescale_q(time_stamp, is->video_st->time_base, AV_TIME_BASE_Q);
            } else {
                time_stamp = 0;
            }
        }
    }

    if (d->pkt_temp.data) {
        if (ffpipeline_is_surface_need_reconfigure_l(pipeline)) {
            jobject new_surface = NULL;

            ffpipeline_lock_surface(pipeline);
            ffpipeline_set_surface_need_reconfigure_l(pipeline, false);
            new_surface = ffpipeline_get_surface_as_global_ref_l(env, pipeline);
            ffpipeline_unlock_surface(pipeline);

            if (!opaque->aformat_need_recreate &&
                (opaque->jsurface == new_surface ||
                (opaque->jsurface && new_surface && (*env)->IsSameObject(env, new_surface, opaque->jsurface)))) {
                J4A_DeleteGlobalRef__p(env, &new_surface);
            } else {
                if (opaque->aformat_need_recreate) {
                    ret = recreate_format_l(env, node);
        
                    opaque->aformat_need_recreate = false;
                }

                opaque->acodec_reconfigure_request = true;
                SDL_LockMutex(opaque->acodec_mutex);
                ret = reconfigure_codec_l(env, node, new_surface);
                opaque->acodec_reconfigure_request = false;
                SDL_CondSignal(opaque->acodec_cond);
                SDL_UnlockMutex(opaque->acodec_mutex);

                J4A_DeleteGlobalRef__p(env, &new_surface);

                SDL_LockMutex(opaque->acodec_first_dequeue_output_mutex);
                while (!q->abort_request &&
                    !opaque->acodec_reconfigure_request &&
                    !opaque->acodec_flush_request &&
                    opaque->acodec_first_dequeue_output_request) {
                    SDL_CondWaitTimeout(opaque->acodec_first_dequeue_output_cond, opaque->acodec_first_dequeue_output_mutex, 100);
                }
                SDL_UnlockMutex(opaque->acodec_first_dequeue_output_mutex);

                if (q->abort_request || opaque->acodec_reconfigure_request || opaque->acodec_flush_request) {
                    ret = 0;
                    goto fail;
                }
            }
        }

        queue_flags = 0;
        input_buffer_index = SDL_AMediaCodec_dequeueInputBuffer(opaque->acodec, timeUs);
        if (input_buffer_index < 0) {
            if (SDL_AMediaCodec_isInputBuffersValid(opaque->acodec)) {
                ret = 0;
                goto fail;
            } else {
                queue_flags |= AMEDIACODEC__BUFFER_FLAG_FAKE_FRAME;
                copy_size    = d->pkt_temp.size;
            }
        } else {
            SDL_AMediaCodecFake_flushFakeFrames(opaque->acodec);

            copy_size = SDL_AMediaCodec_writeInputData(opaque->acodec, input_buffer_index, d->pkt_temp.data, d->pkt_temp.size);
        }

        time_stamp = d->pkt_temp.pts;
        if (time_stamp == AV_NOPTS_VALUE && d->pkt_temp.dts != AV_NOPTS_VALUE)
            time_stamp = d->pkt_temp.dts;
        if (time_stamp >= 0) {
            time_stamp = av_rescale_q(time_stamp, is->video_st->time_base, AV_TIME_BASE_Q);
        } else {
            time_stamp = 0;
        }
        amc_ret = SDL_AMediaCodec_queueInputBuffer(opaque->acodec, input_buffer_index, 0, copy_size, time_stamp, queue_flags);
        if (amc_ret != SDL_AMEDIA_OK) {
            ALOGE("%s: SDL_AMediaCodec_getInputBuffer failed\n", __func__);
            ret = -1;
            goto fail;
        }
        opaque->input_packet_count++;
        if (enqueue_count)
            ++*enqueue_count;
    }

    if (copy_size < 0) {
        d->packet_pending = 0;
    } else {
        d->pkt_temp.dts =
        d->pkt_temp.pts = AV_NOPTS_VALUE;
        if (d->pkt_temp.data) {
            d->pkt_temp.data += copy_size;
            d->pkt_temp.size -= copy_size;
            if (d->pkt_temp.size <= 0)
                d->packet_pending = 0;
        } else {
                d->packet_pending = 0;
                d->finished = d->pkt_serial;
        }
    }

}
```