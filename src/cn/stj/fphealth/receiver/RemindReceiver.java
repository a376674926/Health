package cn.stj.fphealth.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.entity.RemindInfo;
import cn.stj.fphealth.service.RemindService;
import cn.stj.fphealth.util.LogUtil;
import cn.stj.fphealth.util.PreferencesUtils;

public class RemindReceiver extends BroadcastReceiver {
    public static long lastRemindTime;
    private boolean mIsDeviceBind;
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.i("debug", "=======RemindReceiver==========");
        mIsDeviceBind = PreferencesUtils.getBoolean(context, Constants.IS_CONFIRM_BIND, false);
        if(mIsDeviceBind){//解绑情况下停止提醒
            RemindInfo remindInfo = intent.getParcelableExtra(RemindService.REMIND_INFO);
            lastRemindTime = System.currentTimeMillis();
            Intent remindIntent = new Intent(context,RemindService.class);
            remindIntent.putExtra(RemindService.REMIND_INFO, remindInfo);
            remindIntent.putExtra(RemindService.REMIND_FLAG, RemindService.REMIND_REPEAT);
            LogUtil.i("debug", "=======RemindReceiver==========remindInfo:" + remindInfo.toString());
            context.startService(remindIntent); 
        }
    }
}
