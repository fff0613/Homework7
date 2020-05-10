package com.bytedance.videoplayer;

import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static int titleHeight = 200;
    private SurfaceView surfaceView;
    private MediaPlayer player;
    private SurfaceHolder holder;
    private SeekBar seekBar;
    private TextView tvTime;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("MediaPlayer");

        setContentView(R.layout.activity_main);
        surfaceView = findViewById(R.id.surfaceView);

        holder = surfaceView.getHolder();
        holder.addCallback(new PlayerCallBack());
        seekBar = findViewById(R.id.seekBar);
        tvTime = findViewById(R.id.time);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (player != null ) {
                    int progress = seekBar.getProgress();
                    System.out.println(progress);
                    player.seekTo(progress);
                    set(seekBar.getProgress(),player.getDuration());
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (player != null && fromUser) {
                    System.out.println(progress);
                    player.seekTo(seekBar.getProgress());
                }
                set(seekBar.getProgress(),player.getDuration());
            }
        });

        findViewById(R.id.buttonPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.start();

            }
        });

        findViewById(R.id.buttonPause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.pause();
            }
        });

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        changeVideoSize(player);
    }
    private void set(int progress,int max){
        tvTime.setText(toTime(progress) + "/" + toTime(max));
    }

    private String toTime(int progress){
        StringBuffer stringBuffer = new StringBuffer();
        int s = (progress/1000) % 60;
        int m = progress / 60000;
        stringBuffer.append(m).append(":");
        if(s < 10){
            stringBuffer.append(0);
        }
        stringBuffer.append(s);
        return stringBuffer.toString();
    }

    public void changeVideoSize(MediaPlayer mediaPlayer) {
        int deviceWidth = getResources().getDisplayMetrics().widthPixels;
        int deviceHeight = getResources().getDisplayMetrics().heightPixels - titleHeight;

        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();

        //根据视频尺寸去计算->视频可以在sufaceView中放大的最大倍数。

        float videoPercent = (float) videoWidth / (float) videoHeight;

        if (getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            //竖屏模式下按视频宽度计算放大倍数值
//            this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
            videoWidth = deviceWidth;
            videoHeight =  (int) (videoWidth / videoPercent);
//            devicePercent = (float) deviceWidth / (float) deviceHeight;
        } else {
            //横屏模式下按视频高度计算放大倍数值
//            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            videoHeight = deviceHeight;
            videoWidth = (int) (videoHeight * (videoPercent+0.27));

        }


//        ViewGroup.LayoutParams r =
        //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
//        videoWidth = (int) Math.ceil((float) videoWidth / max);
//        videoHeight = (int) Math.ceil((float) videoHeight / max);
//        System.out.println("max: "+max);
        //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
        surfaceView.setLayoutParams(new LinearLayout.LayoutParams(videoWidth, videoHeight));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.stop();
            player.release();
        }
    }

    private class PlayerCallBack implements SurfaceHolder.Callback {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            player = new MediaPlayer();
            try {
                player.setDataSource(getResources().openRawResourceFd(R.raw.bytedance));
                player.prepare();
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        // 自动播放
//                        player.start();
                        seekBar.setMax(player.getDuration());
                        Timer timer = new Timer();
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                seekBar.setProgress(player.getCurrentPosition());
                            }
                        };
                        timer.schedule(task,0,100);
                        set(seekBar.getProgress(),player.getDuration());
                        player.setLooping(true);
                    }
                });
                player.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                        changeVideoSize(mp);
//
                    }
                });
                player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
                        System.out.println(percent);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            player.setDisplay(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            changeVideoSize(player);
            player.setDisplay(holder);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    }


}
