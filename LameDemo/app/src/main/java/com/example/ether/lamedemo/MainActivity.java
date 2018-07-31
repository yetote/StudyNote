package com.example.ether.lamedemo;

import android.media.AudioFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        String pcmPath = this.getExternalCacheDir().getPath() + "/pcm/test.pcm";
        String mp3Path = this.getExternalCacheDir().getPath() + "/pcm/sample.mp3";
        MyMP3Encoder encoder = new MyMP3Encoder();
        int ret = encoder.init(pcmPath, AudioFormat.CHANNEL_IN_STEREO, 128 , 44100, mp3Path);
        Log.e(TAG, "onCreate: " + ret);
        if (ret >= 0) {
            encoder.encode();
            encoder.destroy();
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

}
