package com.example.ether.ndkplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.ether.ndkplayer.utils.TextRecourseReader;
import com.example.ether.ndkplayer.utils.TextureHelper;

public class MainActivity extends AppCompatActivity {

    private SurfaceView surfaceView;
    private static final String TAG = "MainActivity";
    private Surface surface;
    private Button btn;
    int w, h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PlayerView playerView = new PlayerView();
        playerView.start();
        surfaceView = findViewById(R.id.surfaceView);
        btn = findViewById(R.id.btn);
        String path = this.getExternalCacheDir().getPath() + "/test.mp4";
        String vertexCode = TextRecourseReader.readTextFileFromResource(MainActivity.this, R.raw.vertex_shader);
        String fragCode = TextRecourseReader.readTextFileFromResource(MainActivity.this, R.raw.fragment_shader);

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.e(TAG, "surfaceChanged: " + Thread.currentThread().getName());
                surface = holder.getSurface();
                w = width;
                h = height;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        btn.setOnClickListener(v -> playerView.draw(path, vertexCode, fragCode, surface, w, h));
    }

}
