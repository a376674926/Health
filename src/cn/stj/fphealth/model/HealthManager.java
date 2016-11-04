
package cn.stj.fphealth.model;

import android.content.Context;
import android.util.Log;

import org.litepal.crud.DataSupport;

import cn.stj.fphealth.entity.HeartRate;
import cn.stj.fphealth.util.LogUtil;

import java.util.List;
import java.util.Random;

/**
 * @author hhj@20160804
 */
public class HealthManager {

    private Context mContext;

    public HealthManager(Context context) {
        mContext = context;
    }

    public List<HeartRate> detectHeartRate() {
        long fromTime = System.currentTimeMillis();
        try {
            Thread.sleep(2 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long toTime = System.currentTimeMillis();
        List<HeartRate> cacheHeartRates = DataSupport.findAll(HeartRate.class);
        Random random = new Random();
        HeartRate newHeartRate = new HeartRate();
        newHeartRate.setFromTime(fromTime + "");
        newHeartRate.setToTime(toTime + "");
        newHeartRate.setHigh(random.nextInt(10) + 100 + "");
        newHeartRate.setLow(random.nextInt(10) + 90 + "");
        newHeartRate.setRate(random.nextInt(10) + 70 + "");
        cacheHeartRates.add(newHeartRate);
        return cacheHeartRates;
    }

}
