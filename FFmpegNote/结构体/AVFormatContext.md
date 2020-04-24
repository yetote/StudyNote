### AVFormatContext结构体
### 成员
 - name
    数据类型|含义
    -|-  
    const char *|名称 

 - av_class 
    数据类型|含义
    -|-  
    const AVClass *|AVClass 

 - iformat 
    数据类型|含义
    -|-  
    ff_const59 struct AVInputFormat *|解封装器格式 

 - *oformat 
    数据类型|含义
    -|-  
    ff_const59 struct AVOutputFormat |封装器格式 

 - priv_data 
    数据类型|含义
    -|-  
    void *|私有的format数据，当且仅当为iformat/oformats时，为一个AVOption结构体 

 - pb 
    数据类型|含义
    -|-  
    AVIOContext *|AVIOContext 

 - ctx_flags 
    数据类型|含义
    -|-  
    int|流属性的标记信号，为AVFMTCTX_XXX 

 - nb_streams 
    数据类型|含义
    -|-  
    unsigned int |AVFormatContext中流的数量 

 - streams 
    数据类型|含义
    -|-  
    AVStream **|文件中所有流的列表 

 - filename 
    数据类型|含义
    -|-  
    char[1024]|输入或输出文件名，被标记为过时。

 - url 
    数据类型|含义
    -|-  
    char *|输入或输出url，与filename不同，这里没有长度限制 

 -  start_time 
    数据类型|含义
    -|-  
    int64_t|组件第一帧的位置，以AV_TIME_BASE小数秒为单位 

 -  duration 
    数据类型|含义
    -|-  
    int64_t|流的时长 

 -  bit_rate 
    数据类型|含义
    -|-  
    int64_t|bit率 

 -  packet_size 
    数据类型|含义
    -|-  
    unsigned int|包的大小(待定) 

 -  max_delay 
    数据类型|含义
    -|-  
    int|最大延迟 

 -  flags 
    数据类型|含义
    -|-  
    int|封装/解封装器标志

    下方给出支持的flag  
    ```
    AVFMT_FLAG_GENPTS         
    AVFMT_FLAG_IGNIDX         
    AVFMT_FLAG_NONBLOCK       
    AVFMT_FLAG_IGNDTS         
    AVFMT_FLAG_NOFILLIN       
    AVFMT_FLAG_NOPARSE        
    AVFMT_FLAG_NOBUFFER       
    AVFMT_FLAG_CUSTOM_IO      
    AVFMT_FLAG_DISCARD_CORRUPT  
    AVFMT_FLAG_FLUSH_PACKETS  
    AVFMT_FLAG_BITEXACT       
    AVFMT_FLAG_MP4A_LATM      
    AVFMT_FLAG_SORT_DTS       
    AVFMT_FLAG_PRIV_OPT       
    AVFMT_FLAG_KEEP_SIDE_DATA   
    AVFMT_FLAG_FAST_SEEK      
    AVFMT_FLAG_SHORTEST       
    AVFMT_FLAG_AUTO_BSF 
    ```
    
 -  probesize 
    数据类型|含义
    -|-     
    int64_t|读取的最大数据大小，用于确定格式 

 -  max_analyze_duration 
    数据类型|含义
    -|-  
    int64_t|读取的数据最大时长 

 - key 
    数据类型|含义
    -|-  
    const uint8_t *|key 

 -  keylen 
    数据类型|含义
    -|-  
    int|key的长度 

 -  nb_programs 
    数据类型|含义
    -|-  
    unsigned int|-

 - programs 
    数据类型|含义
    -|-  
    AVProgram **|AVProgram 

 - video_codec_id，audio_codec_id，subtitle_codec_id
    数据类型|含义
    -|-  
    enum AVCodecID |对应的解码器id 

 - max_index_size 
    数据类型|含义
    -|-  
    unsigned int|每个流的最大内存 

 - max_picture_buffer 
    数据类型|含义
    -|-  
     unsigned int |帧的最大内存 

 -  nb_chapters 
    数据类型|含义
    -|-  
    unsigned int|-

 - chapters 
    数据类型|含义
    -|-  
    AVChapter **|- 

 -  metadata 
    数据类型|含义
    -|-  
    AVDictionary *|元数据 

 - start_time_realtime 
    数据类型|含义
    -|-  
    int64_t|这个流的生成时间 

 -  fps_probe_size 
    数据类型|含义
    -|-  
    int|用于确定avformat_find_stream_inf()中的帧率的帧数。 

 - error_recognition 
    数据类型|含义
    -|-  
    int|错误检测 

 - interrupt_callback 
    数据类型|含义
    -|-  
    AVIOInterruptCB|自定义的中断回调 

 - debug 
    数据类型|含义
    -|-  
    int|debug标记 

 - max_interleave_delta 
    数据类型|含义
    -|-  
    int64_t|交织的最大缓冲时长

 -  strict_std_compliance 
    数据类型|含义
    -|-  
    int|允许非标准和实验性扩展 

 -  event_flags 
    数据类型|含义
    -|-  
    int|供用户检测文件上发生的事件的标志 

 -  max_ts_probe 
    数据类型|含义
    -|-  
    int|等待第一个时间戳时要读取的最大数据包数 

 -  ts_id 
    数据类型|含义
    -|-  
    int|传输流ID 

 -  audio_preload 
    数据类型|含义
    -|-  
    int|音频预加载 

 -  max_chunk_duration 
    数据类型|含义
    -|-  
    int|最大块时间 

 -  max_chunk_size 
    数据类型|含义
    -|-  
    int|最大块大小 

 -  use_wallclock_as_timestamps 
    数据类型|含义
    -|-  
    int|强制使用wallclock时间戳作为数据包的pts / dts 

 -  avio_flags 
    数据类型|含义
    -|-  
    int|avio flag 

 -  duration_estimation_method 
    数据类型|含义
    -|-  
    enum AVDurationEstimationMethod|时长字段可以通过多种方式估算，并且该字段可用于了解时长的估算方式 

 -  skip_initial_bytes 
    数据类型|含义
    -|-  
    int64_t|打开流时跳过初始字节 

 - correct_ts_overflow 
    数据类型|含义
    -|-  
    unsigned int|纠正单个时间戳溢出 

 -  seek2any 
    数据类型|含义
    -|-  
    int|强制跳转到任意帧 

 -  flush_packets 
    数据类型|含义
    -|-  
    int|每个Packet后刷新io context 

 -  probe_score 
    数据类型|含义
    -|-  
    int|格式探测得分 

 -  format_probesize 
    数据类型|含义
    -|-  
    int|读取的用于确定格式的最大字节数

 - codec_whitelist 
    数据类型|含义
    -|-  
    char *|允许的解码器列表 

 - format_whitelist 
    数据类型|含义
    -|-  
    char *|允许的解封装器列表 

 - internal 
    数据类型|含义
    -|-  
    AVFormatInternal *|libavformat内部的私有字段 

 -  io_repositioned 
    数据类型|含义
    -|-  
    int|io更改标志 

 - video_codec，audio_codec，subtitle_codec，data_codec
    数据类型|含义
    -|-  
    AVCodec *|强制使用的解码器 

 -  metadata_header_padding 
    数据类型|含义
    -|-  
    int|元数据头中的padding值 

 - opaque 
    数据类型|含义
    -|-  
    void *|用户信息(私有数据) 

 -  control_message_cb 
    数据类型|含义
    -|-  
    av_format_control_message|设备和应用之间通信的回调函数 

 -  output_ts_offset 
    数据类型|含义
    -|-  
    int64_t|输出时间戳偏移 

 - dump_separator 
    数据类型|含义
    -|-  
    uint8_t *|转储格式分隔符 

 -  data_codec_id 
    数据类型|含义
    -|-  
    enum AVCodecID|强制的codecid 

 - open_cb 
    数据类型|含义
    -|-  
     int (*)(struct AVFormatContext *s, AVIOContext **p, const char *url, int flags,const AVIOInterruptCB *int_cb, AVDictionary **options)|解封装时需要打开的其他io上下文，已标志过时 

 - protocol_whitelist 
    数据类型|含义
    -|-  
    char *|用“,”分割的允许的协议列表 

 - io_open 
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *s, AVIOContext **pb, const char *url,int flags, AVDictionary **options)|用于打开新的io流的回调 

 - io_close
    数据类型|含义
    -|-  
    void (*)(struct AVFormatContext *s, AVIOContext *pb)|关闭 io_open打开的io流

 - protocol_blacklist 
    数据类型|含义
    -|-  
    char *|用“,”分割的禁用的协议列表  

 -  max_streams 
    数据类型|含义
    -|-  
    int|流的最大数量 

 -  skip_estimate_duration_from_pts 
    数据类型|含义
    -|-  
    int|跳过时长用estimate_timings_from_pts计算 