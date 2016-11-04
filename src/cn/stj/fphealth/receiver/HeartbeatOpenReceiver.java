
package cn.stj.fphealth.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.stj.fphealth.activity.HealthMainActivity;
import cn.stj.fphealth.app.Utils;
import cn.stj.fphealth.service.HeartbeatService;
import cn.stj.fphealth.util.LogUtil;

/**
 * @author hhj@20160816
 */
public class HeartbeatOpenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.i("debug", "===========HeartbeatOpenReceiver========onReceive==" + Utils.isTopActivy(context, HealthMainActivity.class.getName()));
        Intent heartBeatIntent = new Intent(context, HealthMainActivity.class);
        heartBeatIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        heartBeatIntent.putExtra("oneKeyOpen", true);
        context.startActivity(heartBeatIntent);
    }

}
