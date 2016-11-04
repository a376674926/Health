
package cn.stj.fphealth.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.entity.HeartRate;
import cn.stj.fphealth.entity.RemindInfo;
import cn.stj.fphealth.model.HealthManager;
import cn.stj.fphealth.model.HealthModel;
import cn.stj.fphealth.model.HealthModelImpl;
import cn.stj.fphealth.tcp.mina.TcpProtocol;
import cn.stj.fphealth.util.LogUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author jackey@20160806
 */
public class HealthService extends Service {

    private final IBinder mBinder = new ServiceBinder();
    private Context mContext = null;
    private HealthManager mHealthManager;
    // Async handler
    private HealthServiceHandler mHealthServiceHandler;
    private ArrayList<Record> mRecords = new ArrayList<Record>();
    private HealthModel mHealthModel;
    public static final String CACHE_HEART_RATES = "cache_heart_rates";
    public static final String HEART_RATE = "heart_rate";
    private TelephonyManager mTm;
    private int mCurrPhoneState;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class ServiceBinder extends Binder {

        public HealthService getService() {
            return HealthService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        mHealthManager = new HealthManager(this);

        HandlerThread handlerThread = new HandlerThread("HealthServiceThread");
        handlerThread.start();
        mHealthServiceHandler = new HealthServiceHandler(handlerThread.getLooper());

        mHealthModel = new HealthModelImpl(mContext);
        
        mTm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        mTm.listen(new MyPhoneStateListener(),  
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    /**
     * The background handler
     */
    class HealthServiceHandler extends Handler {
        public HealthServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle;
            switch (msg.what) {
            // heart rate
                case HealthListener.MSGID_DETECT_HEARTRATE:
                    bundle = msg.getData();
                    HeartRate heartRate = bundle.getParcelable(HEART_RATE);
                    detectHeartRate(heartRate);
//                    mHealthModel.sendUserInfoGet(1355);
//                    RemindInfo remindInfo = new RemindInfo();
//                    remindInfo.setTime(System.currentTimeMillis());
//                    mHealthModel.sendRemindSuccessNotice(remindInfo);
//                    mHealthModel.sendLocationUpload();
//                	mHealthModel.sendLocationQuery();
//                	mHealthModel.sendLocationTrrige();
//                	mHealthModel.sendConstactsQuery();
//                	mHealthModel.sendMonitorFinish();
                    break;
                // device bind
                case HealthListener.MSGID_DEVICE_BIND:
                    bundle = msg.getData();
                    mHealthModel.sendDeviceBind(bundle.getInt(Constants.DEVICEBIND_STATUS),
                            new HealthListener() {

                                @Override
                                public void onCallBack(Bundle bundle) {
                                    bundle.putInt(HealthListener.CALLBACK_FLAG,
                                            HealthListener.MSGID_DEVICE_BIND);
                                    notifyActivityStateChanged(bundle);
                                }
                            });
                    break;
                // device hello
                case HealthListener.MSGID_DEVICE_HELLO:
                    mHealthModel.sendDeviceHello(new HealthListener() {

                        @Override
                        public void onCallBack(Bundle bundle) {
                            bundle.putInt(HealthListener.CALLBACK_FLAG,
                                    HealthListener.MSGID_DEVICE_HELLO);
                            notifyActivityStateChanged(bundle);
                        }
                    });
                    break;
                default:
                    break;
            }
        }

    }

    public void registerHealthListener(HealthListener callback) {
        synchronized (mRecords) {
            // register callback in AudioProfileService, if the callback is
            // exist, just replace the event.
            Record record = null;
            int hashCode = callback.hashCode();
            final int n = mRecords.size();
            for (int i = 0; i < n; i++) {
                record = mRecords.get(i);
                if (hashCode == record.mHashCode) {
                    return;
                }
            }
            record = new Record();
            record.mHashCode = hashCode;
            record.mCallback = callback;
            mRecords.add(record);
        }
    }

    /**
     * Health listener record
     */
    private static class Record {
        int mHashCode; // hash code
        HealthListener mCallback; // call back
    }

    public void detectHeartRateAsync() {
        mHealthServiceHandler.removeMessages(HealthListener.MSGID_DETECT_HEARTRATE);
        Bundle bundle = new Bundle();
        Message msg = mHealthServiceHandler.obtainMessage(HealthListener.MSGID_DETECT_HEARTRATE);
        msg.setData(bundle);
        mHealthServiceHandler.sendMessage(msg);
    }

    private void detectHeartRate(HeartRate heartRate) {
//        List<HeartRate> heartRates = mHealthManager.detectHeartRate();
        Bundle bundle = new Bundle();
        bundle.putInt(HealthListener.CALLBACK_FLAG, HealthListener.MSGID_DETECT_HEARTRATE);
//        notifyActivityStateChanged(bundle);
        List<HeartRate> heartRates = new ArrayList<HeartRate>();
        heartRates.add(heartRate);
        mHealthModel.sendBloodPressueUpload(heartRates);
    }
    
    public void sendHeartRateAsync(HeartRate heartRate) {
        mHealthServiceHandler.removeMessages(HealthListener.MSGID_DETECT_HEARTRATE);
        Bundle bundle = new Bundle();
        bundle.putParcelable(HEART_RATE, heartRate);
        Message msg = mHealthServiceHandler.obtainMessage(HealthListener.MSGID_DETECT_HEARTRATE);
        msg.setData(bundle);
        mHealthServiceHandler.sendMessage(msg);
    }

    public void sendDeviceBindAsync(int status) {
        mHealthServiceHandler.removeMessages(HealthListener.MSGID_DEVICE_BIND);
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.DEVICEBIND_STATUS, status);
        Message msg = mHealthServiceHandler.obtainMessage(HealthListener.MSGID_DEVICE_BIND);
        msg.setData(bundle);
        mHealthServiceHandler.sendMessage(msg);
    }

    /**
     * Call back from service to activity
     * 
     * @param bundle The message to activity
     */
    private void notifyActivityStateChanged(Bundle bundle) {
        if (!mRecords.isEmpty()) {
            synchronized (mRecords) {
                Iterator<Record> iterator = mRecords.iterator();
                while (iterator.hasNext()) {
                    Record record = (Record) iterator.next();

                    HealthListener listener = record.mCallback;

                    if (listener == null) {
                        iterator.remove();
                        return;
                    }

                    listener.onCallBack(bundle);
                }
            }
        }
    }

    /**
     * Unregister Health listener
     * 
     * @param callback FM Radio listener
     */
    public void unregisterHealthListener(HealthListener callback) {
        remove(callback.hashCode());
    }

    /**
     * Remove call back according hash code
     * 
     * @param hashCode The call back hash code
     */
    private void remove(int hashCode) {
        synchronized (mRecords) {
            Iterator<Record> iterator = mRecords.iterator();
            while (iterator.hasNext()) {
                Record record = (Record) iterator.next();
                if (record.mHashCode == hashCode) {
                    iterator.remove();
                }
            }
        }
    }

    public void sendDeviceHelloAsync() {
        mHealthServiceHandler.removeMessages(HealthListener.MSGID_DEVICE_HELLO);
        Bundle bundle = new Bundle();
        Message msg = mHealthServiceHandler.obtainMessage(HealthListener.MSGID_DEVICE_HELLO);
        msg.setData(bundle);
        mHealthServiceHandler.sendMessage(msg);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            final List<HeartRate> cacheHeartRates = intent.getParcelableArrayListExtra(CACHE_HEART_RATES);
            if (cacheHeartRates != null && cacheHeartRates.size() > 0) {
                new Thread(new Runnable() {
                    
                    @Override
                    public void run() {
                        mHealthModel.sendBloodPressueUpload(cacheHeartRates);
                    }
                }).start();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
    
    class MyPhoneStateListener extends PhoneStateListener {  
    	  
        @Override  
        public void onCallStateChanged(int state, String incomingNumber) {  
            switch(state) {  
            case TelephonyManager.CALL_STATE_IDLE: //空闲 
            	if(mCurrPhoneState == TelephonyManager.CALL_STATE_OFFHOOK){
            		mHealthModel.sendMonitorFinish();
            	}
            	mCurrPhoneState = TelephonyManager.CALL_STATE_IDLE;
                break;  
            case TelephonyManager.CALL_STATE_RINGING: //来电 
            	mCurrPhoneState = TelephonyManager.CALL_STATE_RINGING;
                break;  
            case TelephonyManager.CALL_STATE_OFFHOOK: //摘机（正在通话中）  
            	if(mCurrPhoneState == TelephonyManager.CALL_STATE_IDLE){
          		    mHealthModel.sendMonitor();
          	    }
            	mCurrPhoneState = TelephonyManager.CALL_STATE_OFFHOOK;
                break;  
            }  
        }     
    }  
    
}
