## AVFormatContext
### AVOption
- class_name    
    数据类型|含义
    -|-  
    const char *|名称
- help
    数据类型|含义
    -|-  
    const char *|帮助
- offset
    数据类型|含义
    -|-
    int|相对于首项的偏移量
- type  
    数据类型|含义
    -|-
    enum AVOptionType|Option类型
- default_val
```
    union {
        int64_t i64;
        double dbl;
        const char *str;
        /* TODO those are unused now */
        AVRational q;
    } default_val;

```
该变量的数据类型为共用体，支持int64_t、double、字符串、分数。
- min、max
    数据类型|含义
    -|-
    double|选项的最大值和最小值
- flags
    数据类型|含义
    -|-
    int|标志
- unit;
    数据类型|含义
    -|-
    const char *|选项所属的逻辑单元
##### 使用
结合下具体的代码理解起来就比较清晰。
```
#define OFFSET(x) offsetof(AVFormatContext,x)

#define E AV_OPT_FLAG_ENCODING_PARAM
#define D AV_OPT_FLAG_DECODING_PARAM

static const AVOption avformat_options[] = {
{"avioflags", NULL, OFFSET(avio_flags), AV_OPT_TYPE_FLAGS, {.i64 = DEFAULT }, INT_MIN, INT_MAX, D|E, "avioflags"},
{"direct", "reduce buffering", 0, AV_OPT_TYPE_CONST, {.i64 = AVIO_FLAG_DIRECT }, INT_MIN, INT_MAX, D|E, "avioflags"},
...
}

```
### AVOptionRange
- str    
    数据类型|含义
    -|-  
    const char *|名称
- value_min/value_max    
    数据类型|含义
    -|-  
    double|取值范围。对于字符串范围，这表示最小/最大长度。对于尺寸，这表示在多组件情况下的最小/最大像素数或宽度/高度。
- component_min/component_max    
    数据类型|含义
    -|-  
    double|值的组成范围。对于字符串，它表示char的unicode范围，ASCII的范围为0-127。
- is_range    
    数据类型|含义
    -|-  
    int|范围标志，如果设置为1，则该结构编码一个范围；如果设置为0，则为单个值。
### AVOptionRanges
- range    
    数据类型|含义
    -|-  
    AVOptionRange **|选项范围数组
- nb_ranges    
    数据类型|含义
    -|-  
    int|组件范围数量
- is_range    
    数据类型|含义
    -|-  
    int|每个组件的范围。
### AVClass 
- class_name    
    数据类型|含义
    -|-  
    const char *|名称
- item_name
    数据类型|含义
    -|-  
    ```const char* (*)(void*)```|一个函数指针，返回与该实例关联的ctx的名称
- option
    数据类型|含义
    -|-  
    const struct AVOption *|配置选项数组
- version
    数据类型|含义
    -|-  
    int|版本号
- log_level_offset_offset
    数据类型|含义
    -|-  
    int|结构体中log_level_offset的偏移量
- parent_log_context_offset
    数据类型|含义
    -|-  
    int|父context中用于日志记录的指针的偏移量
- child_next
    数据类型|含义
    -|-  
    void* (*)(void *obj, void *prev)|返回下一个支持AVOption的子对象
- child_class_next  

    数据类型|含义
    -|-  
    const struct AVClass* (*)(const struct AVClass *prev)|返回与下一个可能启用AVOptions的子级对应的AVClass。
与child_next不同的是，child_next遍历现有对象，而child_class_next遍历所有可能的子对象。
- category  

    数据类型|含义
    -|-  
    AVClassCategory|返回与下一个可能启用AVOptions的子级对应的AVClass。
- query_ranges  
    数据类型|含义
    -|-  
    int (*)(struct AVOptionRanges **, void *, const char *, int )|返回配置项所允许的范围

