
package cn.stj.fphealth.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.app.FPHealthApplication;
import cn.stj.fphealth.entity.HeartRate;
import cn.stj.fphealth.service.BootService;
import cn.stj.fphealth.service.HealthService;
import cn.stj.fphealth.util.LogUtil;
import cn.stj.fphealth.util.NetworkUtil;
import cn.stj.fphealth.util.PreferencesUtils;

/**
 * @author hhj@20160823
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    private boolean mIsDeviceBind;
    private boolean mIsBoot;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (NetworkUtil.checkNetwork(context)) {
            mIsDeviceBind = PreferencesUtils.getBoolean(context, Constants.IS_CONFIRM_BIND, false);
            LogUtil.i("debug", "=====@@@@@@@@@==NetworkStateReceiver======onReceive=========mIsDeviceBind:" + mIsDeviceBind
                    + "====mHasQrcodeImg:" + FPHealthApplication.mHasQrcodeImg + 
                    "===mIsShowConfirmBind:" + FPHealthApplication.mIsShowConfirmBind);
            if(!mIsDeviceBind && !FPHealthApplication.mHasQrcodeImg && !FPHealthApplication.mIsShowConfirmBind){
                context.startService(new Intent(context,BootService.class)); 
            }
        }
        
    }

}
