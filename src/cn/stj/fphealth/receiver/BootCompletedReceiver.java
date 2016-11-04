package cn.stj.fphealth.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;

import java.sql.Date;
import java.util.Calendar;

import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.app.FPHealthApplication;
import cn.stj.fphealth.service.BootService;
import cn.stj.fphealth.util.DatetimeUtil;
import cn.stj.fphealth.util.LogUtil;
import cn.stj.fphealth.util.NetworkUtil;
import cn.stj.fphealth.util.PreferencesUtils;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.i("debug", "=====BootCompletedReceiver==========onReceive=====");
        long stepLastTime = PreferencesUtils.getLong(FPHealthApplication.getInstance(), Constants.KEY_STEP_TIME, System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE), 0, 0, 0);
        String currentTime = DatetimeUtil.format(calendar.getTimeInMillis(), "yyyy/MM/dd HH:mm:ss");
        Calendar stepLastCalendar = Calendar.getInstance();
        stepLastCalendar.setTimeInMillis(stepLastTime);
        stepLastCalendar.set(stepLastCalendar.get(Calendar.YEAR), stepLastCalendar.get(Calendar.MONTH),
                stepLastCalendar.get(Calendar.DATE), 0, 0, 0);
        String stepTime = DatetimeUtil.format(stepLastCalendar.getTimeInMillis(), "yyyy/MM/dd HH:mm:ss");
        if(!currentTime.equals(stepTime)){//不同日期，需要清零
            PreferencesUtils.putInt(FPHealthApplication.getInstance(), Constants.KEY_TOTAL_STEP, 0);
        }
    }

}
