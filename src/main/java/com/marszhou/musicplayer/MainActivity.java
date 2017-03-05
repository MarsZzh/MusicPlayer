package com.marszhou.musicplayer;

import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button bt_sp, bt_next, bt_qt;
    private static TextView tv_crt, tv_tol;
    private static SeekBar seekBar;
    static int count = 0;                    //count为偶数表示暂停，奇数表示播放
    private static int progress;             //拖动条进度
    static ArrayList<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        bt_sp.setOnClickListener(this);
        bt_next.setOnClickListener(this);
        bt_qt.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {  //当seekBar被持续拖动时回调
                Log.e("changed", progress + "");
                int current = (int) (progress * MusicService.mp.getDuration() / 1000.0);
                tv_crt.setText(new SimpleDateFormat("mm:ss").format(current));                //更新拖动进度
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {               //当seekBar被拖动时回调
                if (bt_sp.getText().toString().equals("暂停")) {                //当音乐在播放时拖动
                    count--;
                    Intent in = new Intent(MainActivity.this, MusicService.class)
                            .putExtra("switch", 0xffffffff);
                    startService(in);
                } else {                                                    //暂停时拖动
                    Intent in = new Intent(MainActivity.this, MusicService.class)
                            .putExtra("switch", 0xffffffff);
                    startService(in);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {                 //当seekBar拖动停止时回调
                count++;
                int progress = seekBar.getProgress();
                int current = (int) (progress * MusicService.mp.getDuration() / 1000.0);
                MusicService.mp.seekTo(current);                                  //在拖动位置处播放
                Intent in = new Intent(MainActivity.this, MusicService.class)
                        .putExtra("switch", 0xffffffff);
                startService(in);
            }
        });
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) { //判断是否挂载sdcard
            getAllMusic();
        }
        if (MusicService.mp != null) {
            if (MusicService.mp.isPlaying()) {
                bt_sp.setText("暂停");
                seekBar.setProgress(progress);
            } else
                seekBar.setProgress(progress);
        }
    }

    public void initView() {                              //获取所有控件实例
        bt_sp = (Button) findViewById(R.id.sp);
        bt_next = (Button) findViewById(R.id.next);
        bt_qt = (Button) findViewById(R.id.quiet);
        tv_crt = (TextView) findViewById(R.id.current);
        tv_tol = (TextView) findViewById(R.id.total);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
    }

    public void getAllMusic() {
        list = new ArrayList<>();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/music";
        Log.e("path", path);
        File dir = new File(path);              //获取/mnt/media_rw/sdcard/music目录对象
        File[] files = dir.listFiles();
        getFilesName(files);
    }

    private void getFilesName(File[] files) {
        if (files != null) {                                // 先判断该目录是否为空
            for (File file : files) {
                if (file.isDirectory()) {
                    getFilesName(file.listFiles());         //递归
                } else {
                    String fileName = file.getName();
                    list.add(fileName);
                    Log.e("name", fileName);
                }
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sp: {
                count++;
                if (count % 2 == 0)
                    bt_sp.setText("播放");
                else
                    bt_sp.setText("暂停");
                Intent in = new Intent(this, MusicService.class)
                        .putExtra("switch", 0xffffffff);
                startService(in);
            }
            break;
            case R.id.next: {
                count = 0;
                bt_sp.setText("播放");
                Intent in = new Intent(this, MusicService.class)
                        .putExtra("switch", 0xfffffffe);
                startService(in);
            }
            break;
            case R.id.quiet:
                stopService(new Intent(this, MusicService.class));
                finish();
                break;
        }
    }


    public static Handler handler = new Handler() {                     //更新进度条和音乐时长
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.obj != null) {
                String total = (String) msg.obj;
                tv_tol.setText(total);
            }
            progress = msg.what;
            int current = (int) (progress * MusicService.mp.getDuration() / 1000.0);
            seekBar.setProgress(progress);
            tv_crt.setText(new SimpleDateFormat("mm:ss").format(current));
        }
    };

}
