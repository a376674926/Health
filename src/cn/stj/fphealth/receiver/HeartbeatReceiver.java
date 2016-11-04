
package cn.stj.fphealth.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.stj.fphealth.service.HeartbeatService;

/**
 * @author hhj@20160816
 */
public class HeartbeatReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent heartBeatIntent = new Intent(context, HeartbeatService.class);
        context.startService(heartBeatIntent);
    }

}
