
package cn.stj.fphealth.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.service.HeartbeatService;
import cn.stj.fphealth.service.LocationUploadService;
import cn.stj.fphealth.util.LogUtil;
import cn.stj.fphealth.util.PreferencesUtils;

/**
 * @author hhj@20160816
 */
public class LocationUploadReceiver extends BroadcastReceiver {
    private boolean mIsDeviceBind;
    @Override
    public void onReceive(Context context, Intent intent) {
        mIsDeviceBind = PreferencesUtils.getBoolean(context, Constants.IS_CONFIRM_BIND, false);
        if(mIsDeviceBind){//解绑情况下停止定位数据采集以及上传
            int repeatFlag = intent.getIntExtra(LocationUploadService.REPEAT_FLAG, -1);
            Intent locationIntent = new Intent(context, LocationUploadService.class);
            locationIntent.putExtra(LocationUploadService.REPEAT_FLAG, repeatFlag);
            context.startService(locationIntent);  
        }
    }

}
