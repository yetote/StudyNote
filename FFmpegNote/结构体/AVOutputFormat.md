### AVOutputFormat
 - name
    数据类型|含义
    -|-  
    const char *|名称 

 - long_name
    数据类型|含义
    -|-  
    const char *|全称 

 - mime_type
    数据类型|含义
    -|-  
    const char *|mime类型

 - extensions 
    数据类型|含义
    -|-  
    const char *|扩展名  

 - audio_codec，video_codec，subtitle_codec
    数据类型|含义
    -|-  
    enum AVCodecID|编码器id

 -  flags
    数据类型|含义
    -|-  
    int|标志
    与AVInputFormat不同。
    标志码|含义|是否支持AVInputFormat
    -|-|-
    AVFMT_NOFILE|解封装器将使用avio_open，调用者不提供任何打开的文件|支持    
    AVFMT_NEEDNUMBER|文件名需要使用%d|支持
    AVFMT_GLOBALHEADER|格式要全局的header|不支持
    AVFMT_NOTIMESTAMPS|format不需要时间戳|支持
    AVFMT_VARIABLE_FPS|格式允许可变fps|不支持
    AVFMT_NODIMENSIONS| 格式不需要宽度/高度|不支持
    AVFMT_NOSTREAMS|格式不需要任何流|不支持
    AVFMT_ALLOW_FLUSH|格式允许刷新|支持
    AVFMT_TS_NONSTRICT|格式不需要严格增加时间戳，但是它们仍然必须是单调的|不支持
    AVFMT_TS_NEGATIVE|格式允许合并否定时间戳记|不支持

    或许以下表格更为直观
    标志码|AVInputFormat|AVOutputFormat
    -|-|-
    AVFMT_NOFILE|:laughing:|:laughing:
    AVFMT_NEEDNUMBER|:laughing:|:laughing:
    AVFMT_SHOW_IDS|:laughing:|:disappointed:
    AVFMT_GLOBALHEADER|:disappointed:|:laughing:
    AVFMT_NOTIMESTAMPS|:laughing:|:laughing:
    AVFMT_GENERIC_INDEX|:laughing:|:disappointed:
    AVFMT_TS_DISCONT|:laughing:|:disappointed:
    AVFMT_VARIABLE_FPS|:disappointed:|:laughing:
    AVFMT_NODIMENSIONS|:disappointed:|:laughing:
    AVFMT_NOSTREAMS|:disappointed:|:laughing:
    AVFMT_NOBINSEARCH|:laughing:|:disappointed:
    AVFMT_NOGENSEARCH|:laughing:|:disappointed:
    AVFMT_NO_BYTE_SEEK|:laughing:|:disappointed:
    AVFMT_ALLOW_FLUSH|:disappointed:|:laughing:
    AVFMT_TS_NONSTRICT|:disappointed:|:laughing:
    AVFMT_TS_NEGATIVE|:disappointed:|:laughing:
    AVFMT_SEEK_TO_PTS|:laughing:|:disappointed:
 - codec_tag
    数据类型|含义
    -|-  
    const struct AVCodecTag * const *|编码器标记

 - priv_class
    数据类型|含义
    -|-  
    const AVClass *|AVClass

 - next
    数据类型|含义
    -|-  
    ff_const59 struct AVOutputFormat *|指向AVOutputFormat的下一节点指针

 -  priv_data_size
    数据类型|含义
    -|-  
    int|Format对应Context的大小

 - write_header
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *)|写入文件头
    这个函数没有注释，我无法确定是否和**AVInputFormat**一致，该函数也初始化**AVFormatContext**。

 - write_packet
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *,AVPacket  *pkt)|写入数据包

 - write_trailer
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *)|写入文件尾

 - interleave_packet
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *,AVPacket *out, AVPacket *in, int flush)|如果不是yuv420p则可以设置像素格式

 - query_codec 
    数据类型|含义
    -|-  
    int (*)(enum AVCodecID id, int std_compliance)|测试容器中能否存储给定的编解码器

 - get_output_timestamp 
    数据类型|含义
    -|-  
    void (*)(structAVFormatContext *s,  int stream,int64_t *dts,  int64_t*wall)|获取时间戳(存疑)

 - control_message 
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *s,int  type, void *data, size_tdata_size)|允许程序向设备发送信息

 - write_uncoded_frame 
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *, int stream_index,AVFrame **frame,unsigned  flags)|写入未编码的AVFrame

 - get_device_list 
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *s,  struct AVDeviceInfoList *device_list)|获取设备列表

 - create_device_capabilities 
    数据类型|含义
    -|-  
    int (*)(structAVFormatContext *s, structAVDeviceCapabilitiesQuery *caps)|创建功能子模块

 - free_device_capabilities 
    数据类型|含义
    -|-  
    int (*)(structAVFormatContext *s,structAVDeviceCapabilitiesQuery *caps)|释放功能子模块

 -  data_codec
    数据类型|含义
    -|-  
    enum AVCodecID|默认的编解码器数据

 - init 
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *)|初始化Format，此方法可能分配数据，并且设置需要用到的AVFormatContext或AVStream

 - deinit 
    数据类型|含义
    -|-  
    void (*)(struct AVFormatContext *)|取消初始化格式

 - check_bitstream 
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *,const  AVPacket *pkt)|设置任何必要的位流过滤并提取全局头所需的任何其他数据。
### 实例
- adts
```
AVOutputFormat ff_adts_muxer = {
    .name              = "adts",
    .long_name         = NULL_IF_CONFIG_SMALL("ADTS AAC (Advanced Audio Coding)"),
    .mime_type         = "audio/aac",
    .extensions        = "aac,adts",
    .priv_data_size    = sizeof(ADTSContext),
    .audio_codec       = AV_CODEC_ID_AAC,
    .video_codec       = AV_CODEC_ID_NONE,
    .init              = adts_init,
    .write_header      = adts_write_header,
    .write_packet      = adts_write_packet,
    .write_trailer     = adts_write_trailer,
    .priv_class        = &adts_muxer_class,
    .flags             = AVFMT_NOTIMESTAMPS,
};
```
- h264
```
AVOutputFormat ff_h264_muxer = {
    .name              = "h264",
    .long_name         = NULL_IF_CONFIG_SMALL("raw H.264 video"),
    .extensions        = "h264,264",
    .audio_codec       = AV_CODEC_ID_NONE,
    .video_codec       = AV_CODEC_ID_H264,
    .write_header      = force_one_stream,
    .write_packet      = ff_raw_write_packet,
    .check_bitstream   = h264_check_bitstream,
    .flags             = AVFMT_NOTIMESTAMPS,
};
```
### 源码
```
typedef struct AVOutputFormat {
    const char *name;
    /**
     * Descriptive name for the format, meant to be more human-readable
     * than name. You should use the NULL_IF_CONFIG_SMALL() macro
     * to define it.
     */
    const char *long_name;
    const char *mime_type;
    const char *extensions; /**< comma-separated filename extensions */
    /* output support */
    enum AVCodecID audio_codec;    /**< default audio codec */
    enum AVCodecID video_codec;    /**< default video codec */
    enum AVCodecID subtitle_codec; /**< default subtitle codec */
    /**
     * can use flags: AVFMT_NOFILE, AVFMT_NEEDNUMBER,
     * AVFMT_GLOBALHEADER, AVFMT_NOTIMESTAMPS, AVFMT_VARIABLE_FPS,
     * AVFMT_NODIMENSIONS, AVFMT_NOSTREAMS, AVFMT_ALLOW_FLUSH,
     * AVFMT_TS_NONSTRICT, AVFMT_TS_NEGATIVE
     */
    int flags;

    /**
     * List of supported codec_id-codec_tag pairs, ordered by "better
     * choice first". The arrays are all terminated by AV_CODEC_ID_NONE.
     */
    const struct AVCodecTag *const *codec_tag;


    const AVClass *priv_class; ///< AVClass for the private context

    /*****************************************************************
     * No fields below this line are part of the public API. They
     * may not be used outside of libavformat and can be changed and
     * removed at will.
     * New public fields should be added right above.
     *****************************************************************
     */
    /**
     * The ff_const59 define is not part of the public API and will
     * be removed without further warning.
     */
#if FF_API_AVIOFORMAT
#define ff_const59
#else
#define ff_const59 const
#endif
    ff_const59 struct AVOutputFormat *next;
    /**
     * size of private data so that it can be allocated in the wrapper
     */
    int priv_data_size;

    int (*write_header)(struct AVFormatContext *);

    /**
     * Write a packet. If AVFMT_ALLOW_FLUSH is set in flags,
     * pkt can be NULL in order to flush data buffered in the muxer.
     * When flushing, return 0 if there still is more data to flush,
     * or 1 if everything was flushed and there is no more buffered
     * data.
     */
    int (*write_packet)(struct AVFormatContext *, AVPacket *pkt);

    int (*write_trailer)(struct AVFormatContext *);

    /**
     * Currently only used to set pixel format if not YUV420P.
     */
    int (*interleave_packet)(struct AVFormatContext *, AVPacket *out,
                             AVPacket *in, int flush);

    /**
     * Test if the given codec can be stored in this container.
     *
     * @return 1 if the codec is supported, 0 if it is not.
     *         A negative number if unknown.
     *         MKTAG('A', 'P', 'I', 'C') if the codec is only supported as AV_DISPOSITION_ATTACHED_PIC
     */
    int (*query_codec)(enum AVCodecID id, int std_compliance);

    void (*get_output_timestamp)(struct AVFormatContext *s, int stream,
                                 int64_t *dts, int64_t *wall);

    /**
     * Allows sending messages from application to device.
     */
    int (*control_message)(struct AVFormatContext *s, int type,
                           void *data, size_t data_size);

    /**
     * Write an uncoded AVFrame.
     *
     * See av_write_uncoded_frame() for details.
     *
     * The library will free *frame afterwards, but the muxer can prevent it
     * by setting the pointer to NULL.
     */
    int (*write_uncoded_frame)(struct AVFormatContext *, int stream_index,
                               AVFrame **frame, unsigned flags);

    /**
     * Returns device list with it properties.
     * @see avdevice_list_devices() for more details.
     */
    int (*get_device_list)(struct AVFormatContext *s, struct AVDeviceInfoList *device_list);

    /**
     * Initialize device capabilities submodule.
     * @see avdevice_capabilities_create() for more details.
     */
    int (*create_device_capabilities)(struct AVFormatContext *s,
                                      struct AVDeviceCapabilitiesQuery *caps);

    /**
     * Free device capabilities submodule.
     * @see avdevice_capabilities_free() for more details.
     */
    int
    (*free_device_capabilities)(struct AVFormatContext *s, struct AVDeviceCapabilitiesQuery *caps);

    enum AVCodecID data_codec; /**< default data codec */
    /**
     * Initialize format. May allocate data here, and set any AVFormatContext or
     * AVStream parameters that need to be set before packets are sent.
     * This method must not write output.
     *
     * Return 0 if streams were fully configured, 1 if not, negative AVERROR on failure
     *
     * Any allocations made here must be freed in deinit().
     */
    int (*init)(struct AVFormatContext *);

    /**
     * Deinitialize format. If present, this is called whenever the muxer is being
     * destroyed, regardless of whether or not the header has been written.
     *
     * If a trailer is being written, this is called after write_trailer().
     *
     * This is called if init() fails as well.
     */
    void (*deinit)(struct AVFormatContext *);

    /**
     * Set up any necessary bitstream filtering and extract any extra data needed
     * for the global header.
     * Return 0 if more packets from this stream must be checked; 1 if not.
     */
    int (*check_bitstream)(struct AVFormatContext *, const AVPacket *pkt);
} AVOutputFormat;
```