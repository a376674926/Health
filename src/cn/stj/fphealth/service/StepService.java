
package cn.stj.fphealth.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.app.FPHealthApplication;
import cn.stj.fphealth.entity.Pedometer;
import cn.stj.fphealth.model.HealthMinaModelImpl;
import cn.stj.fphealth.model.HealthModel;
import cn.stj.fphealth.receiver.StepReceiver;
import cn.stj.fphealth.service.StepDetector.StepDetectorListener;
import cn.stj.fphealth.util.LogUtil;
import cn.stj.fphealth.util.PreferencesUtils;

import java.util.Calendar;
import java.util.List;

public class StepService extends Service implements SensorEventListener {
    public static final String TAG = StepService.class.getName();
    public static final String REPEAT_FLAG = "repeat_flag";
    private static final int REPEAT_DAY = 20;
    private static final int REPEAT_UPLOAD = 21;
    public static Boolean flag = false;
    private SensorManager mSensorManager;
    // private StepDetector mStepDetector;
    private HealthModel mHealthModel;
    private AlarmManager mAlarmManager;
    private Context mContext;
    private long mLastTime;
    private long mPedometerLastTime;
    private int mLastStep;
    private final IBinder mBinder = new ServiceBinder();
    private static final long PERIOD = 24 * 60 * 60 * 1000;
    private StepDetectorListener mStepDetectorListener;
    private int mRepeatFlag;
    private HealthUnbindReceiver mHealthUnbindReceiver;
    private WakeLock mWakeLock = null;
    public static final int SCREEN_OFF_RECEIVER_DELAY = 500;

    public static int CURRENT_SETP = 0;
    public static float SENSITIVITY = 2; // SENSITIVITY
    private float mLastValues[] = new float[3 * 2];
    private float mScale[] = new float[2];
    private float mYOffset;
    private static long end = 0;
    private static long start = 0;
    private float mLastDirections[] = new float[3 * 2];
    private float mLastExtremes[][] = {
            new float[3 * 2], new float[3 * 2]
    };
    private float mLastDiff[] = new float[3 * 2];
    private int mLastMatch = -1;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class ServiceBinder extends Binder {

        public StepService getService() {
            return StepService.this;
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (mStepDetectorListener != null) {
                mStepDetectorListener.stepDetectCallBack();
            }
        };
    };

    @Override
    public void onCreate() {
        super.onCreate();
        int h = 480;
        mYOffset = h * 0.5f;
        mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        mScale[1] = -(h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
        startStepDetector();
        mContext = getApplicationContext();
        mHealthModel = new HealthMinaModelImpl(mContext);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mLastStep = PreferencesUtils.getInt(this, Constants.KEY_TOTAL_STEP, 0);
        StepService.CURRENT_SETP = PreferencesUtils.getInt(this, Constants.KEY_TOTAL_STEP, 0);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.DEVICE_UNBIND_ACTION);
        intentFilter.addAction(Constants.CLEAR_STEP_ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mHealthUnbindReceiver = new HealthUnbindReceiver();
        registerReceiver(mHealthUnbindReceiver, intentFilter);

        mRepeatFlag = -1;
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));

    }

    private void startStepDetector() {
        flag = true;
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        registerStepListener();
        PowerManager manager =
                (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.i("debug", "===StepService=========onStartCommand====");
        if (intent != null) {
            mRepeatFlag = intent.getIntExtra(REPEAT_FLAG, -1);
            switch (mRepeatFlag) {
                case REPEAT_UPLOAD:
                    uploadWalkSteps();
                    repeatUpload(REPEAT_UPLOAD);
                    break;
                case REPEAT_DAY:
                    repeatDay(REPEAT_DAY);
                    StepService.CURRENT_SETP = 0;
                    mLastStep = 0;
                    PreferencesUtils.putInt(this, Constants.KEY_TOTAL_STEP, 0);
                    if (mStepDetectorListener != null) {
                        mStepDetectorListener.stepDetectCallBack();
                    }
                    break;
                default:
                    repeatDay(REPEAT_DAY);
                    repeatUpload(REPEAT_UPLOAD);
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void uploadWalkSteps() {
        // LogUtil.i("debug",
        // "====StepService==@@=====uploadWalkSteps==========@@=======mPedometerLastTime:"
        // + mPedometerLastTime + "===remindTime" + System.currentTimeMillis()
        // + "======currentStep:" + StepDetector.CURRENT_SETP +
        // "=============mLastStep:" + mLastStep);
        int count = StepService.CURRENT_SETP - mLastStep;
        if (count > 0) {
            mLastStep = StepService.CURRENT_SETP;
            List<Pedometer> pedometers = FPHealthApplication.getInstance().getmDatabase()
                    .getPedometers();
            Pedometer pedometer = new Pedometer();
            pedometer.setType(1);
            pedometer.setCount(count + "");
            pedometer.setFromTime(String.valueOf(mPedometerLastTime));
            pedometer.setToTime(String.valueOf(System.currentTimeMillis()));
            pedometer.setHeight(PreferencesUtils.getInt(mContext, Constants.DEVICE_PARAM.HEIGHT)
                    + "");
            pedometer.save();// 缓存到数据库中
            pedometers.add(pedometer);
            LogUtil.i("debug", "上传步数：" + pedometer.getCount() + " 开始时间：" + pedometer.getFromTime()
                    + " 结束时间：" + pedometer.getToTime());
            mHealthModel.sendWalkUpload(pedometers);
        }
    }

    private void repeatDay(int repeatDay) {
        scheduleAlarms(this, repeatDay);
    }

    private void repeatUpload(int repeatUpload) {
        scheduleAlarms(this, repeatUpload);
    }

    public void scheduleAlarms(Context context, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // 选择提醒的时间
        long remindTime = 0l;
        if (requestCode == REPEAT_DAY) {
            if (mRepeatFlag == -1) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DATE) + 1, 0, 0, 0);
                remindTime = calendar.getTimeInMillis();
                // LogUtil.i("debug",
                // "====StepService=====scheduleAlarms=@@@@@==requestCode:REPEAT_DAY==>>"
                // + DatetimeUtil.format(remindTime, "yyyy-MM-dd HH:mm:ss") +
                // "---mRepeatFlag:" + mRepeatFlag);
            } else {
                remindTime = System.currentTimeMillis() + PERIOD;
            }
        } else if (requestCode == REPEAT_UPLOAD) {
            mPedometerLastTime = System.currentTimeMillis();
            long walkUfreq = PreferencesUtils.getInt(mContext, Constants.DEVICE_PARAM.WALK_UFREQ,
                    60) * 1000;
            long uploadAtTime = mPedometerLastTime + walkUfreq;
            remindTime = uploadAtTime;
        }
        Intent intent = new Intent(this, StepReceiver.class);
        intent.putExtra(REPEAT_FLAG, requestCode);
        intent.putExtra("remindTime", remindTime);
        PendingIntent locationIntent = PendingIntent.getBroadcast(context, requestCode,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(locationIntent);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, remindTime, locationIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.i("debug", "==========StepService=====onDestroy()==========");
        flag = false;
        unregisterStepListener();
        if (mHealthUnbindReceiver != null) {
            unregisterReceiver(mHealthUnbindReceiver);
        }
        if (mWakeLock != null) {
            mWakeLock.release();
        }
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    public void registerStepDetectLister(StepDetector.StepDetectorListener listener) {
        mStepDetectorListener = listener;
        // mStepDetector.setmStepDetectorListener(listener);
    }

    private class HealthUnbindReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.i("debug", "====StepService===@@=======HealthUnbindReceiver=====@@@====");
            mLastStep = 0;
            StepService.CURRENT_SETP = 0;
        }

    }

    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.i("debug", "onReceive(" + intent + ")");

            if (!intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                return;
            }

            Runnable runnable = new Runnable() {
                public void run() {
                    LogUtil.i("debug", "Runnable executing.");
                    unregisterStepListener();
                    registerStepListener();
                }
            };

            new Handler().postDelayed(runnable, SCREEN_OFF_RECEIVER_DELAY);
        }
    };

    private void unregisterStepListener() {
        /*
         * if (mStepDetector != null) {
         * mSensorManager.unregisterListener(mStepDetector); mStepDetector =
         * null; }
         */
        mSensorManager.unregisterListener(this);
    }

    private void registerStepListener() {
        // mStepDetector = new StepDetector(this);
        Sensor sensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, sensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        // mStepDetector.setmStepDetectorListener(mStepDetectorListener);
    }

    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        synchronized (this) {
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                float vSum = 0;
                for (int i = 0; i < 3; i++) {
                    final float v = mYOffset + event.values[i] * mScale[1];
                    vSum += v;
                }
                int k = 0;
                float v = vSum / 3;

                float direction = (v > mLastValues[k] ? 1
                        : (v < mLastValues[k] ? -1 : 0));
                if (direction == -mLastDirections[k]) {
                    // Direction changed
                    int extType = (direction > 0 ? 0 : 1); // minumum or
                                                           // maximum?
                    mLastExtremes[extType][k] = mLastValues[k];
                    float diff = Math.abs(mLastExtremes[extType][k]
                            - mLastExtremes[1 - extType][k]);

                    if (diff > SENSITIVITY) {
                        boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k] * 2 / 3);
                        boolean isPreviousLargeEnough = mLastDiff[k] > (diff / 3);
                        boolean isNotContra = (mLastMatch != 1 - extType);

                        if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough
                                && isNotContra) {
                            end = System.currentTimeMillis();
                            if (end - start > 500) {

                                CURRENT_SETP++;
                                mLastMatch = extType;
                                start = end;
                                if (mStepDetectorListener != null) {
                                    mStepDetectorListener.stepDetectCallBack();
                                }
                            }
                        } else {
                            mLastMatch = -1;
                        }
                    }
                    mLastDiff[k] = diff;
                }
                mLastDirections[k] = direction;
                mLastValues[k] = v;
            }

        }
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {

    }

}
