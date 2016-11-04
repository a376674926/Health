
package cn.stj.fphealth.util;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;

import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.entity.Mobile;
import cn.stj.fphealth.entity.MobileBaseStation;
import cn.stj.fphealth.entity.RemindInfo;
import cn.stj.fphealth.entity.Wifi;
import cn.stj.fphealth.entity.WifiHotspot;
import cn.stj.fphealth.http.DeviceInitResponse;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * json util Created by hhj@20160810.
 */
public class JsonUtil {

    private static Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            // Filtered off the field name contains the "baseObjId"
            return f.getName().contains("baseObjId");
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    }).setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    public static Gson getGson() {
        return gson;
    }

    /**
     * Use Gson analyzed
     * 
     * @param jsonString
     * @param cls
     * @param <T>
     * @return Objects obtained after parsing
     */
    public static <T> T getData(String jsonString, Class<T> cls) {
        T t = null;
        System.out.println("jsonString =" + jsonString + " cls = " + cls);
        try {
            t = gson.fromJson(jsonString, cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    /**
     * Use Gson analyzed
     * 
     * @param jsonString
     * @param cls
     * @param <T>
     * @return object list
     */
    public static <T> List<T> getDatas(String jsonString, Class<T> cls) {
        List<T> list = new ArrayList<T>();
        try {
            Type listType = type(List.class, cls);
            list = gson.fromJson(jsonString, listType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    static ParameterizedType type(final Class raw, final Type... args) {
        return new ParameterizedType() {
            public Type getRawType() {
                return raw;
            }

            public Type[] getActualTypeArguments() {
                return args;
            }

            public Type getOwnerType() {
                return null;
            }

        };
    }

    public static String getWifiJson(List<Wifi> wifis) {

        JSONArray jsonArray = new JSONArray();
        try {
            for (int i = 0; i < wifis.size(); i++) {
                Wifi wifi = wifis.get(i);
                JSONObject jsonObject = new JSONObject();
                List<WifiHotspot> wifiHotspots = wifi.getWifiHotspots();
                for (int j = 0; j < wifiHotspots.size(); j++) {
                    WifiHotspot wifiHotspot = wifiHotspots.get(j);
                    jsonObject.put("mac" + (j + 1), wifiHotspot.getMac());
                    jsonObject.put("macName" + (j + 1), wifiHotspot.getMacName());
                    jsonObject.put("signal" + (j + 1), wifiHotspot.getSignal());
                }
                jsonObject.put("time", wifi.getTime());
                jsonArray.put(jsonObject);
            }
            return jsonArray.toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }

        return "";
    }
    
    /**
     * Get mobile phone base station information
     * 
     * @throws JSONException
     */
    public static String getGSMCellLocationJson(List<Mobile> mobiles) {
 
        try {
            JSONArray jsonArray = new JSONArray();
            for(int i = 0;i < mobiles.size();i++){
                Mobile mobile = mobiles.get(i);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("serverIp", mobile.getServerIp());
                jsonObject.put("network", mobile.getNetwork());
                jsonObject.put("mcc", mobile.getMcc());
                jsonObject.put("mnc", mobile.getMnc());
                jsonObject.put("time", mobile.getTime());
                List<MobileBaseStation> mobileBaseStations = mobile.getMobileBaseStations();
                if (mobileBaseStations != null && mobileBaseStations.size() > 0) {
                    for (int j = 0; j < mobileBaseStations.size(); j++) {
                        MobileBaseStation mobileBaseStation = mobileBaseStations.get(j);
                        jsonObject.put("lac" + (j + 1), mobileBaseStation.getLac());
                        jsonObject.put("ci" + (j + 1), mobileBaseStation.getCi());
                        jsonObject.put("rssi" + (j + 1), TextUtils.isEmpty(mobileBaseStation.getRssi())?"":Integer.valueOf(mobileBaseStation.getRssi()));
                    }
                }
                jsonArray.put(jsonObject);
            }
            
            return jsonArray.toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }
    
    /**
     * get all device parameters string
     * @return
     */
    public static String getAllDeviceParams(Context context){
        String allParams = "" ;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.DEVICE_PARAM.COMMON, PreferencesUtils.getString(context, Constants.DEVICE_PARAM.COMMON_CODE));
            jsonObject.put(Constants.DEVICE_PARAM.CONNECT, PreferencesUtils.getString(context, Constants.DEVICE_PARAM.CONNECT_CODE));
            jsonObject.put(Constants.DEVICE_PARAM.FREQUENCY, PreferencesUtils.getString(context, Constants.DEVICE_PARAM.FREQUENCY_CODE));
            jsonObject.put(Constants.DEVICE_PARAM.HEALTH, PreferencesUtils.getString(context, Constants.DEVICE_PARAM.HEALTH_CODE));
            allParams = jsonObject.toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return allParams;
    }

}
