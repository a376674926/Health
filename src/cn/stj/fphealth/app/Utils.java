
package cn.stj.fphealth.app;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
//hhj@20161022
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.widget.Toast;

import cn.stj.fphealth.R;
import cn.stj.fphealth.db.Database;
import cn.stj.fphealth.db.LitepalDatabaseImpl;
import cn.stj.fphealth.entity.LocationInfo;
import cn.stj.fphealth.entity.Mobile;
import cn.stj.fphealth.entity.MobileBaseStation;
import cn.stj.fphealth.entity.RemindInfo;
import cn.stj.fphealth.entity.Wifi;
import cn.stj.fphealth.entity.WifiHotspot;
import cn.stj.fphealth.http.DeviceDownloadResponse;
import cn.stj.fphealth.http.DeviceInitResponse;
import cn.stj.fphealth.http.DeviceQrcodeResponse;
import cn.stj.fphealth.http.RequestListener;
import cn.stj.fphealth.http.VolleyClient;
import cn.stj.fphealth.service.LocationUploadService;
import cn.stj.fphealth.service.RemindVoiceService;
import cn.stj.fphealth.service.StepDetector;
import cn.stj.fphealth.tcp.mina.TcpProtocol;
import cn.stj.fphealth.util.FileUtil;
import cn.stj.fphealth.util.JsonUtil;
import cn.stj.fphealth.util.LogUtil;
import cn.stj.fphealth.util.NetworkUtil;
import cn.stj.fphealth.util.PermissionsChecker;
import cn.stj.fphealth.util.PreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.security.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {
    private static final boolean WCN_DISABLED = SystemProperties.get("ro.wcn").equals("disabled");
    /** Broadcast intent action when the location mode is about to change. */
    private static final String MODE_CHANGING_ACTION =
            "com.android.settings.location.MODE_CHANGING";
    private static final String CURRENT_MODE_KEY = "CURRENT_MODE";
    private static final String NEW_MODE_KEY = "NEW_MODE";

    public static void saveDeviceParams(Context context,
            DeviceInitResponse deviceInitResponse) {
        saveDeviceCommonParams(context, deviceInitResponse.getCommon());
        saveDeviceConnectParams(context, deviceInitResponse.getConnect());
        saveDeviceFrequencyParams(context, deviceInitResponse.getFrequency());
        saveDeviceHealthParams(context, deviceInitResponse.getHealth());
    }

    public static void saveDeviceAllParams(Context context, String allParams) {
        PreferencesUtils.putString(context, Constants.DEVICE_PARAM.ALL_CODE,
                allParams);
        try {
            JSONObject jsonObject = new JSONObject(allParams);
            saveDeviceCommonParams(context,
                    jsonObject.getString(Constants.DEVICE_PARAM.COMMON));
            saveDeviceConnectParams(context,
                    jsonObject.getString(Constants.DEVICE_PARAM.CONNECT));
            saveDeviceFrequencyParams(context,
                    jsonObject.getString(Constants.DEVICE_PARAM.FREQUENCY));
            saveDeviceHealthParams(context,
                    jsonObject.getString(Constants.DEVICE_PARAM.HEALTH));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void saveDeviceConnectParams(Context context,
            String connectParams) {
        PreferencesUtils.putString(context,
                Constants.DEVICE_PARAM.CONNECT_CODE, connectParams);
        try {
            JSONObject connect = new JSONObject(connectParams);
            PreferencesUtils.putInt(context, Constants.DEVICE_PARAM.BEAT,
                    connect.getInt(Constants.DEVICE_PARAM.BEAT));
            PreferencesUtils.putInt(context, Constants.DEVICE_PARAM.TIMEOUT,
                    connect.getInt(Constants.DEVICE_PARAM.TIMEOUT));
            PreferencesUtils.putInt(context, Constants.DEVICE_PARAM.RETRY,
                    connect.getInt(Constants.DEVICE_PARAM.RETRY));
            PreferencesUtils.putInt(context, Constants.DEVICE_PARAM.RESTART,
                    connect.getInt(Constants.DEVICE_PARAM.RESTART));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PreferencesUtils.putString(context, Constants.DEVICE_PARAM.ALL_CODE,
                JsonUtil.getAllDeviceParams(context));
    }

    public static void saveDeviceCommonParams(Context context,
            String commonParams) {
        LogUtil.i("debug", "=====Utils===saveDeviceCommonParams=============commonParams:"
                + commonParams);
        PreferencesUtils.putString(context, Constants.DEVICE_PARAM.COMMON_CODE,
                commonParams);
        try {
            JSONObject common = new JSONObject(commonParams);
            PreferencesUtils.putInt(context, Constants.DEVICE_PARAM.BRIGHT,
                    common.getInt(Constants.DEVICE_PARAM.BRIGHT));
            PreferencesUtils.putInt(context, Constants.DEVICE_PARAM.SOUND,
                    common.getInt(Constants.DEVICE_PARAM.SOUND));
            PreferencesUtils.putInt(context, Constants.DEVICE_PARAM.SHAKE,
                    common.getInt(Constants.DEVICE_PARAM.SHAKE));
            PreferencesUtils.putInt(context, Constants.DEVICE_PARAM.QUIET,
                    common.getInt(Constants.DEVICE_PARAM.QUIET));
            JSONArray powerAutoArray = common
                    .getJSONArray(Constants.DEVICE_PARAM.POWER_AUTO);
            String bootTime = "";
            String offTime = "";
            LogUtil.i("debug",
                    "===========Utils===saveDeviceCommonParams===========powerAutoArray.length():"
                            + powerAutoArray.length());
            if (powerAutoArray.length() == 2) {
                bootTime = (String) powerAutoArray.get(0);
                offTime = (String) powerAutoArray.get(1);
            }
            PreferencesUtils.putString(context,
                    Constants.DEVICE_PARAM.BOOT_TIME, bootTime);
            PreferencesUtils.putString(context,
                    Constants.DEVICE_PARAM.OFF_TIME, offTime);
            PreferencesUtils.putInt(context,
                    Constants.DEVICE_PARAM.LOCATION_ONOFF,
                    common.getInt(Constants.DEVICE_PARAM.LOCATION_ONOFF));
            PreferencesUtils.putInt(context,
                    Constants.DEVICE_PARAM.HEART_ONOFF,
                    common.getInt(Constants.DEVICE_PARAM.HEART_ONOFF));
            PreferencesUtils.putInt(context, Constants.DEVICE_PARAM.WALK_ONOFF,
                    common.getInt(Constants.DEVICE_PARAM.WALK_ONOFF));
            PreferencesUtils.putInt(context,
                    Constants.DEVICE_PARAM.SITTING_ONOFF,
                    common.getInt(Constants.DEVICE_PARAM.SITTING_ONOFF));
            PreferencesUtils.putInt(context,
                    Constants.DEVICE_PARAM.SLEEP_ONOFF,
                    common.getInt(Constants.DEVICE_PARAM.SLEEP_ONOFF));
            PreferencesUtils.putString(context,
                    Constants.DEVICE_PARAM.TIMEZONE,
                    common.getString(Constants.DEVICE_PARAM.TIMEZONE));
            boolean locationOnoff = common.getInt(Constants.DEVICE_PARAM.LOCATION_ONOFF) == 1 ? true
                    : false;
            LogUtil.i("debug", "===Utils======saveDeviceCommonParams===@@@@@@@@@@===locationOnoff:"
                    + locationOnoff);
            // 打开设置中位置服务
            // hhj@20161022
            setLocationEnabled(locationOnoff);
            if (locationOnoff) {
                // 发起定位请求
                requestLocation();
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PreferencesUtils.putString(context, Constants.DEVICE_PARAM.ALL_CODE,
                JsonUtil.getAllDeviceParams(context));
    }

    public static void saveDeviceFrequencyParams(Context context,
            String frequencyParams) {
        PreferencesUtils.putString(context,
                Constants.DEVICE_PARAM.FREQUENCY_CODE, frequencyParams);
        try {
            JSONObject frequency = new JSONObject(frequencyParams);
            PreferencesUtils.putInt(context,
                    Constants.DEVICE_PARAM.SIGNAL_GFREQ,
                    frequency.getInt(Constants.DEVICE_PARAM.SIGNAL_GFREQ));
            PreferencesUtils.putInt(context,
                    Constants.DEVICE_PARAM.SIGNAL_UFREQ,
                    frequency.getInt(Constants.DEVICE_PARAM.SIGNAL_UFREQ));
            PreferencesUtils.putInt(context,
                    Constants.DEVICE_PARAM.BATTERY_GFREQ,
                    frequency.getInt(Constants.DEVICE_PARAM.BATTERY_GFREQ));
            PreferencesUtils.putInt(context,
                    Constants.DEVICE_PARAM.BATTERY_UFREQ,
                    frequency.getInt(Constants.DEVICE_PARAM.BATTERY_UFREQ));
            PreferencesUtils.putInt(context,
                    Constants.DEVICE_PARAM.HEART_GFREQ,
                    frequency.getInt(Constants.DEVICE_PARAM.HEART_GFREQ));
            PreferencesUtils.putInt(context,
                    Constants.DEVICE_PARAM.HEART_UFREQ,
                    frequency.getInt(Constants.DEVICE_PARAM.HEART_UFREQ));
            PreferencesUtils.putInt(context, Constants.DEVICE_PARAM.WALK_UFREQ,
                    frequency.getInt(Constants.DEVICE_PARAM.WALK_UFREQ));
            PreferencesUtils.putLong(context,
                    Constants.DEVICE_PARAM.LOCATION_GFREQ,
                    frequency.getLong(Constants.DEVICE_PARAM.LOCATION_GFREQ));
            PreferencesUtils.putLong(context,
                    Constants.DEVICE_PARAM.LOCATION_UFREQ,
                    frequency.getLong(Constants.DEVICE_PARAM.LOCATION_UFREQ));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PreferencesUtils.putString(context, Constants.DEVICE_PARAM.ALL_CODE,
                JsonUtil.getAllDeviceParams(context));
    }

    public static void saveDeviceHealthParams(Context context,
            String healthParams) {
        PreferencesUtils.putString(context, Constants.DEVICE_PARAM.HEALTH_CODE,
                healthParams);
        try {
            JSONObject health = new JSONObject(healthParams);
            PreferencesUtils.putInt(context, Constants.DEVICE_PARAM.HEIGHT,
                    health.getInt(Constants.DEVICE_PARAM.HEIGHT));
            PreferencesUtils.putInt(context, Constants.DEVICE_PARAM.WEIGHT,
                    health.getInt(Constants.DEVICE_PARAM.WEIGHT));
            PreferencesUtils.putInt(context,
                    Constants.DEVICE_PARAM.SITTING_TIME,
                    health.getInt(Constants.DEVICE_PARAM.SITTING_TIME));

            JSONArray sittingSpanArray = health
                    .getJSONArray(Constants.DEVICE_PARAM.SITTING_SPAN);
            String sittingSpanOne = "";
            String sittingSpanSecond = "";
            if (sittingSpanArray.length() == 2) {
                sittingSpanOne = (String) sittingSpanArray.get(0);
                sittingSpanSecond = (String) sittingSpanArray.get(1);
            }
            PreferencesUtils.putString(context,
                    Constants.DEVICE_PARAM.SITTING_SPAN_ONE, sittingSpanOne);
            PreferencesUtils.putString(context,
                    Constants.DEVICE_PARAM.SITTING_SPAN_SECOND,
                    sittingSpanSecond);
            JSONArray sleepSpanArray = health
                    .getJSONArray(Constants.DEVICE_PARAM.SLEEP_SPAN);
            String sleepSpanOne = "";
            String sleepSpanSecond = "";
            if (sleepSpanArray.length() == 2) {
                sleepSpanOne = (String) sleepSpanArray.get(0);
                sleepSpanSecond = (String) sleepSpanArray.get(1);
            }
            PreferencesUtils.putString(context,
                    Constants.DEVICE_PARAM.SLEEP_SPAN_ONE, sleepSpanOne);
            PreferencesUtils.putString(context,
                    Constants.DEVICE_PARAM.SLEEP_SPAN_SECOND, sleepSpanSecond);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PreferencesUtils.putString(context, Constants.DEVICE_PARAM.ALL_CODE,
                JsonUtil.getAllDeviceParams(context));
    }

    /**
     * the signal strength of the wifi list is to be sorted from strongest to
     * weakest
     * 
     * @param list
     */
    public static void sortByLevel(List<WifiHotspot> list) {
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                for (int j = i; j < list.size(); j++) {
                    if (list.get(i).getSignal() < list.get(j).getSignal()) {
                        WifiHotspot temp = null;
                        temp = list.get(i);
                        list.set(i, list.get(j));
                        list.set(j, temp);
                    }
                }
            }
        }
    }

    /**
     * collect wifi hot spots info
     * 
     * @param context
     * @return
     */
    public static Wifi getWifiInfo(Context context) {
        Wifi wifi = new Wifi();
        wifi.setTime(System.currentTimeMillis() + "");
        List<WifiHotspot> saveWifoHotspots = new ArrayList<WifiHotspot>();
        // 增加权限判断
        if (!PermissionsChecker.lacksPermission(Constants.ACCESS_COARSE_LOCATION_PERMISSION)) {
            List<WifiHotspot> wifoHotspots = NetworkUtil.getWifiHotspots(context);
            Utils.sortByLevel(wifoHotspots);
            int wifiHotspotsSize = wifoHotspots.size() < Constants.WIFI_HOTSPOT_NUMBER ? wifoHotspots
                    .size()
                    : Constants.WIFI_HOTSPOT_NUMBER;
            for (int i = 0; i < wifiHotspotsSize; i++) {
                saveWifoHotspots.add(wifoHotspots.get(i));
            }
        }
        wifi.setWifiHotspots(saveWifoHotspots);
        return wifi;
    }

    /**
     * the signal strength of the NeighboringCellInfo list is to be sorted from
     * strongest to weakest
     * 
     * @param list
     */
    public static void sortByRssi(List<NeighboringCellInfo> list) {
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                for (int j = i; j < list.size(); j++) {
                    if (list.get(i).getRssi() < list.get(j).getRssi()) {
                        NeighboringCellInfo temp = null;
                        temp = list.get(i);
                        list.set(i, list.get(j));
                        list.set(j, temp);
                    }
                }
            }
        }
    }

    /**
     * check whether activity is top or not
     * 
     * @param cmdName
     * @return
     */
    public static boolean isTopActivy(Context context, String className) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
        String classNameTemp = null;
        if (null != runningTaskInfos) {
            classNameTemp = runningTaskInfos.get(0).topActivity.getClassName();
        }

        if (null == classNameTemp)
            return false;
        return classNameTemp.equals(className);
    }

    public static String getServerUrl(Context context, String serverPath) {
        String serverIp = PreferencesUtils.getString(context,
                Constants.SERVER_IP);
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("http://").append(serverIp == null ? "" : serverIp)
                .append(":").append(Constants.HTTP.SERVER_PORT)
                .append(serverPath);
        sBuffer.append("?").append("key=").append(Constants.HTTP.SERVER_KEY);
        sBuffer.append("&").append("imei=").append(getImei(context));
        return sBuffer.toString();
    }

    public static String getServerResourceDownloadUrl(Context context,
            String resourceId) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("http://").append(Constants.HTTP.SERVER_DOMAIN)
                .append(":").append(Constants.HTTP.SERVER_PORT)
                .append(Constants.HTTP.RESOURCE_DOWNLOAD_PATH);
        sBuffer.append("?").append("token=")
                .append(PreferencesUtils.getString(context, Constants.TOKEN));
        sBuffer.append("&").append("resourceId=").append(resourceId);
        return sBuffer.toString();
    }

    public static double getVersionCode(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (pi != null) {
            LogUtil.i("debug", "=======Utils=====@@@@@==getVersionCode===" + pi.versionCode);
            return pi.versionCode;
        }
        return 1.0;
    }

    public static String getImei(Context context) {
        if (!PermissionsChecker.lacksPermission(Constants.READ_PHONE_STATE_PERMISSION)) {
            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            String imei = telephonyManager.getDeviceId();
            return imei;
        } else {
            return null;
        }

    }

    public static void playRemindVoice(Context context, RemindInfo remindInfo) {
        LogUtil.i("debug", "=====Util=============playRemindVoice=====");
        if (!PermissionsChecker.lacksPermission(Constants.WRITE_EXTERNAL_STORAGE_PERMISSION)) {
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                File remindVoiceFile = new File(Environment.getExternalStorageDirectory() +
                        "/health/remindVoice/" + remindInfo.getRemindId() + "_"
                        + remindInfo.getContent() + ".amr");
                if (remindVoiceFile.exists()) {
                    Intent playRemindVoiceIntent = new Intent();
                    playRemindVoiceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    playRemindVoiceIntent.setAction(android.content.Intent.ACTION_VIEW);
                    playRemindVoiceIntent.setDataAndType(Uri.fromFile(remindVoiceFile),
                            "audio/amr");
                    context.startActivity(playRemindVoiceIntent);
                } else {
                    Toast.makeText(context, R.string.remind_voice_error,
                            Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(context, R.string.sdcard_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * @param context
     * @param resourceId
     * @param fileName
     */
    public static void downRemindVoiceByHttp(final Context context, final RemindInfo remindInfo) {
        VolleyClient.getInstance().remindVoiceGet(
                Utils.getServerResourceDownloadUrl(context, remindInfo.getContent()),
                new RequestListener<DeviceDownloadResponse>() {

                    @Override
                    public void onRequestSuccess(DeviceDownloadResponse response) {
                        // TODO Auto-generated method stub
                        if (response != null) {
                            LogUtil.i("debug",
                                    "=====downRemindVoiceByHttp=======onRequestSuccess======isExitVoiceStream:"
                                            + response.isExitVoiceStream());
                            if (response.isExitVoiceStream()) {
                                FileUtil.saveRemindVoice(context,
                                        response.getOstream(), remindInfo);
                            } else {
                                LogUtil.i("debug",
                                        "=====downRemindVoiceByHttp=======onRequestSuccess======status:"
                                                + response.getStatus()
                                                + "---prompt:" + response.getPrompt());
                            }
                        }

                    }

                    @Override
                    public void onRequestFail(int code, String msg) {
                        // TODO Auto-generated method stub
                        LogUtil.i("debug",
                                "=====deviceQrcodeGet======onRequestFail========code:"
                                        + code + " msg:" + msg);
                    }

                    @Override
                    public void onRequestError(int code, String msg) {
                        // TODO Auto-generated method stub
                        LogUtil.i("debug",
                                "=====deviceQrcodeGet======onRequestError========code:"
                                        + code + " msg:" + msg);
                    }

                    @Override
                    public void onPreRequest() {
                        // TODO Auto-generated method stub

                    }
                });
    }

    /**
     * 通过插槽获取运营商信息
     * 
     * @param context
     * @param predictedMethodName
     * @param slotID
     * @return
     */
    public static String getOperatorBySlot(Context context, String predictedMethodName, int slotID) {
        String inumeric = null;
        TelephonyManager telephony = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());
            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimID = telephonyClass.getMethod(predictedMethodName, parameter);
            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimID.invoke(telephony, obParameter);
            if (ob_phone != null) {
                inumeric = ob_phone.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inumeric;
    }

    /**
     * 解除绑定后清除用户信息
     */
    public static void clearUserInfo(Context context) {
        PreferencesUtils.putInt(context, Constants.FROMID, -1);
        PreferencesUtils.putInt(context, Constants.CHATROOMID, -1);
        PreferencesUtils.putString(context, Constants.PHONENUMBER, null);
        PreferencesUtils.putString(context, Constants.NICKNAME, null);
        PreferencesUtils.putInt(context, Constants.USERID, -1);
        PreferencesUtils.putInt(context, Constants.DEVICEID, -1);
        PreferencesUtils.putString(context, Constants.FAMILYNAME, null);
        PreferencesUtils.putString(context, Constants.TOKEN, null);
        PreferencesUtils.putBoolean(context, Constants.IS_CONFIRM_BIND, false);
    }

    /**
     * 获取服务器响应提醒消息
     * 
     * @param receTcpProtocol
     * @return
     */
    public static ArrayList<RemindInfo> getRemindInfos(TcpProtocol receTcpProtocol) {
        List<RemindInfo> remindInfos = new ArrayList<RemindInfo>();
        List<Map<String, Object>> datas = receTcpProtocol.getDatas();
        for (int i = 0; i < datas.size(); i++) {
            Map<String, Object> dataMap = datas.get(i);
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                if ("data".equals(entry.getKey())) {
                    remindInfos = JsonUtil.getDatas(entry.getValue().toString(), RemindInfo.class);
                }
            }
        }
        ArrayList<RemindInfo> remindInfoList = new ArrayList<RemindInfo>();
        for (int i = 0; i < remindInfos.size(); i++) {
            RemindInfo remindInfo = remindInfos.get(i);
            remindInfo.setRemindId(remindInfo.getId());
            remindInfoList.add(remindInfo);
        }
        return remindInfoList;
    }

    public static void cleanLocationData() {
        Database mDatabase = new LitepalDatabaseImpl();
        mDatabase.deleteGPSInfo();
        mDatabase.deleteMobileInfo();
        mDatabase.deleteWifiInfo();
    }

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     * 
     * @param context
     * @return true 表示开启
     */
    public static final boolean isOPen(final Context context) {
        LocationManager locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }

        return false;
    }

    /**
     * Enable or disable location in settings.
     * <p>
     * This will attempt to enable/disable every type of location setting (e.g.
     * high and balanced power).
     * <p>
     * If enabling, a user consent dialog will pop up prompting the user to
     * accept. If the user doesn't accept, network location won't be enabled.
     * 
     * @return true if attempt to change setting was successful.
     */
    // hhj@20161022
    public static boolean setLocationEnabled(boolean enabled) {
        if (isRestricted()) {
            return false;
        }
        final ContentResolver cr = FPHealthApplication.getInstance().getContentResolver();
        // When enabling location, a user consent dialog will pop up, and the
        // setting won't be fully enabled until the user accepts the agreement.
        int mode = enabled
                ? Settings.Secure.LOCATION_MODE_HIGH_ACCURACY : Settings.Secure.LOCATION_MODE_OFF;
        if (enabled && WCN_DISABLED) {
            mode = Settings.Secure.LOCATION_MODE_BATTERY_SAVING;
        }
        LogUtil.i("debug", "========Utils============setLocationEnabled=====mode:" + mode);
        int mCurrentMode = Settings.Secure.getInt(cr, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF);
        Intent intent = new Intent(MODE_CHANGING_ACTION);
        intent.putExtra(CURRENT_MODE_KEY, mCurrentMode);
        intent.putExtra(NEW_MODE_KEY, mode);
        FPHealthApplication.getInstance().sendBroadcast(intent,
                android.Manifest.permission.WRITE_SECURE_SETTINGS);
        // QuickSettings always runs as the owner, so specifically set the
        // settings
        // for the current foreground user.
        return Settings.Secure
                .putInt(cr, Settings.Secure.LOCATION_MODE, mode);
    }

    // hhj@20161022
    private static boolean isRestricted() {
        final UserManager um = (UserManager) FPHealthApplication.getInstance().getSystemService(
                Context.USER_SERVICE);
        return um.hasUserRestriction(UserManager.DISALLOW_SHARE_LOCATION);
    }

    /**
     * 发起定位
     */
    public static void requestLocation() {
        LogUtil.i("debug", "======Utils============requestLocation==");
        LocationInfo locationInfo = new LocationInfo();
        LocationManager locationManager = (LocationManager) FPHealthApplication
                .getInstance().getSystemService(Context.LOCATION_SERVICE);

        // 获取所有可用的位置提供器
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);// 设置为最大精度
        criteria.setAltitudeRequired(false);// 不要求海拔信息
        criteria.setBearingRequired(false);// 不要求方位信息
        criteria.setCostAllowed(true);// 是否允许付费
        criteria.setPowerRequirement(Criteria.POWER_LOW);// 对电量的要求
        // 增加权限判断
        if (!PermissionsChecker.lacksPermission(Constants.ACCESS_COARSE_LOCATION_PERMISSION)) {
            String provider = locationManager.getBestProvider(criteria, true);
            if (!TextUtils.isEmpty(provider)) {
                Location location = locationManager.getLastKnownLocation(provider);
                updateWithNewLocation(location);
                locationManager.requestLocationUpdates(provider, 10000, 10, new LocationListener() {

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onLocationChanged(Location location) {
                        LogUtil.i("debug", "========Utils===========onLocationChanged=====");
                        updateWithNewLocation(location);
                    }
                });
            }
        }
    }

    private static void updateWithNewLocation(Location location) {
        LogUtil.i("debug", "======Utils============updateWithNewLocation:");
        String latitude = "";
        String longitude = "";
        if (location != null) {
            latitude = location.getLatitude() + "";// 纬度
            longitude = location.getLongitude() + "";// 经度
            float spe = location.getSpeed();// 速度
            float acc = location.getAccuracy();// 精度
            double alt = location.getAltitude();// 海拔
            float bea = location.getBearing();// 轴承
            long tim = location.getTime();// 返回UTC时间1970年1月1毫秒
        }
        PreferencesUtils.putString(FPHealthApplication.getInstance(), Constants.KEY_LATITUDE,
                latitude);
        PreferencesUtils.putString(FPHealthApplication.getInstance(), Constants.KEY_LONGITUDE,
                longitude);
    }

    public static LocationInfo getGPSLocation(Context mContext) {
        String latitude = PreferencesUtils.getString(mContext, Constants.KEY_LATITUDE);
        String longitude = PreferencesUtils.getString(mContext, Constants.KEY_LONGITUDE);
        if (!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude)) {
            LocationInfo locationInfo = new LocationInfo();
            locationInfo.setLatitude(latitude);
            locationInfo.setLongitude(longitude);
            locationInfo.setTime(System.currentTimeMillis() + "");
            return locationInfo;
        }
        return null;
    }

    /**
     * collect mobile phone base station information
     * 
     * @param context
     * @return
     */
    public static Mobile getMobileBaseStation(Context context) {
        // 增加权限判断
        if (!PermissionsChecker.lacksPermissions(Constants.ACCESS_COARSE_LOCATION_PERMISSION,
                Constants.READ_PHONE_STATE_PERMISSION)) {
            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            String serverIp = NetworkUtil.getGateWayIp();
            String network = "";
            if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
                network = "GSM";
            } else if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
                network = "CDMA";
            }

            Mobile mobile = new Mobile();
            List<MobileBaseStation> mobileBaseStations = new ArrayList<MobileBaseStation>();
            List<CellInfo> infoLists = telephonyManager.getAllCellInfo();
            LogUtil.i("debug",
                    "=======getMobileBaseStation=========>>infoListsSize:" + infoLists.size());
            if (infoLists != null && infoLists.size() > 0) {
                int infoSize = infoLists.size() < Constants.BASESTATION_NUMBER ? infoLists
                        .size() : Constants.BASESTATION_NUMBER;
                for (int i = 0; i < infoSize; i++) {
                    CellInfo cellInfo = infoLists.get(i);
                    String additional_info;
                    MobileBaseStation mobileBaseStation = new MobileBaseStation();
                    String ci = "";
                    String lac = "";
                    String rssi = "";
                    String mcc = "";
                    String mnc = "";
                    if (cellInfo instanceof CellInfoGsm) {
                        CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                        CellIdentityGsm cellIdentityGsm = cellInfoGsm.getCellIdentity();
                        CellSignalStrengthGsm cellSignalStrengthGsm = cellInfoGsm
                                .getCellSignalStrength();
                        int strength = cellSignalStrengthGsm.getDbm();
                        ci = cellIdentityGsm.getCid() + "";
                        lac = cellIdentityGsm.getLac() + "";
                        rssi = cellSignalStrengthGsm.getDbm() + "";
                        mcc = cellIdentityGsm.getMcc() + "";
                        mnc = cellIdentityGsm.getMnc() + "";
                        additional_info = "cell identity " + cellIdentityGsm.getCid() + "\n"
                                + "Mobile country code " + cellIdentityGsm.getMcc() + "\n"
                                + "Mobile network code " + cellIdentityGsm.getMnc() + "\n"
                                + "local area " + cellIdentityGsm.getLac() + "\n"
                                + "strength: " + strength + "\n";
                        LogUtil.i("debug",
                                "======getMobileBaseStation====CellInfoGsm=====additional_info:"
                                        + additional_info);
                    } else if (cellInfo instanceof CellInfoLte) {
                        CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                        CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
                        CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte
                                .getCellSignalStrength();
                        int strength = cellSignalStrengthLte.getDbm();
                        ci = cellIdentityLte.getCi() + "";
                        lac = cellIdentityLte.getTac() + "";
                        rssi = cellSignalStrengthLte.getDbm() + "";
                        mcc = cellIdentityLte.getMcc() + "";
                        mnc = cellIdentityLte.getMnc() + "";
                        additional_info = "cell identity " + cellIdentityLte.getCi() + "\n"
                                + "Mobile country code " + cellIdentityLte.getMcc() + "\n"
                                + "Mobile network code " + cellIdentityLte.getMnc() + "\n"
                                + "physical cell " + cellIdentityLte.getPci() + "\n"
                                + "Tracking area code " + cellIdentityLte.getTac() + "\n"
                                + "strength " + strength + "\n";
                        LogUtil.i("debug",
                                "======getMobileBaseStation====CellInfoLte=====additional_info:"
                                        + additional_info);
                    } else if (cellInfo instanceof CellInfoWcdma) {
                        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                        CellIdentityWcdma cellIdentityWcdma = cellInfoWcdma.getCellIdentity();
                        CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma
                                .getCellSignalStrength();
                        int strength = cellSignalStrengthWcdma.getDbm();
                        ci = cellIdentityWcdma.getCid() + "";
                        lac = cellIdentityWcdma.getLac() + "";
                        rssi = cellSignalStrengthWcdma.getDbm() + "";
                        mcc = cellIdentityWcdma.getMcc() + "";
                        mnc = cellIdentityWcdma.getMnc() + "";
                        additional_info = "cell identity " + cellIdentityWcdma.getCid() + "\n"
                                + "Mobile country code " + cellIdentityWcdma.getMcc() + "\n"
                                + "Mobile network code " + cellIdentityWcdma.getMnc() + "\n"
                                + "local area " + cellIdentityWcdma.getLac() + "\n"
                                + "strength " + strength + "\n";
                        LogUtil.i("debug",
                                "======getMobileBaseStation====CellInfoWcdma=====additional_info:"
                                        + additional_info);
                    }
                    if (i == 0) {
                        mobile.setMcc(mcc);
                        mobile.setMnc(mnc);
                        mobile.setNetwork(network);
                        mobile.setServerIp(serverIp);
                        mobile.setTime(System.currentTimeMillis() + "");
                    }
                    mobileBaseStation.setCi(ci);
                    mobileBaseStation.setLac(lac);
                    mobileBaseStation.setRssi(rssi);
                    mobileBaseStations.add(mobileBaseStation);
                }
                mobile.setMobileBaseStations(mobileBaseStations);
            }
            return mobile;
        } else {
            return null;
        }

    }

}
