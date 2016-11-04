
package cn.stj.fphealth.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;

import java.sql.Date;

import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.service.HeartbeatService;
import cn.stj.fphealth.service.LocationUploadService;
import cn.stj.fphealth.service.StepService;
import cn.stj.fphealth.util.DatetimeUtil;
import cn.stj.fphealth.util.LogUtil;
import cn.stj.fphealth.util.PreferencesUtils;

/**
 * @author hhj@20160816
 */
public class StepReceiver extends BroadcastReceiver {
    private boolean mIsDeviceBind;

    @Override
    public void onReceive(Context context, Intent intent) {
        mIsDeviceBind = PreferencesUtils.getBoolean(context, Constants.IS_CONFIRM_BIND, false);
        long remindTime = intent.getLongExtra("remindTime", 0l);
        if (mIsDeviceBind) {// 解绑情况下停止计步上传
            int repeatFlag = intent.getIntExtra(StepService.REPEAT_FLAG, -1);
//            LogUtil.i("debug", "=======StepReceiver=====@@@===onReceive====repeatFlag:" + repeatFlag + "remindTime:" + DatetimeUtil.format(remindTime, "yyyy-MM-dd HH:mm:ss"));
            Intent stepIntent = new Intent(context, StepService.class);
            stepIntent.putExtra(LocationUploadService.REPEAT_FLAG, repeatFlag);
            context.startService(stepIntent);
        }
    }

}
