### 入口方法
```
ijkMediaPlayer.setDataSource(path);
```
### 分析
#### Java
```
@Override
public void setDataSource(String path)throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
    mDataSource = path;
     _setDataSource(path, null, null);
}
```
又是一个**native**方法，只传递了资源路径，剩下的参数都是**null**。
#### Native
查询**jni**映射，得知对应的方法为```IjkMediaPlayer_setDataSourceCallback```
- IjkMediaPlayer_setDataSourceCallback  
该文件在**ijkmedia/ijkplayer/android/ijkplayer_jni.c**中
```
static void
IjkMediaPlayer_setDataSourceCallback(JNIEnv *env, jobject thiz, jobject callback) {
    char uri[128];
    int64_t nativeMediaDataSource = 0;
    IjkMediaPlayer *mp = jni_get_media_player(env, thiz);
    nativeMediaDataSource = jni_set_media_data_source(env, thiz, callback);
    snprintf(uri, sizeof(uri), "ijkmediadatasource:%"
    PRId64, nativeMediaDataSource);
    retval = ijkmp_set_data_source(mp, uri);
}
```
去除掉无用的代码后，首先会调用```jni_get_media_player```  
- jni_get_media_player  
    该方法在同一文件下
    ```
    static IjkMediaPlayer *jni_get_media_player(JNIEnv *env, jobject thiz) {
        IjkMediaPlayer *mp = (IjkMediaPlayer *) (intptr_t) J4AC_IjkMediaPlayer__mNativeMediaPlayer__get__catchAll(env, thiz);
        if (mp) {
            ijkmp_inc_ref(mp);
        }
        return mp;
    }
    ```
```J4AC_IjkMediaPlayer__mNativeMediaPlayer__get__catchAll```这个方法用来获取**java**层的**ijkplayer**的，ijkmp_inc_ref则是将引用计数+1的。也就是说这个方法用于获取**java**层的**ijkplayer**。  
接下来会去调用```jni_set_media_data_source```方法，该方法也在**ijkplayer_jni**中。  
- jni_set_media_data_source
    ```
    static int64_t jni_set_media_data_source(JNIEnv *env, jobject thiz, jobject media_data_source) {
        int64_t nativeMediaDataSource = 0;

        jobject old = (jobject)(intptr_t)J4AC_IjkMediaPlayer__mNativeMediaDataSource__get__catchAll(env, thiz);
        if (old) {
            J4AC_IMediaDataSource__close__catchAll(env, old);
            J4A_DeleteGlobalRef__p(env, &old);
            J4AC_IjkMediaPlayer__mNativeMediaDataSource__set__catchAll(env, thiz, 0);
        }

        if (media_data_source) {
            jobject global_media_data_source = (*env)->NewGlobalRef(env, media_data_source);
            
            nativeMediaDataSource = (int64_t)(intptr_t)global_media_data_source;
            J4AC_IjkMediaPlayer__mNativeMediaDataSource__set__catchAll(env, thiz,(jlong) nativeMediaDataSource);
        }
    }
    ```
先获取到旧的**dataSource(java层的IjkMediaPlayer.mNativeMediaDataSource)**，然后进行清空操作，然后设置新得**dataSource**。  
接下来就是```ijkmp_set_data_source```方法了。
- ijkmp_set_data_source  
该方法定义在**ijkplayer.c**中。
```
int ijkmp_set_data_source(IjkMediaPlayer *mp,const char *url) {
    int retval = ijkmp_set_data_source_l(mp, url);
    return retval;
}
```
这里调用了```ijkmp_set_data_source_l```这个方法
- ijkmp_set_data_source_l  
该方法定义在**ijkplayer.c**中。
```
static int ijkmp_set_data_source(IjkMediaPlayer *mp, const char *url) {
    //没搞懂这段代码，看起来应该是检查状态的
    // MPST_RET_IF_EQ(mp->mp_stateMP_STATE_IDLE);
    MPST_RET_IF_EQ(mp->mp_stateMP_STATE_INITIALIZED);
    MPST_RET_IF_EQ(mp->mp_stateMP_STATE_ASYNC_PREPARING);
    MPST_RET_IF_EQ(mp->mp_stateMP_STATE_PREPARED);
    MPST_RET_IF_EQ(mp->mp_stateMP_STATE_STARTED);
    MPST_RET_IF_EQ(mp->mp_stateMP_STATE_PAUSED);
    MPST_RET_IF_EQ(mp->mp_stateMP_STATE_COMPLETED);
    MPST_RET_IF_EQ(mp->mp_stateMP_STATE_STOPPED);
    MPST_RET_IF_EQ(mp->mp_stateMP_STATE_ERROR);
    MPST_RET_IF_EQ(mp->mp_state, MP_STATE_END)
    freep((void **) &mp->data_source);
    mp->data_source = strdup(url);
    ijkmp_change_state_l(mpMP_STATE_INITIALIZED);
    return 0;
}
```
那些调用宏定义的代码没搞清楚是什么含义，猜测是用来检测状态的，```freep```看起来是释放指针的。接下来将**mp**的```data_source```的指针指向到**url**，接下来调用```ijkmp_change_state_l```
- ijkmp_change_state_l  
该方法定义在**ijkplayer.c**中
```
void ijkmp_change_state_l(IjkMediaPlay*mp, int new_state) {
    mp->mp_state = new_state;
    ffp_notify_msg1(mp->ffplayeFFP_MSG_PLAYBACK_STATE_CHANGED);
}
```
传递进来的**state**是**MP_STATE_INITIALIZED**，意义为初始化状态。接着调用```ffp_notify_msg1```  
- ffp_notify_msg1  
该函数定义在**ijkmedia/ijkplayer/ff_ffplay_def.h**中
```
inline static void ffp_notify_(FFPlayer *ffp, int what{
    msg_queue_put_simplffp->msg_queue, what, 0, 0);
}
```
该方法为一个内联方法，```msg_queue_put_simple3```作用是把```what(FFP_MSG_PLAYBACK_STATE_CHANGED)```放入到```ffp->msg_queue```队列中
这里我们回到```IjkMediaPlayer_setDataSourceCallback```方法中，发现```IjkMediaPlayer_setDataSourceCallback```已全部执行完毕
#### 总结
**ijkmediaplayer**的**setDataSource**相比较初始化流程较短，主要执行了重设**java**的**mNativeMediaDataSource**，然后将**url**赋予给**native**的**mp**中的**data_source**，然后调整**mp**的状态为**MP_STATE_INITIALIZED**，最后通过**ffplayer**的**msg_queue**把**FFP_MSG_PLAYBACK_STATE_CHANGED**状态传递出去

### 结构
```
--->ijkmedia/ijkplayer/android/ijkplayer_jni.c/IjkMediaPlayer_setDataSourceCallback
------->ijkmedia/ijkplayer/android/ijkplayer_jni.c/jni_get_media_player
------->ijkmedia/ijkplayer/android/ijkplayer_jni.c/jni_set_media_data_source
----------->ijkmedia/ijkplayer/ijkplayer.c/ijkmp_set_data_source
--------------->ijkmedia/ijkplayer/ijkplayer.c/ijkmp_set_data_source_l
------------------->ijkmedia/ijkplayer/ijkplayer.c/ijkmp_change_state_l
----------------------->ijkmedia/ijkplayer/ijkplayer.c/ffp_notify_msg1
--------------------------->ijkmedia/ijkplayer/ff_ffplay_def.h
```