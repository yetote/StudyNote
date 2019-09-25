### Idle状态
当MediaPlayer被创建或者调用reset函数时，这时候播放器处于idle（就绪）状态
### End状态
当调用release函数时，播放器就会被销毁，这时候进入End状态，在idle和end状态之间，就是MediaPlayer的生命周期
### Error状态
当MediaPlayer出错是，会回调用户设置的OnErrorListener.OnError方法，就会进入到Error状态，这时为了重新启动MediaPlayer，就需要调用reset函数,这时候就会进入到idle状态
### Initialized状态
当调用setDataSource函数时,Mediaplayer就会从idle状态变为initialized状态
### Prepared状态
Mediaplayer有两种方式到达prepared状态,一种同步(播放本地资源)一种异步(播放网络资源),调用prepared(同步)函数时,mediaplayer会从initialized状态进入到prepared状态,当调用prepareAsync(异步)时,会先进入到preparing,然后进入到(回调(OnPreparedListener)prepared状态,进入prepared状态后,可以设置一些相关的函数(屏幕常亮、音量调整)
### Start状态
当Mediaplayer处prepared状态后,就可以调用start函数开始播放了,这时进入start状态,如果注册过OnBufferUpdateListener函数的话,就会回调该函数,用来跟踪音视频流数据,如果处于started状态后再次调用start函数没有任何作用
### Pause状态
当mediaplayer进入start状态后,可以通过pause函数暂停播放,从started到pasue是瞬间的,但是反过来的话,却需要一定的耗时
### Stop状态
当调用stop函数时,无论Mediaplayer处于什么状态,都会进入到stoped状态,这时就可以调用release进行销毁了,当处于stoped状态时,只有通过prepared状态重新启用mediaplayer
### PlayerCompleate状态
未设置setlooping()函数时,当播放完成后,Mediaplayer会回调OnCompletion.OnCompletion函数,表示播放完成,这时如果重新启用start函数时,播放器将重头播放该资源
### 从创建到setDisplay过程
通过GetService从ServiceManager获取对应的MediaPlayerService,然后调用native_setup函数创建播放器,接着通过setDataSource把url传入底层,当准备完成后,通过setDisplay传入surfaceholder,然后通过SurfaceHolder获取Surface展示视频数据
### 创建过程
当app层调用Mediaplayer.create函数时,进入创建过程  

```
public static MediaPlayer create(Context context, Uri uri,SurfaceHolder holder,AudioAttributes audioAttributes, int audioSessionId) {
       try {
         //初始化一个MediaPlayer
           MediaPlayer mp = new MediaPlayer();
          //设置音频相关属性,如果未传入,则new一个空的音频属性
           final AudioAttributes aa = audioAttributes != null ? audioAttributes : new AudioAttributes.Builder().build();
           //设置音频属性
           mp.setAudioAttributes(aa);
           //设置音频会话id(目前来看是用来关联音频播放设备的id)
           mp.setAudioSessionId(audioSessionId);
           //设置要播放的资源
           mp.setDataSource(context, uri);
           if (holder != null) {
             //可选,如果含有视频的话,会将holder传入到mediaplayer中
               mp.setDisplay(holder);
           }
           //进入准备状态
           mp.prepare();
           return mp;
       } catch (IOException ex) {
           Log.d(TAG, "create failed:", ex);
           // fall through
       } catch (IllegalArgumentException ex) {
           Log.d(TAG, "create failed:", ex);
           // fall through
       } catch (SecurityException ex) {
           Log.d(TAG, "create failed:", ex);
           // fall through
       }

       return null;
   }
```
当通过create创建mediaplayer时,会new一个mediaplayer并设置好相关属性与uri,然后调用prepare方法,最后返回该mediaplayer,所以在外部只需要调用start方法,就可以播放资源了
#### 构造方法
无论是使用new还是create创建MediaPlayer,都需要走构造方法
```
public MediaPlayer() {
        super(new AudioAttributes.Builder().build(),AudioPlaybackConfiguration.PLAYER_TYPE_JAM_MEDIAPLAYER);

        //声明一个Looper
        Looper looper;

        if ((looper = Looper.myLooper()) != null) {
          // 如果myLooper不为空的话,就新建一个EventHanlder
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
          //或者将主线程的looper赋值给这个looper
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }

        mTimeProvider = new TimeProvider(this);
        mOpenSubtitleSources = new Vector<InputStream>();

        native_setup(new WeakReference<MediaPlayer>(this));

        baseRegisterPlayer();
    }

```
