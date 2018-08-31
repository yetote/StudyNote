package com.example.ether.myplayer;

import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author yetote QQ:503779938
 * @name MyPlayer
 * @class name：com.example.ether.myplayer
 * @class MainActivity
 * @time 2018/8/30 11:04
 * @change
 * @chang time
 * @class describe
 */
public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final int UPDATE_PLAY_VIEDO_TIME_FLAG = 1201;
    private SurfaceView surfaceView;
    private Button playBtn, pauseBtn;
    private ProgressBar progressBar;
    private SurfaceHolder surfaceHolder;
    private PlayerController playerController;
    private float totalDuration = 0.0f;
    //    private Handler handler = new Handler() {
//
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case UPDATE_PLAY_VIEDO_TIME_FLAG:
//                    if (!isDragging) {
//                        String curtime = String.format("%02d:%02d", (int) playTimeSeconds / 60, (int) playTimeSeconds % 60);
//                        String totalTime = String.format("%02d:%02d", (int) totalDuration / 60, (int) totalDuration % 60);
//                        current_time_label.setText(curtime);
//                        end_time_label.setText(totalTime);
//                        int progress = totalDuration == 0.0f ? 0 : (int) (playTimeSeconds * 100 / totalDuration);
//                        int secondProgress = totalDuration == 0.0f ? 0 : (int) (bufferedTimeSeconds * 100 / totalDuration);
//                        playerSeekBar.setProgress(progress);
//                        playerSeekBar.setSecondaryProgress(secondProgress);
//                    }
//                    break;
//                case PLAY_END_FLAG:
//                    Toast.makeText(ChangbaPlayerActivity.this, "播放结束了！", Toast.LENGTH_SHORT).show();
//                    break;
//                default:
//                    break;
//            }
//        }
//
//    };
    private boolean isDragging = false;
    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        surfaceHolder.addCallback(this);
        pauseBtn.setOnClickListener(v -> playerController.onPause());
        playBtn.setOnClickListener(v -> playerController.play());
    }

    private void initView() {
        surfaceView = findViewById(R.id.surfaceView);
        playBtn = findViewById(R.id.play);
        pauseBtn = findViewById(R.id.pauseBtn);
        progressBar = findViewById(R.id.progressBar);
        surfaceHolder = surfaceView.getHolder();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (isFirst) {
            playerController = new PlayerController() {
                @Override
                public void onCompletion() {
                    super.onCompletion();
                    playerController.onPause();
//                        timerTask.cancel();
//                        timerTask = null;
//                        timer.cancel();
//                        timer = null;
//                        playerSeekBar.setProgress(0);
//                        playerSeekBar.setSecondaryProgress(0);
//                        playerController.seekToPosition(0);
                }

                @Override
                public void videoDecodeException() {
                    super.videoDecodeException();
                }

                @Override
                public void viewStreamMetaCallback(int width, int height, float duration) {
                    super.viewStreamMetaCallback(width, height, duration);
//                        MainActivity.this.totalDuration = duration;
//                        handler.post(() -> {
//                            int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
//                            int drawHeight = (int) ((float) screenWidth / ((float) width / (float) height));
//                            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) surfaceView.getLayoutParams();
//                            params.height = drawHeight;
//                            surfaceView.setLayoutParams(params);
//
//                            playerController.resetRenderSize(0, 0, screenWidth, drawHeight);
//                        });
                }
            };
            playerController.setUseMediaCodec(false);
            Point point = new Point();
            getWindowManager().getDefaultDisplay().getSize(point);
            int width = point.x;
            int height = point.y;
            String path = MainActivity.this.getExternalCacheDir().getPath() + "/test.flv";
            playerController.init(path, surfaceHolder.getSurface(), width, height, new OnInitializedCallback() {
                @Override
                public void onInitialized(OnInitialStatus onInitialStatus) {
                    Log.i("problem", "onInitialized called");
                }
            });
            isFirst = false;
        } else {
            playerController.onSurfaceCreated(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        playerController.resetRenderSize(0, 0, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        playerController.onSurfaceDestroyed(holder.getSurface());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerController.stopPlay();

    }
}

