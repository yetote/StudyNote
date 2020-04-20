#### AVInputFormat
AVInputFormat仅用于解封装器,在av_register_all(新版已删除)函数中被注册并在链表中保存所有的AVInputFormat。
源码基于FFmpeg4.2.1

- name
    数据类型|含义
    -|-  
    const char *|封装器名称
    
- long_name
    数据类型|含义
    -|-  
    const char *|全称
    
-  flags
    数据类型|含义
    -|-  
    int|标志

    这里虽然类型为int，但是在注释中给出了所有的标志码，这些标志码定义在avformat.h中。

    标志码|含义
    -|-  
    AVFMT_NOFILE|解封装器将使用avio_open，调用者不提供任何打开的文件
    AVFMT_NEEDNUMBER|文件名需要使用%d
    AVFMT_SHOW_IDS|显示format流的id号码
    AVFMT_NOTIMESTAMPS|format不需要时间戳
    AVFMT_GENERIC_INDEX|使用通用索引构建代码
    AVFMT_TS_DISCONT|格式允许时间戳不连续
    AVFMT_NOBINSEARCH|格式不允许通过read_timestamp返回二进制搜索
    AVFMT_NOGENSEARCH|格式不允许使用一般搜索
    AVFMT_NO_BYTE_SEEK|格式不允许按字节跳转
    AVFMT_SEEK_TO_PTS|基于PTS进行跳转

    注释中给出的flag并不是全部的flag，还有
    标志码|含义
    -|- 
    AVFMT_GLOBALHEADER|格式要全局的header
    AVFMT_VARIABLE_FPS|格式允许可变fps
    AVFMT_NODIMENSIONS|格式不需要宽度/高度
    AVFMT_NOSTREAMS|格式不需要任何流
    AVFMT_ALLOW_FLUSH|格式允许刷新
    AVFMT_TS_NONSTRICT|格式不需要严格增加时间戳，但是它们仍然必须是单调的
    AVFMT_TS_NEGATIVE|格式允许合并否定时间戳记

    这些标记有一些翻译的很晦涩，但是，这些都会在函数中使用到。我们在之后使用到的时候通过格式、特征以及使用场景再次进行分析。


- extensions
    数据类型|含义
    -|-  
    const char *|扩展名
    
- codec_tag;
    数据类型|含义
    -|-  
    const struct AVCodecTag * const *|AVCodecTag

    AVCodecTag结构体比较简单，直接在这里列出来就可以
    ```
    typedef struct AVCodecTag {
        enum AVCodecID id;
        unsigned int tag;
    } AVCodecTag;
    ```
    AVCodecTag有两个成员，AVCodecID和一个无符号类型的tag。
- priv_class
    数据类型|含义
    -|-  
    const AVClass *|私有的AVClass
    AVClass之前已经阅读过，主要的作用是关联AVXXXFormat和AVOption。
   
- mime_type
    数据类型|含义
    -|-  
    const char *|mime_type
    
- next
    数据类型|含义
    -|-  
    ff_const59 struct AVInputFormat *|AVInputFormat节点
    这里我们看出AVInputFormat为一个链表，所以猜测为这个字段表示当前各式所支持的下一个AVInputForma。

-  raw_codec_id
    数据类型|含义
    -|-  
    int|解码器id
    
-  priv_data_size
    数据类型|含义
    -|-  
    int|与格式对应的Context的大小
    
-  read_probe
    数据类型|含义
    -|-  
    int (*)(const AVProbeData *)|判断文件是否可以解析为此格式。
   
- read_header
    数据类型|含义
    -|-  
     int (*)(struct AVFormatContext *)|读取文件头并初始化AVFormatContext
    
- read_packet
    数据类型|含义
    -|-  
    int (*read_packet)(struct AVFormatContext *, AVPacket *pkt)|读取数据包并将其放入到pkt中，也就是从数据中拆分数据包。
   
- read_close
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *)|关闭流，AVFormatContext和AVStream不会被释放
    
- read_seek
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *, int stream_index, int64_t timestamp, int flags)|从指定的时间戳处读取数据，也就是跳转。
    
- read_timestamp
    数据类型|含义
    -|-  
    int64_t (*)(struct AVFormatContext *s, int stream_index,int64_t *pos, int64_t pos_limit)|以time_base为单位获取流的下一个时间戳。
    
- read_play
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *)|开始/恢复播放。
    
- read_pause
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *)|暂停
    
- read_seek2
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *s, int stream_index, int64_t min_ts, int64_t ts, int64_t max_ts, int flags)|也是用于seek的函数，要求要求跳转的时间逼近ts并在mix和max之间。
   
- get_device_list
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *s, struct AVDeviceInfoList *device_list)|获取设备列表
    
- create_device_capabilities
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *s, struct AVDeviceCapabilitiesQuery *caps)|初始化设备功能子模块
    
- free_device_capabilities
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *s, struct AVDeviceCapabilitiesQuery *caps)|释放设备功能子模块

### 实例
- aac
```
AVInputFormat ff_aac_demuxer = {
    .name         = "aac",
    .long_name    = NULL_IF_CONFIG_SMALL("raw ADTS AAC (Advanced Audio Coding)"),
    .read_probe   = adts_aac_probe,
    .read_header  = adts_aac_read_header,
    .read_packet  = adts_aac_read_packet,
    .flags        = AVFMT_GENERIC_INDEX,
    .extensions   = "aac",
    .mime_type    = "audio/aac,audio/aacp,audio/x-aac",
    .raw_codec_id = AV_CODEC_ID_AAC,
};

```
NULL_IF_CONFIG_SMALL根据CONFIG_SMALL判断long_name是否为NULL
```
#if CONFIG_SMALL
#   define NULL_IF_CONFIG_SMALL(x) NULL
#else
#   define NULL_IF_CONFIG_SMALL(x) x
#endif
```
- mp3
```
AVInputFormat ff_mp3_demuxer = {
    .name           = "mp3",
    .long_name      = NULL_IF_CONFIG_SMALL("MP2/3 (MPEG audio layer 2/3)"),
    .read_probe     = mp3_read_probe,
    .read_header    = mp3_read_header,
    .read_packet    = mp3_read_packet,
    .read_seek      = mp3_seek,
    .priv_data_size = sizeof(MP3DecContext),
    .flags          = AVFMT_GENERIC_INDEX,
    .extensions     = "mp2,mp3,m2a,mpa", /* XXX: use probe */
    .priv_class     = &demuxer_class,
};

```
### 源码
```
/**
 * @addtogroup lavf_decoding
 * @{
 */
typedef struct AVInputFormat {
    /**
     * A comma separated list of short names for the format. New names
     * may be appended with a minor bump.
     */
    const char *name;

    /**
     * Descriptive name for the format, meant to be more human-readable
     * than name. You should use the NULL_IF_CONFIG_SMALL() macro
     * to define it.
     */
    const char *long_name;

    /**
     * Can use flags: AVFMT_NOFILE, AVFMT_NEEDNUMBER, AVFMT_SHOW_IDS,
     * AVFMT_NOTIMESTAMPS, AVFMT_GENERIC_INDEX, AVFMT_TS_DISCONT, AVFMT_NOBINSEARCH,
     * AVFMT_NOGENSEARCH, AVFMT_NO_BYTE_SEEK, AVFMT_SEEK_TO_PTS.
     */
    int flags;

    /**
     * If extensions are defined, then no probe is done. You should
     * usually not use extension format guessing because it is not
     * reliable enough
     */
    const char *extensions;

    const struct AVCodecTag * const *codec_tag;

    const AVClass *priv_class; ///< AVClass for the private context

    /**
     * Comma-separated list of mime types.
     * It is used check for matching mime types while probing.
     * @see av_probe_input_format2
     */
    const char *mime_type;

    /*****************************************************************
     * No fields below this line are part of the public API. They
     * may not be used outside of libavformat and can be changed and
     * removed at will.
     * New public fields should be added right above.
     *****************************************************************
     */
    ff_const59 struct AVInputFormat *next;

    /**
     * Raw demuxers store their codec ID here.
     */
    int raw_codec_id;

    /**
     * Size of private data so that it can be allocated in the wrapper.
     */
    int priv_data_size;

    /**
     * Tell if a given file has a chance of being parsed as this format.
     * The buffer provided is guaranteed to be AVPROBE_PADDING_SIZE bytes
     * big so you do not have to check for that unless you need more.
     */
    int (*read_probe)(const AVProbeData *);

    /**
     * Read the format header and initialize the AVFormatContext
     * structure. Return 0 if OK. 'avformat_new_stream' should be
     * called to create new streams.
     */
    int (*read_header)(struct AVFormatContext *);

    /**
     * Read one packet and put it in 'pkt'. pts and flags are also
     * set. 'avformat_new_stream' can be called only if the flag
     * AVFMTCTX_NOHEADER is used and only in the calling thread (not in a
     * background thread).
     * @return 0 on success, < 0 on error.
     *         When returning an error, pkt must not have been allocated
     *         or must be freed before returning
     */
    int (*read_packet)(struct AVFormatContext *, AVPacket *pkt);

    /**
     * Close the stream. The AVFormatContext and AVStreams are not
     * freed by this function
     */
    int (*read_close)(struct AVFormatContext *);

    /**
     * Seek to a given timestamp relative to the frames in
     * stream component stream_index.
     * @param stream_index Must not be -1.
     * @param flags Selects which direction should be preferred if no exact
     *              match is available.
     * @return >= 0 on success (but not necessarily the new offset)
     */
    int (*read_seek)(struct AVFormatContext *,
                     int stream_index, int64_t timestamp, int flags);

    /**
     * Get the next timestamp in stream[stream_index].time_base units.
     * @return the timestamp or AV_NOPTS_VALUE if an error occurred
     */
    int64_t (*read_timestamp)(struct AVFormatContext *s, int stream_index,
                              int64_t *pos, int64_t pos_limit);

    /**
     * Start/resume playing - only meaningful if using a network-based format
     * (RTSP).
     */
    int (*read_play)(struct AVFormatContext *);

    /**
     * Pause playing - only meaningful if using a network-based format
     * (RTSP).
     */
    int (*read_pause)(struct AVFormatContext *);

    /**
     * Seek to timestamp ts.
     * Seeking will be done so that the point from which all active streams
     * can be presented successfully will be closest to ts and within min/max_ts.
     * Active streams are all streams that have AVStream.discard < AVDISCARD_ALL.
     */
    int (*read_seek2)(struct AVFormatContext *s, int stream_index, int64_t min_ts, int64_t ts, int64_t max_ts, int flags);

    /**
     * Returns device list with it properties.
     * @see avdevice_list_devices() for more details.
     */
    int (*get_device_list)(struct AVFormatContext *s, struct AVDeviceInfoList *device_list);

    /**
     * Initialize device capabilities submodule.
     * @see avdevice_capabilities_create() for more details.
     */
    int (*create_device_capabilities)(struct AVFormatContext *s, struct AVDeviceCapabilitiesQuery *caps);

    /**
     * Free device capabilities submodule.
     * @see avdevice_capabilities_free() for more details.
     */
    int (*free_device_capabilities)(struct AVFormatContext *s, struct AVDeviceCapabilitiesQuery *caps);
} AVInputFormat;
```