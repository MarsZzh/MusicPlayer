package com.marszhou.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class MusicService extends Service {
    static MediaPlayer mp;
    private ArrayList<String> list;
    private int flag = 1;
    private static final String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath() + "/music/"; // sdcard路径/mnt/media_rw/sdcard/music

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("service", "服务被启动");
        mp = new MediaPlayer();
        list = MainActivity.list;          //获取所有音乐名
        try {
            mp.setDataSource(SDCARD + list.get(0));         //设置第一首歌
            mp.prepare();             //装载音乐文件资源
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            int num = intent.getIntExtra("switch", 0);
            if (num == 0xffffffff) {                               //按下播放按钮
                if (MainActivity.count % 2 == 0) {
                    mp.pause();
                } else {
                    mp.start();
                    updateProgress();
                }
            } else if (num == 0xfffffffe) {                        //按下下一首按钮
                if (flag > 0 && flag <= list.size() - 1) {
                    if (mp != null) {
                        if (mp.isPlaying()) {
                            mp.stop();
                        }
                        mp.reset();                                     //重置
                        mp.setDataSource(SDCARD + list.get(flag));
                        mp.prepare();                              //装载音乐文件资源
                    }

                } else {
                    flag = 0;
                    if (mp != null) {
                        if (mp.isPlaying()) {
                            mp.stop();
                        }
                        mp.reset();                                     //重置
                        mp.setDataSource(SDCARD + list.get(flag));
                        mp.prepare();                              //装载音乐文件资源
                    }
                }
                flag++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return super.onStartCommand(intent, flags, startId);
    }

    public void updateProgress() {                                   //更新进度条
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                msg.obj = getTime();
                MainActivity.handler.sendMessage(msg);
                while (MainActivity.count % 2 != 0) {
                    Message msg_ = Message.obtain();
                    int progress = (int) (mp.getCurrentPosition() * 1000.0 / mp.getDuration());
                    msg_.what = progress;                        //获取当前播放位置
                    MainActivity.handler.sendMessage(msg_);
                    SystemClock.sleep(1000);                     //延时一秒钟
                }
            }
        }).start();
        Log.e("thread", "更新线程结束");
    }

    public String getTime() {                        //将毫秒转换成时间
        int total = mp.getDuration();
        String format = "mm:ss";                     //自定义时间模式
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(total);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mp != null) {
            if (mp.isPlaying()) {
                mp.stop();
                mp.release();          //释放音乐文件资源
            } else
                mp.release();
        }

        Log.e("service", "服务被销毁");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
