
package cn.stj.fphealth.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.model.HealthMinaModelImpl;
import cn.stj.fphealth.model.HealthModel;
import cn.stj.fphealth.model.HealthModelImpl;
import cn.stj.fphealth.receiver.HeartbeatReceiver;
import cn.stj.fphealth.util.LogUtil;
import cn.stj.fphealth.util.PreferencesUtils;

/**
 * heart beat service to keep alive connection
 * 
 * @author jackey
 */
public class HeartbeatService extends Service {
    private Context mContext = null;
    private HealthModel mHealthModel;
    private AlarmManager mAlarmManager;

    @Override
    public void onCreate() {
        mContext = getApplicationContext();
        mHealthModel = new HealthMinaModelImpl(mContext);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.i("debug", "=========beatService=======onStartCommand=======");
        new Thread(new Runnable() {

            @Override
            public void run() {
                mHealthModel.sendHeartBeat(1);
            }
        }).start();
        long intervalTime = PreferencesUtils.getInt(this,Constants.DEVICE_PARAM.BEAT) == -1?Constants.INTERVAL_TIME: PreferencesUtils.getInt(this,Constants.DEVICE_PARAM.BEAT) * 1000 ;
        long triggerAtTime = System.currentTimeMillis() + intervalTime;
        Intent i = new Intent(this, HeartbeatReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(this,
                HeartbeatReceiver.class), 0);
        mAlarmManager.cancel(pi);
    }
}
