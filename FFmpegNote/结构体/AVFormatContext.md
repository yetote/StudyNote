### AVFormatContext结构体
### 成员
#### AVInputFormat
在**AVFormatContext**中的注释提到**AVInputFormat**仅用于解封装器，在```avformat_open_input```时配置

- name
    数据类型|含义
    -|-  
    const char *|AVClass
   
- long_name
    数据类型|含义
    -|-  
    const char *|AVClass
    
-  flags
    数据类型|含义
    -|-  
    int|AVClass
    
- extensions
    数据类型|含义
    -|-  
    const char *|AVClass

- codec_tag;
    数据类型|含义
    -|-  
    const struct AVCodecTag *|AVClass

- priv_class
    数据类型|含义
    -|-  
    const AVClass *|AVClass
   
- mime_type
    数据类型|含义
    -|-  
    const char *|AVClass
    
- next
    数据类型|含义
    -|-  
    ff_const59 struct AVInputFormat *|AVClass

-  raw_codec_id
    数据类型|含义
    -|-  
    int|AVClass
    
-  priv_data_size
    数据类型|含义
    -|-  
    int|AVClass
    
-  read_probe
    数据类型|含义
    -|-  
    int (*)(const AVProbeData *)|AVClass
   
- read_header
    数据类型|含义
    -|-  
     int (*)(struct AVFormatContext *)|AVClass
    
- read_packet
    数据类型|含义
    -|-  
    int (*read_packet)(struct AVFormatContext *, AVPacket *pkt)|AVClass
   
- read_close
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *)|AVClass
    
- read_seek
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *, int stream_index, int64_t timestamp, int flags)|AVClass
    
- read_timestamp
    数据类型|含义
    -|-  
    int64_t (*)(struct AVFormatContext *s, int stream_index,
                              int64_t *pos, int64_t pos_limit)|AVClass
    
- read_play
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *)|AVClass
    
- read_pause
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *)|AVClass
    
- read_seek2
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *s, int stream_index, int64_t min_ts, int64_t ts, int64_t max_ts, int flags)|AVClass
   
- get_device_list
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *s, struct AVDeviceInfoList *device_list)|AVClass
    
- create_device_capabilities
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *s, struct AVDeviceCapabilitiesQuery *caps)|AVClass
    
- free_device_capabilities
    数据类型|含义
    -|-  
    int (*)(struct AVFormatContext *s, struct AVDeviceCapabilitiesQuery *caps)|AVClass

