package com.cyl.music_hnust.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.animation.AnimationUtils;

import com.cyl.music_hnust.MyActivity;
import com.cyl.music_hnust.R;
import com.cyl.music_hnust.fragment.MusicListFragment;
import com.cyl.music_hnust.lyric.LrcProcess;
import com.cyl.music_hnust.lyric.LrcView;
import com.cyl.music_hnust.utils.MusicInfo;
import com.cyl.music_hnust.utils.ToastUtil;

public class MusicPlayService extends Service {

    private final IBinder mBinder = new LocalBinder();
    private Context context;
    /* MediaPlayer对象 */
    private MediaPlayer mMediaPlayer = null;
    private int currentTime = 0;//歌曲播放进度
    private int currentListItme = -1;//当前播放第几首歌
    private List<MusicInfo> songs;//要播放的歌曲集合
    public LrcProcess mLrcProcess;
    public LrcView mLrcView;


    MyReceiver serviceReceiver;
    public static final String BROADCAST_ACTION_SERVICE = "com.cyl.music_hnust.service";// 广播标志



    @Override
    public void onCreate() {
        super.onCreate();
        //实例化过滤器，设置广播
        serviceReceiver = new MyReceiver();

        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION_SERVICE);

        intentFilter.addAction(MyActivity.CTL_ACTION);
        //注册广播
        registerReceiver(serviceReceiver, intentFilter);

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer arg0) {
                // 播放完成一首之后进行下一首
                nextMusic();
            }

        });
    }

    /**
     * 得到当前播放进度
     */
    public int getCurrent() {
        if (mMediaPlayer.isPlaying()) {
            return mMediaPlayer.getCurrentPosition();
        } else {
            return currentTime;
        }
    }

    /**
     * 跳到输入的进度
     */
    public void movePlay(int progress) {
        mMediaPlayer.seekTo(progress);
        currentTime = progress;
    }

    /**
     * 根据歌曲存储路径播放歌曲
     */
    public void playMusic(String path) {
        Intent it = new Intent(MyActivity.UPDATE_ACTION);
        it.putExtra("current", currentListItme);
        it.putExtra("update", 1);
        it.putExtra("name", getSongName());
        it.putExtra("artist", getSingerName());
        sendBroadcast(it);

        try {

            /* 重置MediaPlayer */
            mMediaPlayer.reset();
            /* 设置要播放的文件的路径 */

            mMediaPlayer.setDataSource(path);
            // mMediaPlayer = MediaPlayer.create(this,
            // R.drawable.bbb);播放资源文件中的歌曲
            /* 准备播放 */
            mMediaPlayer.prepare();
//            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    if (mp == mMediaPlayer) {
//                        mMediaPlayer.start();
//                    }
//                }
//            });
//            /* 开始播放 */


           // initLrc(path);
            mMediaPlayer.start();

        } catch (IOException e) {
        }
    }






    /* 下一首 */
    public void nextMusic() {
        if (++currentListItme >= songs.size()) {
            currentListItme = 0;
        }
        if (songs.get(currentListItme).getPath()!=null) {
            playMusic(songs.get(currentListItme).getPath());
        }else {
            ToastUtil.show(context,"播放列表为空");
        }

    }

    /* 上一首 */
    public void frontMusic() {
        Log.v("itme", currentListItme + "hree");
        if (--currentListItme < 0) {
            currentListItme = songs.size() - 1;
        }
        if (songs.get(currentListItme).getPath()!=null) {
            playMusic(songs.get(currentListItme).getPath());
        }else {
            ToastUtil.show(context,"播放列表为空");
        }

    }


    /**
     * 歌曲是否真在播放
     */
    public boolean isPlay() {
        return mMediaPlayer.isPlaying();
    }

    /**
     * 暂停或开始播放歌曲
     */
    public void pausePlay() {
        if (mMediaPlayer.isPlaying()) {
            currentTime = mMediaPlayer.getCurrentPosition();
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return mBinder;
    }
    public MusicInfo getSong(){
        return songs.get(currentListItme);
    }

    public String getSongName() {
        return songs.get(currentListItme).getName();
    }
    public String getPath() {
        return songs.get(currentListItme).getPath();
    }

    public String getSingerName() {
        return songs.get(currentListItme).getArtist();
    }


    /**
     * -，通过这里的getService得到Service，之后就可调用Service这里的方法了
     */
    public class LocalBinder extends Binder {
        public MusicPlayService getService() {
            return MusicPlayService.this;
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public MediaPlayer getmMediaPlayer() {
        return mMediaPlayer;
    }

    public void setmMediaPlayer(MediaPlayer mMediaPlayer) {
        this.mMediaPlayer = mMediaPlayer;
    }

    public int getCurrentListItme() {
        return currentListItme;
    }

    public void setCurrentListItme(int currentListItme) {
        this.currentListItme = currentListItme;
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public List<MusicInfo> getSongs() {
        return songs;
    }

    public void setSongs(List<MusicInfo> songs) {
        this.songs = songs;
    }

    // 兼容2.0以前版本
    @Override
    public void onStart(Intent intent, int startId) {
    }

    // 在2.0以后的版本如果重写了onStartCommand，那onStart将不会被调用，注：在2.0以前是没有onStartCommand方法
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("Service", "Received start id " + startId + ": " + intent);
        // 如果服务进程在它启动后(从onStartCommand()返回后)被kill掉, 那么让他呆在启动状态但不取传给它的intent.
        // 随后系统会重写创建service，因为在启动时，会在创建新的service时保证运行onStartCommand
        // 如果没有任何开始指令发送给service，那将得到null的intent，因此必须检查它.
        // 该方式可用在开始和在运行中任意时刻停止的情况，例如一个service执行音乐后台的重放

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        mMediaPlayer.stop();
        mMediaPlayer.release();

        unregisterReceiver(serviceReceiver);
    }

    private class MyReceiver extends BroadcastReceiver{
        int status = 0;//0未播放 1正在播放 2暂停
        @Override
        public void onReceive(Context context, Intent intent) {
            int control = intent.getIntExtra("control",-1);
            Log.e("ddd",control+"");
            switch (control){
                //播放或暂停
                case 1:
                    pausePlay();
                    if (mMediaPlayer.isPlaying()) {
                        status = 1 ;
                    }else {
                        status = 2 ;
                    }
                    break;
                //上一首歌
                case 2:
                    status =1 ;
                    frontMusic();
                    break;
                //下一首歌
                case 3:
                    status =1 ;
                    nextMusic();
                    break;

            }
            Intent it = new Intent(MyActivity.UPDATE_ACTION);
            it.putExtra("update", status);
            it.putExtra("current", currentListItme);
            it.putExtra("name", getSongName()+"");
            it.putExtra("artist",getSingerName()+"");
            it.putExtra("pic",getSong().getAlbumPic()+"");
            sendBroadcast(it);
        }
    }
}