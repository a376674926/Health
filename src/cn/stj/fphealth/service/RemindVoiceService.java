
package cn.stj.fphealth.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

public class RemindVoiceService extends Service {

    private static final String TAG = RemindVoiceService.class.getSimpleName();
    public static final String REMIND_VOICE_PATH = "remind_voice_path";
    private Vibrator mVibrator;
    private boolean mIsVibrate;
    private MediaPlayer mMediaPlayer;
    private String mRemindVoicePath;
    private PowerManager.WakeLock mWakeLock;
    private AudioManager mAudioManager;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    public void onCreate() {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    }

    private void setWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (!pm.isScreenOn()) {
            // 点亮屏
            mWakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
            mWakeLock.acquire();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 设置唤醒锁屏
        setWakeLock();

        initDatas(intent);
        setVibrator();
        // playTest() ;
        play();
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 初始化数据
     * 
     * @param intent
     */
    private void initDatas(Intent intent) {
        if (intent != null) {
            mRemindVoicePath = intent.getStringExtra(REMIND_VOICE_PATH);
        }
        mIsVibrate = true;
    }

    // 设置震动
    private void setVibrator() {
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (mIsVibrate) {
            mVibrator.vibrate(new long[] {
                    1000, 2000
            }, -1);
        } else {
            closeVirbrator();
        }
    }

    private void play() {
        if (TextUtils.isEmpty(mRemindVoicePath)) {
            return;
        }
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mRemindVoicePath);
            if (mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                // 设置声音播放通道
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver stopAlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            closePlay();
            closeVirbrator();
        }
    };

    private void closePlay() {
        try {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                Log.v(TAG, "onDestroy closeMediaPlayer");
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void closeVirbrator() {
        if (mVibrator != null) {
            mVibrator.cancel();
            mVibrator = null;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        closeVirbrator();
        closePlay();
        unregisterReceiver(stopAlarmReceiver);
    }

}
