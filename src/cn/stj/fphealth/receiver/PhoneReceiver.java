package cn.stj.fphealth.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import java.util.ArrayList;

import cn.stj.fphealth.service.HealthService;
import cn.stj.fphealth.util.LogUtil;

public class PhoneReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.i("debug", "=======PhoneReceiver=======onReceive=====action:" + intent.getAction());
        if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            LogUtil.i("debug", "call OUT:" + phoneNumber); 
        }
    }

}
