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
        //这是意味着有有音频流就必须有视频流
        st_index[AVMEDIA_TYPE_AUDIO] =av_find_best_stream(ic, AVMEDIA_TYPE_AUDIO,st_index[AVMEDIA_TYPE_AUDIO],st_index[AVMEDIA_TYPE_VIDEO],NULL, 0);
    if (!video_disable && !subtitle_disable)
        //这里确定了字幕流必须与音频流或视频流相关
        st_index[AVMEDIA_TYPE_SUBTITLE] =
                av_find_best_stream(ic, AVMEDIA_TYPE_SUBTITLE,st_index[AVMEDIA_TYPE_SUBTITLE],(st_index[AVMEDIA_TYPE_AUDIO] >= 0 ?st_index[AVMEDIA_TYPE_AUDIO] :st_index[AVMEDIA_TYPE_VIDEO]),NULL, 0);

    is->show_mode = show_mode;
```
代码看起来很长，但是仔细分析一下，发现重复的代码较多。第一步，对各个流的索引进行了初始化，接下来在字典中添加scan_all_pmts，接下就开始寻找流信息了，剩下的就是确定各个流的索引了。因为ffplay以视频为主，所以音频在这里不是必须的。这一环节结束后st_index数组中就存储了流索引了。
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
    if (video_stream_count > 1 && st_index[AVMEDIA_TYPE_VIDEO] < 0) {
        st_index[AVMEDIA_TYPE_VIDEO] = first_h264_stream;
        av_log(NULL, AV_LOG_WARNING, "multiple video stream found, prefer first h264 stream: %d\n",
               first_h264_stream);
    }
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


