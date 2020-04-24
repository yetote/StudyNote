### AVFormatInternal
 - nb_interleaved_streams;
    数据类型|含义
    -|-  
    int|与交叉相关的流数 

 - packet_buffer，packet_buffer_end
    数据类型|含义
    -|-  
    struct AVPacketList *|这个链表用于存储以缓冲但未解码的packet，例如获取流的解码器参数 

    AVPacketList是一个AVPacket链表。

 - data_offset;
    数据类型|含义
    -|-  
    int64_t|第一个数据包的偏移量 

 - raw_packet_buffer，raw_packet_buffer_end
    数据类型|含义
    -|-  
    struct AVPacketList *|缓冲区用于存储解封装后的数据，主要用于读取编解码器。 

 - parse_queue，parse_queue_end
    数据类型|含义
    -|-  
    struct AVPacketList *|该链表用于存储解析器拆分出来的数据包。

 - raw_packet_buffer_remaining_size;
    数据类型|含义
    -|-  
    int|raw_packet_buffer可用的数据大小 

 - offset;
    数据类型|含义
    -|-  
    int64_t|偏移重新映射时间戳 

 - offset_timebase;
    数据类型|含义
    -|-  
    AVRational|时间戳偏移量的timebase 

 - missing_ts_warning;
    数据类型|含义
    -|-  
    int|- 

 - inject_global_side_data;
    数据类型|含义
    -|-  
    int|- 

 - int avoid_negative_ts_use_pts;
    数据类型|含义
    -|-  
    int|-

 - shortest_end;
    数据类型|含义
    -|-  
    int64_t|最短流结束的时间戳 

 - initialized;
    数据类型|含义
    -|-  
    int|是否已调用avformat_init_output 

 - streams_initialized;
    数据类型|含义
    -|-  
    int|avformat_init_output是否完全初始化流 

 - id3v2_meta;
    数据类型|含义
    -|-  
    AVDictionary *|MP3解封装的ID3V2标签 

 - prefer_codec_framerate
    数据类型|含义
    -|-  
    int|codec帧率用于avg_frame_rate计算 
### 总结
AVFormatInternal是在ffmpeg内部中使用的结构体，其中有一些意义不明显，不过根据包含的AVPacketList猜测该结构体用于探测解码器以及拆分数据包。