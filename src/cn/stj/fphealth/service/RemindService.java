
package cn.stj.fphealth.service;

import android.R.integer;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.widget.Toast;

import cn.stj.fphealth.R;
import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.app.Utils;
import cn.stj.fphealth.db.Database;
import cn.stj.fphealth.db.LitepalDatabaseImpl;
import cn.stj.fphealth.entity.RemindInfo;
import cn.stj.fphealth.model.HealthMinaModelImpl;
import cn.stj.fphealth.model.HealthModel;
import cn.stj.fphealth.model.HealthModelImpl;
import cn.stj.fphealth.receiver.RemindReceiver;
import cn.stj.fphealth.util.DatetimeUtil;
import cn.stj.fphealth.util.FileUtil;
import cn.stj.fphealth.util.LogUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.SimpleFormatter;

public class RemindService extends Service {

    private Database mDatabase;
    public static final String REMIND_INFO = "remindInfo";
    public static final String EDIT_REMIND_INFO = "edit_remindInfo";
    public static final String REMIND_FLAG = "remind_flag";
    public static final int REMIND_ADD = 1;
    public static final int REMIND_EDIT = 2;
    public static final int REMIND_DEL = 3;
    public static final int REMIND_REPEAT = 4;
    public static final int FRIDAY = 6;
    public static final int SATURDAY = 4;
    private int mRemindFlag;
    private List<RemindInfo> mRemindInfos = new ArrayList<RemindInfo>();
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private HealthModel mHealthModel;
    private Vibrator mVibrator;
    private PowerManager.WakeLock mWakeLock;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mDatabase = new LitepalDatabaseImpl();
        mHealthModel = new HealthMinaModelImpl(this);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mRemindFlag = -1;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mRemindFlag = intent.getIntExtra(REMIND_FLAG, -1);
            LogUtil.i("debug", "=========RemindService=======onStartCommand=====mRemindFlag:" + mRemindFlag);
            switch (mRemindFlag) {
                case REMIND_ADD:
                    List<RemindInfo> addRemindInfos = intent
                            .getParcelableArrayListExtra(REMIND_INFO);
                    addRemind(addRemindInfos);
                    break;
                case REMIND_EDIT:
                    List<RemindInfo> editRemindInfos = intent
                            .getParcelableArrayListExtra(REMIND_INFO);
                    updateRemind(editRemindInfos);
                    break;
                case REMIND_DEL:
                    List<RemindInfo> delRemindInfos = intent
                            .getParcelableArrayListExtra(REMIND_INFO);
                    deleteRemind(delRemindInfos);
                    //刷新提醒界面
                    Intent unbindIntent1 = new Intent(Constants.REFRESH_REMIND_ACTION);
                    sendBroadcast(unbindIntent1);
                    break;
                case REMIND_REPEAT:
                    final RemindInfo remindInfo = intent.getParcelableExtra(REMIND_INFO);
                    if (remindInfo.getTime() == remindInfo.getEndTime()) {
                        // List<RemindInfo> repeatRemindInfos = new
                        // ArrayList<RemindInfo>();
                        // repeatRemindInfos.add(remindInfo);
                        // mDatabase.deleteRemindInfo(repeatRemindInfos);
                        // FileUtil.deleteRemindVoice(this, remindInfo);
                    } else {
                        LogUtil.i("debug", "==========RemindService===============remindInfo:" + remindInfo.toString());
                        repeatRemind(remindInfo);
                        setWakeLock();
                        Utils.playRemindVoice(this, remindInfo);
                        setVibrator();
                        mHealthModel.sendRemindSuccessNotice(remindInfo);
                        //刷新提醒界面
                        Intent unbindIntent = new Intent(Constants.REFRESH_REMIND_ACTION);
                        sendBroadcast(unbindIntent);
                    }
                    break;
                default:
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void deleteRemind(List<RemindInfo> remindInfos) {
        for (int i = 0; i < remindInfos.size(); i++) {
            RemindInfo remindInfo = remindInfos.get(i);
            cancelRemind(this, remindInfo);
            FileUtil.deleteRemindVoice(this, remindInfo);
        }
    }

    private void updateRemind(List<RemindInfo> remindInfos) {
        LogUtil.i("debug", "=====ReMindService========updateRemind==========");
        for (int i = 0; i < remindInfos.size(); i++) {
            RemindInfo remindInfo = remindInfos.get(i);
            scheduleAlarms(this, remindInfo);
            FileUtil.updateRemindVoice(this, remindInfo);
        }
    }

    private void addRemind(List<RemindInfo> remindInfos) {
        LogUtil.i("debug", "=============addRemind==========");
        for (int i = 0; i < remindInfos.size(); i++) {
            RemindInfo remindInfo = remindInfos.get(i);
            scheduleAlarms(this, remindInfo);
            Utils.downRemindVoiceByHttp(this, remindInfo);
        }
    }

    private void repeatRemind(RemindInfo remindInfo) {
        int periodType = remindInfo.getPeriodType();
        switch (periodType) {
            case RemindInfo.PERIOD_ONCE:
                LogUtil.i("debug", "============repeatRemind=========RemindInfo.PERIOD_ONCE");
                // remindInfo.setEndTime(remindInfo.getTime() + Constants.DAY);
//                repeatOnceRemind(remindInfo);
                break;
            case RemindInfo.PERIOD_DAY:
                repeatDayRemind(remindInfo);
                break;
            case RemindInfo.PERIOD_WORKING_DAY:
                repeatWorkingDayRemind(remindInfo);
                break;
            case RemindInfo.PERIOD_WEEK:
                repeatWeekRemind(remindInfo);
                break;
            case RemindInfo.PERIOD_MONTH:
                repeatMonthRemind(remindInfo);
                break;
            case RemindInfo.PERIOD_YEAR:
                repeatYearRemind(remindInfo);
                break;
            default:
                break;
        }
    }

    private void repeatYearRemind(RemindInfo remindInfo) {
        List<RemindInfo> yearRemindInfos = new ArrayList<RemindInfo>();
        yearRemindInfos.add(remindInfo);
        mDatabase.updateRemindInfo(yearRemindInfos);
        long remindTime = remindInfo.getTime();
        long endTime = remindInfo.getEndTime();
        remindTime = remindTime + Constants.DAY * 365;
        if (remindTime < endTime) {
            remindInfo.setTime(remindTime);
            scheduleAlarms(this, remindInfo);
        } else {
            /*
             * remindInfo.setTime(remindInfo.getEndTime()); scheduleAlarms(this,
             * remindInfo);
             */
        }
    }

    private void repeatMonthRemind(RemindInfo remindInfo) {
        List<RemindInfo> monthRemindInfos = new ArrayList<RemindInfo>();
        monthRemindInfos.add(remindInfo);
        mDatabase.updateRemindInfo(monthRemindInfos);
        long remindTime = remindInfo.getTime();
        long endTime = remindInfo.getEndTime();
        remindTime = remindTime + Constants.DAY * 30;
        if (remindTime < endTime) {
            remindInfo.setTime(remindTime);
            scheduleAlarms(this, remindInfo);
        } else {
            /*
             * remindInfo.setTime(remindInfo.getEndTime()); scheduleAlarms(this,
             * remindInfo);
             */
        }
    }

    private void repeatWeekRemind(RemindInfo remindInfo) {
        List<RemindInfo> weekRemindInfos = new ArrayList<RemindInfo>();
        weekRemindInfos.add(remindInfo);
        mDatabase.updateRemindInfo(weekRemindInfos);
        long remindTime = remindInfo.getTime();
        long endTime = remindInfo.getEndTime();
        remindTime = remindTime + Constants.DAY * 7;
        if (remindTime < endTime) {
            remindInfo.setTime(remindTime);
            scheduleAlarms(this, remindInfo);
        } else {
            /*
             * remindInfo.setTime(remindInfo.getEndTime()); scheduleAlarms(this,
             * remindInfo);
             */
        }
    }

    private void repeatWorkingDayRemind(RemindInfo remindInfo) {
        List<RemindInfo> workingDayRemindInfos = new ArrayList<RemindInfo>();
        workingDayRemindInfos.add(remindInfo);
        mDatabase.updateRemindInfo(workingDayRemindInfos);
        long remindTime = remindInfo.getTime();
        long endTime = remindInfo.getEndTime();
        int dayOfWeek = DatetimeUtil.dayOfWeek(new Date(remindTime));
        if (dayOfWeek == FRIDAY) {
            remindTime = remindTime + Constants.DAY * 3;
        } else if (dayOfWeek == SATURDAY) {
            remindTime = remindTime + Constants.DAY * 2;
        } else {
            remindTime = remindTime + Constants.DAY;
        }
        if (remindTime < endTime) {
            remindInfo.setTime(remindTime);
            scheduleAlarms(this, remindInfo);
        } else {
            /*
             * remindInfo.setTime(remindInfo.getEndTime()); scheduleAlarms(this,
             * remindInfo);
             */
        }
    }

    private void repeatDayRemind(RemindInfo remindInfo) {
        List<RemindInfo> dayRemindInfos = new ArrayList<RemindInfo>();
        dayRemindInfos.add(remindInfo);
        mDatabase.updateRemindInfo(dayRemindInfos);
        long remindTime = remindInfo.getTime();
        long endTime = remindInfo.getEndTime();
        remindTime = remindTime + Constants.DAY;
        if (remindTime < endTime) {
            remindInfo.setTime(remindTime);
            scheduleAlarms(this, remindInfo);
        } else {
            /*
             * remindInfo.setTime(remindInfo.getEndTime()); scheduleAlarms(this,
             * remindInfo);
             */
        }
    }

    private void repeatOnceRemind(RemindInfo remindInfo) {
        LogUtil.i("debug", "=====RemindService========repeatOnceRemind=======");
        remindInfo.setTime(remindInfo.getEndTime());
        scheduleAlarms(this, remindInfo);
    }

    public void scheduleAlarms(Context context, RemindInfo remindInfo) {
        LogUtil.i("debug", "=====RemindService========scheduleAlarms=======");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long systemTime = System.currentTimeMillis();
        // 选择提醒的时间
        long remindTime = remindInfo.getTime();
        // 如果当前时间大于提醒时间，更新提醒时间日期到当前日期，如果更新后当前时间还大于提醒时间，则更新提醒日期到下一天
        if (systemTime > remindTime) {
            Calendar currCalendar = Calendar.getInstance();
            currCalendar.setTimeInMillis(systemTime);
            Calendar remindCalendar = Calendar.getInstance();
            remindCalendar.setTimeInMillis(remindTime);
            remindCalendar.set(Calendar.YEAR,
                    currCalendar.get(Calendar.YEAR));
            remindCalendar.set(Calendar.MONTH,
                    currCalendar.get(Calendar.MONTH));
            remindCalendar.set(Calendar.DAY_OF_MONTH,
                    currCalendar.get(Calendar.DAY_OF_MONTH));
            remindTime = remindCalendar.getTimeInMillis();
            if (systemTime > remindTime) {
                remindCalendar.add(Calendar.DAY_OF_MONTH, 1);
                remindTime = remindCalendar.getTimeInMillis();
                if (remindInfo.getEndTime() != 0 && remindTime > remindInfo.getEndTime()) {// 在子女端app更新结束时间，避免最后一次提醒时间大于结束时间
                // remindTime = remindInfo.getEndTime();
                    return;
                }
            }
        }
        LogUtil.i("debug",
                "=========RemindService======scheduleAlarms===remindId:" + remindInfo.getRemindId()
                        + " remindTime:" + simpleDateFormat.format(new Date(remindTime)));
        Intent intent = new Intent(this, RemindReceiver.class);
        intent.putExtra(REMIND_INFO, remindInfo);
        PendingIntent remindIntent = PendingIntent.getBroadcast(context, remindInfo.getRemindId(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(remindIntent);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, remindTime, remindIntent);

    }

    public void cancelRemind(Context context, RemindInfo remindInfo) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, RemindReceiver.class);
        PendingIntent remindIntent = PendingIntent.getBroadcast(context, remindInfo.getRemindId(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(remindIntent);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        closeVirbrator();
    }

    // 设置震动
    private void setVibrator() {
        mVibrator.vibrate(new long[] {
                1000, 2000
        }, -1);
    }

    private void closeVirbrator() {
        if (mVibrator != null) {
            mVibrator.cancel();
            mVibrator = null;
        }
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
}
