package com.example.ether.opensldemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    private static final String TAG = "MainActivity";
    private Button btn;
    private OpenSLTest openSLTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        openSLTest = new OpenSLTest();
        final String pcmPath = this.getExternalCacheDir().getPath() + "/test.pcm";
        Log.e(TAG, "onCreate: " + pcmPath);
        btn = findViewById(R.id.sample_text);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openSLTest.play(pcmPath);
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

}
