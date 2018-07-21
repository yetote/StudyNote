### 简介
**AudioRecord**和**AudioTrack**是google提供的音频录制与播放的api，相比MediaRecorder，AudioRecord更接近与底层，我们可以进行更多的操作，但是缺点也很明显，AduioRecord保存的是pcm数据，只能通过AudioTrack进行播放，而MediaRecorder使用起来更加的方便快捷。这里我们主要介绍AudioRecord和AudioTrack的使用。
#### AudioRecord
要使用AudioRecord我们首先要申请**RECORD_AUDIO**权限，这里不再对权限申请进行赘述。
接下来我们要声明一些录制过程中用到的常量，分别为**采样率**，**声道设置**，**采样数据格式**，还有音频**最小缓冲区大小**  
 - **采样率**  
指的是音频每秒钟采样次数，采样率越高，音质越高，有8000，16000，22050,44100几种，单位为Hz，这里我们使用最常用的44100
 - **声道设置**  
是指单声道还是双声道立体声，我们这里使用立体声
 - **采样数据格式**    
  有16bit和8bit，但是8bit现已不支持，只有16bit可选
 - **最小缓冲区大小**  
 这个要通过计算得来的，
但是google提供了一个apiAudioRecord.getMinBufferSize可以帮助我们计算出该数值，要注意，通常我们设置最小缓冲区为计算出来的**2~10**倍，否则会出现一些莫名其妙的错误参数说明请查找本文的api部分
代码如下
```
private final int SAMPLE_RATE = 44100;
private final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
private final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
```
这样我们准备工作做完了，接下来我们进行音频的采集
首先我们初始化**AudioRecord**，
```
audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);
 ```
该方法共需要5个参数，我们介绍了后四个，下面我们来说下第一个参数，改参数表示需要采集的声音的来源，MIC则表示有麦克风进行数据的采集
初始化完成之后呢，我们就可以进行音频的采集了,就一行代码
```
audioRecord.read(temp, 0, bufferSize);
```
返回值表示**采集的音频的大小**，要注意我们在进行采集的时候为了性能的考虑通常将采集过程放到**子线程**中去执行，而且采集是一个持续的过程，所以我们要通过**while**不断地**循环**这个采集的过程
##### 完整代码
```
class AudioRecordThread extends Thread {
        @Override
        public void run() {
            super.run();
            byte[] temp = new byte[bufferSize];
            while (isRecording) {
                int length = audioRecord.read(temp, 0, bufferSize);
                Log.e(TAG, "startRecording: " + "录音成功，正在写入。。。。" + length);
                writePCM.writeData(temp);
            }
        }
    }
```
```
WritePcm.java
```
```
public class WritePCM {
    private ByteBuffer buffer;
    private FileOutputStream outputStream;
    private FileChannel channel;
    private int size;
    private String path;
    private static final String TAG = "WritePCM";

public WritePCM(int size, String path) {
        this.size = size;
        this.path = path;
        buffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        File file = new File(path);
        try {
            if (!file.exists()) {
                file.getParentFile().mkdir();
                file.createNewFile();
            }
            outputStream = new FileOutputStream(file, true);
            channel = outputStream.getChannel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

public void writeData(byte[] data) {
    if (data.length == 0) {
            Log.e(TAG, "writeData: " + "数据为空");
            return;
    }
    if (channel == null || outputStream == null) {
    Log.e(TAG, "writeData: " + "初始化失败");
            return;
        }
        buffer.put(data);
        buffer.flip();
        try {
            channel.write(buffer);
            Log.e(TAG, "writeData: " + "写入成功");
            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

public void close() {
    buffer.clear();
    try {
        channel.close();
        outputStream.close();
        Log.e(TAG, "close: " + "关闭成功");
    } catch (IOException e) {
        Log.e(TAG, "close: " + "关闭失败");
        e.printStackTrace();
        }
    }
}
```
当采集工作完成后，我们需要**关闭AudioRecord**，同时我们也将**io流进行关闭**
```
isRecording = false;
audioRecord.stop();
audioRecord.release();    
writePCM.close();
```
#### AudioTrack
用来播放pcm音频
在Android M的时候，之前的方法被弃用，我们需要使用新的方法来进行初始化
```
 audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
                .setAudioFormat(new AudioFormat.Builder()
                .setEncoding(AUDIO_FORMAT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(CHANNEL_CONFIG)
            .build())
            .setBufferSizeInBytes(bufferSize).build();
 ```
乍一看挺繁琐的，但是其实就是配置一些参数而已。播放和采集过程一样，也需要在**子线程**中进行，也需要不断地**循环**
```
byte[] temp = readPCM.readData();
audioTrack.write(temp, 0, temp.length);
audioTrack.play();
```
播放完成后，我们需要**释放资源**
```
isPlaying = false;
audioTrack.stop();
audioTrack.release();
readPCM.close();
```
这样我们就完成了音视频的采集与播放任务
#### 全部代码
```
private void playPCM() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioTrack = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AUDIO_FORMAT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(CHANNEL_CONFIG)
                            .build())
                    .setBufferSizeInBytes(bufferSize)
                    .build();
        }
        new Thread(() -> {
            while (isPlaying) {
                byte[] temp = readPCM.readData();
                int length = audioTrack.write(temp, 0, temp.length);
                Log.e(TAG, "playPCM: "+length );
                audioTrack.play();
            }
        }).start();
    }
```

ReadPCM.java

```
public class ReadPCM {
    private ByteBuffer buffer;
    private FileInputStream inputStream;
    FileChannel channel;
    private String path;
    private int size;
    int count = 0;
    private static final String TAG = "ReadPCM";

    public ReadPCM(String path, int size) {
        this.path = path;
        this.size = size;
        buffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        try {
            inputStream = new FileInputStream(path);
            channel = inputStream.getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public byte[] readData() {
        byte[] data = new byte[size];
        try {
            channel.position(count);
            channel.read(buffer);
            count += size;
            Log.e(TAG, "readData: "+count );
            buffer.flip();
            for (int i = 0; i < size; i++) {
                data[i] = buffer.get();
            }
            buffer.clear();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        buffer.clear();
        try {
            channel.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

```
### API
#### AudioRecord
##### getMinBufferSize(int sampleRateInHz, int channelConfig, int audioFormat) 
获取系统设置的最小数据缓冲区，参数为**采样率**，**声道配置**，**采样数据格式**,返回值为**最小缓冲区大小**，注意我们在初始化的时候，要将这个值**扩大2-10倍**  
##### startRecording 
开启音频采集  
##### read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) 
收集音频数据，参数为要将数据填充到的数组，偏移量，长度，返回值为采集到的数据的长度  
##### stop() 
停止AudioRecord  
##### release()  
释放AudioRecord   
#### AudioTrack
##### write(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes)  
要播放的数据，参数为数据数组，起始位置，长度
##### play() 
播放
##### stop() 
停止AudioTrack
##### release() 
释放AudioTrack

全部代码
MainActivity.java
```
public class MainActivity extends AppCompatActivity {
    private Button start, stop, play;
    private AudioRecord audioRecord;
    private boolean isRecording;
    private int bufferSize;
    private final int SAMPLE_RATE = 44100;
    private final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    private final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int PERMISSION_RECORD_CODE = 1;
    private String path;
    private WritePCM writePCM;
    private static final String TAG = "MainActivity";
    private AudioTrack audioTrack;
    private ReadPCM readPCM;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        start.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_RECORD_CODE);
            } else {
                isRecording = true;
                startRecording();
            }
        });
        stop.setOnClickListener(v -> {
            if (isRecording) {
                isRecording = false;
                audioRecord.stop();
                audioRecord.release();
                writePCM.close();
            }
            if (isPlaying) {
                isPlaying = false;
                audioTrack.stop();
                audioTrack.release();
                readPCM.close();
            }
        });
        play.setOnClickListener(v -> {
            isPlaying = true;
            playPCM();
        });
    }

    private void playPCM() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioTrack = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AUDIO_FORMAT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(CHANNEL_CONFIG)
                            .build())
                    .setBufferSizeInBytes(bufferSize)
                    .build();
        }
        new Thread(() -> {
            while (isPlaying) {
                byte[] temp = readPCM.readData();
                int length = audioTrack.write(temp, 0, temp.length);
                Log.e(TAG, "playPCM: "+length );
                audioTrack.play();
            }
        }).start();
    }

    private void startRecording() {
        isRecording = true;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);
        audioRecord.startRecording();
        AudioRecordThread audioRecordThread = new AudioRecordThread();
        audioRecordThread.start();
    }

    private void init() {
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        play = findViewById(R.id.play);
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)*2;
        path = this.getExternalCacheDir().getPath() + "/test.pcm";
        writePCM = new WritePCM(bufferSize, path);
        readPCM = new ReadPCM(path, bufferSize);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_RECORD_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) startRecording();
                else Toast.makeText(this, "请开启录音权限", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    class AudioRecordThread extends Thread {
        @Override
        public void run() {
            super.run();
            byte[] temp = new byte[bufferSize];
            while (isRecording) {
                int length = audioRecord.read(temp, 0, bufferSize);
                Log.e(TAG, "startRecording: " + "录音成功，正在写入。。。。" + length);
                writePCM.writeData(temp);
            }
        }
    }


}
```
WritePCM.java
```
public class WritePCM {
    private ByteBuffer buffer;
    private FileOutputStream outputStream;
    private FileChannel channel;
    private int size;
    private String path;
    private static final String TAG = "WritePCM";

    public WritePCM(int size, String path) {
        this.size = size;
        this.path = path;
        buffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        File file = new File(path);
        try {
            if (!file.exists()) {
                file.getParentFile().mkdir();
                file.createNewFile();
            }
            outputStream = new FileOutputStream(file, true);
            channel = outputStream.getChannel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeData(byte[] data) {
        if (data.length == 0) {
            Log.e(TAG, "writeData: " + "数据为空");
            return;
        }
        if (channel == null || outputStream == null) {
            Log.e(TAG, "writeData: " + "初始化失败");
            return;
        }
        buffer.put(data);
        buffer.flip();
        try {
            channel.write(buffer);
            Log.e(TAG, "writeData: " + "写入成功");
            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        buffer.clear();
        try {
            channel.close();
            outputStream.close();
            Log.e(TAG, "close: " + "关闭成功");
        } catch (IOException e) {
            Log.e(TAG, "close: " + "关闭失败");
            e.printStackTrace();
        }
    }
}
```
ReadPCM.java
```
public class ReadPCM {
    private ByteBuffer buffer;
    private FileInputStream inputStream;
    FileChannel channel;
    private String path;
    private int size;
    int count = 0;
    private static final String TAG = "ReadPCM";

    public ReadPCM(String path, int size) {
        this.path = path;
        this.size = size;
        buffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        try {
            inputStream = new FileInputStream(path);
            channel = inputStream.getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public byte[] readData() {
        byte[] data = new byte[size];
        try {
            channel.position(count);
            channel.read(buffer);
            count += size;
            Log.e(TAG, "readData: "+count );
            buffer.flip();
            for (int i = 0; i < size; i++) {
                data[i] = buffer.get();
            }
            buffer.clear();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        buffer.clear();
        try {
            channel.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

```