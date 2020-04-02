# av_register_all
该函数在4.x中被删除，所以此部分源码分析基于3.2.1。
## 源码分析
该部分参考雷神的[博客](https://blog.csdn.net/leixiaohua1020/article/details/12677129)

### 函数声明
该函数在libavformat/allformats.c中
```
void av_register_all(void)
{
    static AVOnce control = AV_ONCE_INIT;

    ff_thread_once(&control, register_all);
}
```
```ff_thread_once```表示只执行一次，具体代码这里不分析。
我们接下来看下```register_all```函数  
### register_all  
该函数在libavformat/allformats.c中
```
static void register_all(void)
{
    avcodec_register_all();

    REGISTER_MUXER   (ADTS,             adts);
    REGISTER_DEMUXER (AAC,              aac);
    REGISTER_MUXDEMUX(MP3,              mp3);
    ...
}
```
这里我们看到了```register_all```也会调用```avcodec_register_all```去注册编解码器。这里先暂时搁置。这里先看下 封装/解封装(复用/解复用)器的注册
这里我们看到**ffmpeg**将封装/解封装器分为三部分    
宏|作用|调用函数
-|-|-
MUXER| 封装器|```av_register_output_format```
DEMUXER |解封装器|```av_register_input_format```
MUXDEMUX| 封装器和解封装器|  ```DEMUXER和MUXER```
放上代码
```
#define REGISTER_MUXER(X, x)                                            \
    {                                                                   \
        extern AVOutputFormat ff_##x##_muxer;                           \
        if (CONFIG_##X##_MUXER)                                         \
            av_register_output_format(&ff_##x##_muxer);                 \
    }

#define REGISTER_DEMUXER(X, x)                                          \
    {                                                                   \
        extern AVInputFormat ff_##x##_demuxer;                          \
        if (CONFIG_##X##_DEMUXER)                                       \
            av_register_input_format(&ff_##x##_demuxer);                \
    }

#define REGISTER_MUXDEMUX(X, x) REGISTER_MUXER(X, x); REGISTER_DEMUXER(X, x)
```
#### av_register_output_format
该函数定义在**libavformat/format.c**中
```
/** head of registered input format linked list */
static AVInputFormat *first_iformat = NULL;
/** head of registered output format linked list */
static AVOutputFormat *first_oformat = NULL;

static AVInputFormat **last_iformat = &first_iformat;
static AVOutputFormat **last_oformat = &first_oformat;

void av_register_input_format(AVInputFormat *format)
{
    AVInputFormat **p = last_iformat;

    // Note, format could be added after the first 2 checks but that implies that *p is no longer NULL
    while(p != &format->next && !format->next && avpriv_atomic_ptr_cas((void * volatile *)p, NULL, format))
        p = &(*p)->next;

    if (!format->next)
        last_iformat = &format->next;
}
```
我们不难看出**last_iformat**是链表的尾节点。**format->next**有两种情况有值和**NULL**，前两个条件分别判断这两种情况，而最后一个条件则是一个函数。
##### avpriv_atomic_ptr_cas
该函数定义在**libavutil/atomic.c**中
```
//Atomic pointer compare and swap
void *avpriv_atomic_ptr_cas(void * volatile *ptr, void *oldval, void *newval)
{
    if (*ptr == oldval) {
        *ptr = newval;
        return oldval;
    }
    return *ptr;
}

```
从注释以及代码看出该函数用于交换**ptr**所指向的值。
我们返回到**while**循环处结合情景分析出```avpriv_atomic_ptr_cas```是当**last_iforma**指向**NULL**时，将**last_iformat**指向**format**。
也就是将传递进的**format**添加到所对应的的链表尾部，这就是```av_register_input_format```的作用。

#### av_register_input_format
与```av_register_output_format```作用类似。这里不再赘述。
### avcodec_register_all
我们分析完了注册封装/解封装器流程后，来看下注册解码器的流程
该函数定义在**libavcodec/allcodecs.c**中
```
void avcodec_register_all(void)
{
    static AVOnce control = AV_ONCE_INIT;

    ff_thread_once(&control, register_all);
}
```
有点熟悉。
#### register_all
**register_all**内部调用了这5个宏，分别为  
宏|作用|调用函数
-|-|-
REGISTER_HWACCE | 硬件加速|```av_register_hwaccel```
REGISTER_ENCODE | 编码器|```avcodec_register```
REGISTER_DECODE | 解码器|```avcodec_register```
REGISTER_ENCDEC | 编码器和解码器|```REGISTER_DECODE```和```REGISTER_ENCODE```
REGISTER_PARSER | 解析器|```av_register_codec_parser```
我们看到编码器解码器都调用了```avcodec_register```，区别在于传递的参数不同。
宏|函数|参数
-|-|-
REGISTER_ENCODE|avcodec_register|ff_##x##_encoder
REGISTER_DECODE|avcodec_register|ff_##x##_decoder
所以我们要分析的也就是这三个函数。

##### avcodec_register
函数定义在**libavcodec/util.c**中
```
av_cold void avcodec_register(AVCodec *codec)
{
    AVCodec **p;
    avcodec_init();
    p = last_avcodec;
    codec->next = NULL;

    while(*p || avpriv_atomic_ptr_cas((void * volatile *)p, NULL, codec))
        p = &(*p)->next;
    last_avcodec = &codec->next;

    if (codec->init_static_data)
        codec->init_static_data(codec);
}
```
```avcodec_init```主要是用于检测是否只执行一次。
和注册封装器(解封装器)类似，也是将**codec**放入链尾。```init_static_data```则是调用具体的编解码器的函数指针，这个我们放到**AVCodec**结构体中再去细说。
##### av_register_hwaccel
```
void av_register_hwaccel(AVHWAccel *hwaccel)
{
    AVHWAccel **p = last_hwaccel;
    hwaccel->next = NULL;
    while(*p || avpriv_atomic_ptr_cas((void * volatile *)p, NULL, hwaccel))
        p = &(*p)->next;
    last_hwaccel = &hwaccel->next;
}
```
代码类似，不赘述了。
##### av_register_codec_parser
```
void av_register_codec_parser(AVCodecParser *parser)
{
    do {
        parser->next = av_first_parser;
    } while (parser->next != avpriv_atomic_ptr_cas((void * volatile *)&av_first_parser, parser->next, parser));
}
```
我怀疑是我版本搞混了，之前都是从尾节点判断，现在改成从头结点遍历。。。
### 小结
至此，```av_register_all```函数我们全部分析完毕。
我们忽略掉```ff_thread_once```，发现```av_register_all```名副其实，分别注册了封装器(解封装器)、硬件加速模块、编码器(解码器)这五部分。具体的注册方式为将对应的组件添加到对应的链表尾部。
### 疑问
4.x删除掉av_register_all```后，这些组件都在那里调用呢？
组件|调用
-|-
muxer|？
demuxer|？
hwacce|？
encode|？
decode|？
parser|？
