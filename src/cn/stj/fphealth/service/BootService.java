package cn.stj.fphealth.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.app.Utils;
import cn.stj.fphealth.http.DeviceInitResponse;
import cn.stj.fphealth.http.DeviceQrcodeResponse;
import cn.stj.fphealth.http.DeviceSoftwareResponse;
import cn.stj.fphealth.http.RequestListener;
import cn.stj.fphealth.http.ServerIpResponse;
import cn.stj.fphealth.http.VolleyClient;
import cn.stj.fphealth.util.FileUtil;
import cn.stj.fphealth.util.LogUtil;
import cn.stj.fphealth.util.PreferencesUtils;

/**
 * boot server
 * @author hhj@20160907 
 *
 */
public class BootService extends Service{
    
    private int mRetryCount;
    private Context mContext;
    private final IBinder mBinder = new ServiceBinder();
    private boolean mIsDeviceBind;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class ServiceBinder extends Binder {

        public BootService getService() {
            return BootService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        LogUtil.i("debug", "======BootService======@@@@@@@===onCreate=======");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.i("debug", "======BootService=====@@@@@@@@@@====onStartCommand=======");
        mIsDeviceBind = PreferencesUtils.getBoolean(mContext, Constants.IS_CONFIRM_BIND, false);
        if(!mIsDeviceBind){
            String serverIp = PreferencesUtils.getString(this, Constants.SERVER_IP);
            if(TextUtils.isEmpty(serverIp)){
                getServerIpByDomain();
            }else{
                mRetryCount = PreferencesUtils.getInt(this, Constants.DEVICE_PARAM.RETRY,2);
                networkRetry();
            } 
        }
        return super.onStartCommand(intent, flags, startId);
    }
    
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            uploadDeviceSoftware(Utils.getVersionCode(mContext)+"");
            getDeviceQrcode();
            getDeviceInit();
        };
    };
    
    public void getServerIpByDomain() {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("http://").append(Constants.HTTP.SERVER_DOMAIN).append(":").
        append(Constants.HTTP.SERVER_PORT).append(Constants.HTTP.SERVER_IP_PATH);
        sBuffer.append("?").append("key=").append(Constants.HTTP.SERVER_KEY);
        sBuffer.append("&").append("imei=").append(Utils.getImei(this));
        LogUtil.i("debug", "===BootService======getServerIpByDomain========URL:" + sBuffer.toString());
        VolleyClient.getInstance().serverIpGet(sBuffer.toString(), new RequestListener<ServerIpResponse>() {

            @Override
            public void onRequestSuccess(ServerIpResponse response) {
                // TODO Auto-generated method stub
                LogUtil.i("debug", "=====serverIpGet======onRequestSuccess========status:"
                        + response.getStatus() + " ip:" + response.getIp());
                PreferencesUtils.putString(mContext, Constants.SERVER_IP, response.getIp());
                Message msg = mHandler.obtainMessage();
                mHandler.sendMessage(msg);
            }

            @Override
            public void onRequestFail(int code, String msg) {
                // TODO Auto-generated method stub
                LogUtil.i("debug", "=====serverIpGet======onRequestFail========code:" + code
                        + " msg:" + msg);
            }

            @Override
            public void onRequestError(int code, String msg) {
                // TODO Auto-generated method stub
                LogUtil.i("debug", "=====serverIpGet======onRequestError========code:" + code
                        + " msg:" + msg);
            }

            @Override
            public void onPreRequest() {
                // TODO Auto-generated method stub

            }
        });
    }
    
    public void networkRetry() {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append(Utils.getServerUrl(mContext,Constants.HTTP.DEVICE_SOFTWARE_PATH));
        sBuffer.append("&").append("version=").append(Utils.getVersionCode(mContext)+"");
        VolleyClient.getInstance().deviceSoftwareGet(sBuffer.toString(), new RequestListener<DeviceSoftwareResponse>() {

            @Override
            public void onRequestSuccess(DeviceSoftwareResponse response) {
                // TODO Auto-generated method stub
                LogUtil.i("debug", "=====deviceSoftware=======onRequestSuccess======status:"+ response.getStatus());
                mHandler.sendMessage(mHandler.obtainMessage());
            }

            @Override
            public void onRequestFail(int code, String msg) {
                // TODO Auto-generated method stub
                LogUtil.i("debug", "=====deviceSoftware======onRequestFail========code:" + code
                        + " msg:" + msg);
                
            }

            @Override
            public void onRequestError(int code, String msg) {
                // TODO Auto-generated method stub
                LogUtil.i("debug", "=====deviceSoftware======onRequestError========code:" + code
                        + " msg:" + msg);
//                isRetrySuccess = false;
                mRetryCount--;
                if(mRetryCount > 0){
                    networkRetry();
                }else{
                    getServerIpByDomain();
                }
            }

            @Override
            public void onPreRequest() {
                // TODO Auto-generated method stub

            }
        });
    }
    
    public void uploadDeviceSoftware(String version){
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append(Utils.getServerUrl(mContext,Constants.HTTP.DEVICE_SOFTWARE_PATH));
        sBuffer.append("&").append("version=").append(version);
        VolleyClient.getInstance().deviceSoftwareGet(sBuffer.toString(), new RequestListener<DeviceSoftwareResponse>() {

            @Override
            public void onRequestSuccess(DeviceSoftwareResponse response) {
                // TODO Auto-generated method stub
                LogUtil.i("debug", "=====deviceSoftware=======onRequestSuccess======status:"+ response.getStatus());
            }

            @Override
            public void onRequestFail(int code, String msg) {
                // TODO Auto-generated method stub
                LogUtil.i("debug", "=====deviceSoftware======onRequestFail========code:" + code
                        + " msg:" + msg);
            }

            @Override
            public void onRequestError(int code, String msg) {
                // TODO Auto-generated method stub
                LogUtil.i("debug", "=====deviceSoftware======onRequestError========code:" + code
                        + " msg:" + msg);
            }

            @Override
            public void onPreRequest() {
                // TODO Auto-generated method stub

            }
        });
        
    }
    
    public void getDeviceQrcode(){
        VolleyClient.getInstance().deviceQrcodeGet(Utils.getServerUrl(mContext,Constants.HTTP.DEVICE_QRCODE_PATH), new RequestListener<DeviceQrcodeResponse>() {

            @Override
            public void onRequestSuccess(DeviceQrcodeResponse response) {
                // TODO Auto-generated method stub
                LogUtil.i("debug", "=====deviceQrcodeGet=======onRequestSuccess======:");
                if(response.isExitQrcodeStream()){
                    Bitmap qrCodeBitmap = BitmapFactory.decodeByteArray(response.getOstream(), 0, response.getOstream().length);
                    if(qrCodeBitmap != null){
                        FileUtil.saveQrCodePicture(mContext, qrCodeBitmap);
                    }
                }else{
                    LogUtil.i("debug",
                            "=====getDeviceQrcode=======onRequestSuccess======status:" + response.getStatus() 
                            + "---prompt:" + response.getPrompt());
                }
                
            }

            @Override
            public void onRequestFail(int code, String msg) {
                // TODO Auto-generated method stub
                LogUtil.i("debug", "=====deviceQrcodeGet======onRequestFail========code:" + code
                        + " msg:" + msg);
            }

            @Override
            public void onRequestError(int code, String msg) {
                // TODO Auto-generated method stub
                LogUtil.i("debug", "=====deviceQrcodeGet======onRequestError========code:" + code
                        + " msg:" + msg);
            }

            @Override
            public void onPreRequest() {
                // TODO Auto-generated method stub

            }
        });
        
    }
    
    public void getDeviceInit(){
        VolleyClient.getInstance().deviceInitGet(Utils.getServerUrl(mContext,Constants.HTTP.DEVICE_INIT_PATH), new RequestListener<DeviceInitResponse>() {

            @Override
            public void onRequestSuccess(DeviceInitResponse response) {
                // TODO Auto-generated method stub
                LogUtil.i("debug", "=====deviceInitGet=======onRequestSuccess======response:" + response.toString());
                Utils.saveDeviceParams(mContext,response);
            }

            @Override
            public void onRequestFail(int code, String msg) {
                // TODO Auto-generated method stub
                LogUtil.i("debug", "=====deviceInitGet======onRequestFail========code:" + code
                        + " msg:" + msg);
            }

            @Override
            public void onRequestError(int code, String msg) {
                // TODO Auto-generated method stub
                LogUtil.i("debug", "=====deviceInitGet======onRequestError========code:" + code
                        + " msg:" + msg);
            }

            @Override
            public void onPreRequest() {
                // TODO Auto-generated method stub

            }
        });
        
    }
}
