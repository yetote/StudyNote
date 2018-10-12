package com.example.ether.openslplay;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String audioPath=this.getExternalCacheDir().getPath()+"/test.mp3";
        String outPath=this.getExternalCacheDir().getPath()+"/test.pcm";
        AudioPlayer.play(audioPath,outPath);
    }

}
