### 初始化
从java层实例化ijkplayer之后,会调用set_up方法,在该方法中,会转到ijkplayer.c中的ijkmp_create方法,通过malloc为IjkMediaPlayer分配空间,在该方法中,也去进行了ffplay的create方法,并且设置了