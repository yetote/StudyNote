package com.yetote.nativemediademo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private SurfaceView surfaceView;
    private Surface surface;
    private Button button, destroyBtn;

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = findViewById(R.id.surfaceView);
        button = findViewById(R.id.btn);
        destroyBtn = findViewById(R.id.destroy);
        final String path = getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath() + "/test.mp4";
        final String path2 = getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath() + "/1.mp3";
        final String path3 = getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath() + "/output2.mp4";
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                init(path3, surfaceHolder.getSurface());
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });
        button.setOnClickListener(v -> play());
        destroyBtn.setOnClickListener(v -> destroy());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroy();
    }

    public native void init(String path, Surface surface);

    public native void play();

    public native void destroy();
}
