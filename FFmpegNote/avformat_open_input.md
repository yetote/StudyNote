# avformat_open_input
这个函数十分重要，无论是网络文件、本地文件、流媒体。解码第一步必定调用这个函数。
## 源码分析
本次分析基于FFmpeg4.2.1，该部分参考雷神的[博客](https://blog.csdn.net/leixiaohua1020/article/details/44064715)
### avformat_open_input
该函数位于libavformat/utils.c中,由于代码过长，所以源码在文末放出。这里我们将其拆分下，逐一分析。
```
    AVFormatContext *s = *ps;
    int i, ret = 0;
    AVDictionary *tmp = NULL;
    ID3v2ExtraMeta *id3v2_extra_meta = NULL;
```
这里进行了一些初始化操作，常规操作，没什么可分析的。
```
    //判断AVFormatContext是否成功分配
    if (!s && !(s = avformat_alloc_context()))
        return AVERROR(ENOMEM);
    if (!s->av_class) {
        return AVERROR(EINVAL);
    }
    if (fmt)
        s->iformat = fmt;

    if (options)
        av_dict_copy(&tmp, *options, 0);

    if (s->pb) // must be before any goto fail
        s->flags |= AVFMT_FLAG_CUSTOM_IO;

    if ((ret = av_opt_set_dict(s, &tmp)) < 0)
        goto fail;

    if (!(s->url = av_strdup(filename ? filename : ""))) {
        ret = AVERROR(ENOMEM);
        goto fail;
    }

#if FF_API_FORMAT_FILENAME
FF_DISABLE_DEPRECATION_WARNINGS
    av_strlcpy(s->filename, filename ? filename : "", sizeof(s->filename));
FF_ENABLE_DEPRECATION_WARNINGS
```
在这一部分是将