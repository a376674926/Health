
package cn.stj.fphealth.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;

import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.app.FPHealthApplication;
import cn.stj.fphealth.app.Utils;
import cn.stj.fphealth.db.Database;
import cn.stj.fphealth.db.LitepalDatabaseImpl;
import cn.stj.fphealth.entity.LocationInfo;
import cn.stj.fphealth.entity.Mobile;
import cn.stj.fphealth.entity.MobileBaseStation;
import cn.stj.fphealth.entity.RemindInfo;
import cn.stj.fphealth.entity.Wifi;
import cn.stj.fphealth.entity.WifiHotspot;
import cn.stj.fphealth.model.HealthMinaModelImpl;
import cn.stj.fphealth.model.HealthModel;
import cn.stj.fphealth.model.HealthModelImpl;
import cn.stj.fphealth.receiver.LocationUploadReceiver;
import cn.stj.fphealth.util.JsonUtil;
import cn.stj.fphealth.util.LogUtil;
import cn.stj.fphealth.util.NetworkUtil;
import cn.stj.fphealth.util.PreferencesUtils;

import org.json.JSONException;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LocationUploadService extends Service {

    public static final String REPEAT_FLAG = "repeat_flag";
    private static final int REPEAT_COLLECT = 10;
    private static final int REPEAT_UPLOAD = 11;
    private HealthModel mHealthModel;
    private Context mContext;
    private int mRepeatFlag;
    private AlarmManager mAlarmManager;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mContext = this;
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mHealthModel = new HealthMinaModelImpl(this);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            mRepeatFlag = intent.getIntExtra(REPEAT_FLAG, -1);
            LogUtil.i("debug", "=========LocationUploadService=====onStartCommand======mRepeatFlag:" + mRepeatFlag);
            switch (mRepeatFlag) {
                case REPEAT_COLLECT:
                    repeatCollect(REPEAT_COLLECT);
                    collectLocationData();
                    break;
                case REPEAT_UPLOAD:
                    repeatUpload(REPEAT_UPLOAD);
                    LogUtil.i("debug", "=========LocationUploadService=====onStartCommand======REPEAT_UPLOAD");
                    mHealthModel.sendLocationUpload();
                    break;
                default:
                    repeatCollect(REPEAT_COLLECT);
                    repeatUpload(REPEAT_UPLOAD);
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
    
    /**
     * 采集定位数据
     */
    public void collectLocationData(){
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                LogUtil.i("debug", "========LocationUploadService======@@@@@@@=======collect");
                // 采集数据并保存
                // GPS
                LocationInfo locationInfo = Utils.getGPSLocation(mContext);
                if(locationInfo != null){
                    LogUtil.i("debug", "======LocationUploadService======collect======GPS:" + locationInfo.toString());
                    locationInfo.save();
                }

                // Wifi
                Wifi wifi = Utils.getWifiInfo(mContext);
                List<WifiHotspot> wifiHotspots = wifi.getWifiHotspots();
                if(wifiHotspots != null && wifiHotspots.size() > 0){
                    DataSupport.saveAll(wifiHotspots);
                    wifi.save();
                }

                // mobile
                Mobile mobile = Utils.getMobileBaseStation(mContext);
                if(mobile != null){
                    List<MobileBaseStation> mobileBaseStations = mobile.getMobileBaseStations();
                    if(mobileBaseStations != null && mobileBaseStations.size() > 0){
                        DataSupport.saveAll(mobileBaseStations);
                        mobile.save();  
                    }
                }
            }
        }).start();
    }

    private void repeatCollect(int repeatCollect) {
        scheduleAlarms(this, repeatCollect);
    }
    
    private void repeatUpload(int repeatUpload) {
        scheduleAlarms(this, repeatUpload);
    }
    
    public void scheduleAlarms(Context context, int requestCode) { 
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // 选择提醒的时间
        long remindTime = 0l;
        if(requestCode == REPEAT_COLLECT){
            long collectFrequency = PreferencesUtils.getLong(this,
                    Constants.DEVICE_PARAM.LOCATION_GFREQ, 60) * 1000;
            long collectAtTime = System.currentTimeMillis() + collectFrequency;
            remindTime = collectAtTime;
        }else if(requestCode == REPEAT_UPLOAD){
            long uploadFrequency = PreferencesUtils.getLong(this,
                    Constants.DEVICE_PARAM.LOCATION_UFREQ, 300) * 1000;
            long uploadAtTime = System.currentTimeMillis() + uploadFrequency;
            remindTime = uploadAtTime;
        }
        Intent intent = new Intent(this, LocationUploadReceiver.class);
        intent.putExtra(REPEAT_FLAG, requestCode);
        PendingIntent locationIntent = PendingIntent.getBroadcast(context, requestCode,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(locationIntent);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, remindTime, locationIntent);
    }
    
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        LogUtil.i("debug", "=====LocationUploadService=========onDestroy=====");
    }
    
}
