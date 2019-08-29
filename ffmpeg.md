### ffmpeg 命令
##### ffprobe

##### ffmpeg
 - 图片转换成视频
 ```
    ffmpeg -f image2 -i image%d.jpg video.mpg
 ```
 - 视频转换成图片
 ```
    ffmpeg -i video.mpg image%d.jpg
 ```
 ##### ffplay
 - 播放yuv
 ```
    ffplay -f rawvideo -video_size 1920x1080 a.yuv
 ```

##### 错误码

### ffmpeg编译
ndk:**r19b**  
ffmpeg:**4.1**  
os:**ubuntu(64位)** 
### ubuntu 支持https
- 1.ndk中删除了对openssl的支持，所以需要先安装openssl，然后将头文件放到ndk中
```
sudo apt-get install libssl-dev
```
之后在/usr/include下找到openssl文件夹，复制到编译脚本中sysroot所指定的目录中即可
- 2.提示opensslconf.h找不到的情况下，在/usr/include/x86_64-linux-gnu/openssl中找到opensslconf.h文件，并将其复制到ndk中的openssl中，这个错误的原因似乎是因为操作系统为数和openssl不一致  
- 3.undefined reference to `SSL_library_init' 

### 使用
- 初始化
    ```
        av_register_all();
        av_format_network();
    ```
- 打开文件
    ```
        
    ```
### 错误信息
- avformat_open_input 
    - 返回-5  
        **检查网络权限是否开启**
- avcodec_send_packet 
    - 返回-22  
        **参数非法，检查codecContex是否正确，确保正确后检查ffmpeg步骤是否正确**
    - 返回-11  
        **没有接受frame，检查avcodec_receive_frame是否正常运行**
### 异常信息
- 分配与释放
    结构体有结构体专用的释放方法，av_malloc用av_free释放，别瞎jb释放
- 视频花屏
    - 不应该在解码时做休眠，这会导致b帧无法正常解码，而应该在播放处做休眠