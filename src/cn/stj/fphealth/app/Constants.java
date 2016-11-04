
package cn.stj.fphealth.app;

import android.Manifest;
import android.R.integer;
import android.os.Environment;

/**
 * @author hhj@20160804
 */
public class Constants {

    public static final String KEY_SEDENTARY_REMIND = "key_sedentary_remind";
    public static final long INTERVAL_TIME = 240 * 1000;
    public static final int DEVICE_BIND_STATUS = 1;
    public static final int DEVICE_NOTBIND_STATUS = 0;
    public static final String DEVICEBIND_STATUS = "device_bind_status";
    public static final String RESPONSE_STATUS = "response_status";
    public static final String RESPONSE_TCP = "response_tcp";
    public static final String LOW_PRESSURE = "low_pressure";
    public static final String HIGH_PRESSURE = "high_pressure";
    public static final String ERROR_CODE = "error_code";
    public static final int WIFI_HOTSPOT_NUMBER = 5;
    public static final int BASESTATION_NUMBER = 5;
    public static final int NEIGHBORING_CELL_NUMBER = 4;
    public static boolean IS_SHOW_REQUEST_LOG = true;

    public static final String FROMID = "fromId";
    public static final String CHATROOMID = "chatRoomId";
    public static final String PHONENUMBER = "phoneNumber";
    public static final String NICKNAME = "nickName";
    public static final String USERID = "userId";
    public static final String DEVICEID = "deviceId";
    public static final String FAMILYNAME = "familyName";
    public static final String TIME = "time";
    public static final String TOKEN = "token";
    public static final String MONITOR_USERID = "monitor_userid";
    public static final String MONITOR_PHONENUMBER = "monitor_phonenumber";

    public static final String SERVER_IP = "server_ip";
    public static final String QRCODE_SAVE_PATH = Environment.getExternalStorageDirectory()
            .getPath() + "FPhealth/qrcode/qrcode.png";
    public static final String BOOT = "boot";
    public static final int TARGET_STEP_NUM = 10000;
    public static final int HEART_RATE_MAX_PREGRESS = 10;
    public static final String IS_CONFIRM_BIND = "is_confirm_bind";
    public static int LOCATION_RETRY = 3;
    public static final long DAY = 24 * 60 * 60 * 1000 ;
    public static final String BIND_QRCODE_FILENAME = "qrcode.jpg";
    public static final String HEALTH_RECEIVER_ACTION = "android.intent.action.HEALTH_RECEIVER";
    public static final String DEVICE_UNBIND_ACTION = "android.intent.action.DEVICE_UNBIND";
    public static final String CLEAR_STEP_ACTION = "android.intent.action.CLEAR_STEP";
    public static final String REFRESH_REMIND_ACTION = "android.intent.action.REFRESH_REMIND";
    public static final String IS_NETWORK_AVAILABLE = "isNetworkAvailable";
    public static final String KEY_TOTAL_STEP = "key_total_step";
    public static final String KEY_STEP_TIME = "key_step_time";
    public static final int UNBIND_NOTIFICATION_ID = 100;
    public static final String KEY_LATITUDE = "key_latitude";
    public static final String KEY_LONGITUDE = "key_longitude";

    public static class DEVICE_PARAM {
        public static final int HEALTH_WALK_UPLOAD_FREP_VALUE = 10 * 1000;
        public static final int HEALTH_WALK_UPLOAD_FREP_CODE = 400021;
        public static final int HEALTH_WALK_ONOFF_VALUE = 1;
        public static final int HEALTH_WALK_ONOFF_CODE = 400020;

        public static final String BEAT = "beat";
        public static final String TIMEOUT = "timeout";
        public static final String RETRY = "retry";
        public static final String RESTART = "restart";

        public static final String BRIGHT = "bright";
        public static final String SOUND = "sound";
        public static final String SHAKE = "shake";
        public static final String QUIET = "quiet";
        public static final String POWER_AUTO = "powerAuto";
        public static final String BOOT_TIME = "bootTime";
        public static final String OFF_TIME = "offTime";
        public static final String LOCATION_ONOFF = "locationOnff";
        public static final String HEART_ONOFF = "heartOnff";
        public static final String WALK_ONOFF = "walkOnff";
        public static final String SITTING_ONOFF = "sittingOnff";
        public static final String SLEEP_ONOFF = "sleepOnff";
        public static final String TIMEZONE = "timezone";
        public static final String COMMON = "common";
        public static final String CONNECT = "connect";
        public static final String HEALTH = "health";
        public static final String FREQUENCY = "frequency";

        public static final String SIGNAL_GFREQ = "signalGfreq";
        public static final String SIGNAL_UFREQ = "signalUfreq";
        public static final String BATTERY_GFREQ = "batteryGfreq";
        public static final String BATTERY_UFREQ = "batteryUfreq";
        public static final String HEART_GFREQ = "heartGfreq";
        public static final String HEART_UFREQ = "heartUfreq";
        public static final String WALK_UFREQ = "walkUfreq";
        public static final String LOCATION_GFREQ = "locationGfreq";
        public static final String LOCATION_UFREQ = "locationUfreq";

        public static final String HEIGHT = "height";
        public static final String WEIGHT = "weight";
        public static final String SITTING_TIME = "sittingTime";
        public static final String SITTING_SPAN = "sittingSpan";
        public static final String SITTING_SPAN_ONE = "sittingSpanOne";
        public static final String SITTING_SPAN_SECOND = "sittingSpanSecond";
        public static final String SLEEP_SPAN = "sleepSpan";
        public static final String SLEEP_SPAN_ONE = "sleepSpanOne";
        public static final String SLEEP_SPAN_SECOND = "sleepSpanSecond";

        public static final String ALL_CODE = "410000";
        public static final String COMMON_CODE = "410001";
        public static final String CONNECT_CODE = "410002";
        public static final String HEALTH_CODE = "410003";
        public static final String FREQUENCY_CODE = "410004";
        public static final String CODE = "code";
        public static final String VALUE = "value";

    }

    public static final int RESULT_ERROR_NET = 0;
    public static final int RESULT_ERROR_EXCEPTION = 1;

    public static class TCP {
        public static final int COMMAND_HEART_UPLOAD = 20744;
        public static final int COMMAND_DEVICE_HEARTBEAT = 20001;
        public static final int COMMAND_DEVICE_HELLO = 20101;
        public static final int COMMAND_DEVICE_BIND = 20102;
        public static final int COMMAND_DEVICE_UNBIND = 20106;
        public static final int COMMAND_WALK_UPLOAD = 20844;
        public static final int COMMAND_WALK_QUERY = 20825;
        public static final int COMMAND_WALK_REMOTE = 20824;
        public static final int COMMAND_SITTING_UPLOAD = 20845;
        public static final int COMMAND_SITTING_QUERY = 20829;
        public static final int COMMAND_BLOOD_PRESSUE_UPLOAD = 20851;

        public static final int COMMAND_USER_INFO_GET = 20205;
        public static final int COMMAND_USER_INFO_PUSH = 20206;
        public static final int COMMAND_CONTACTS_SET = 20301;
        public static final int COMMAND_CONTACTS_QUERY = 20321;
        public static final int COMMAND_REMIND_ADD = 20403;
        public static final int COMMAND_REMIND_EDIT = 20404;
        public static final int COMMAND_REMIND_DEL = 20405;
        public static final int COMMAND_REMIND_SEND_NOTICE = 20406;
        public static final int COMMAND_LOCATION_TRRIGE = 20626;
        public static final int COMMAND_LOCATION_UPLOAD = 20646;
        public static final int COMMAND_LOCATION_QUERY = 20647;
        public static final int COMMAND_MONITOR = 28961;
        public static final int COMMAND_MONITOR_FINISH = 28962;
        public static final int COMMAND_PARAM_SET = 20208;
        public static final int COMMAND_PARAM_GET = 20209;

        public static final int COMMAND_SIGNAL_QUERY = 20186;
        public static final int COMMAND_SIGNAL_UPLOAD = 20187;
        public static final int COMMAND_BATTERY_QUERY = 20196;
        public static final int COMMAND_BATTERY_UPLOAD = 20197;
        public static final int COMMAND_DEVICE_OPERATION = 28999;

        public static final int STATUS_DEVICE_BIND = 0;
        public static final int STATUS_DEVICE_UNBIND = 200001;
        public static final int STATUS_IMEI_INVALID = 200002;

    }

    public static class HTTP {
        public static final String SERVER_URL_BASE = "http://www.ejialian365.com:6060";
        public static final String SERVER_IP_URL = SERVER_URL_BASE + "/server/ip";
        public static final String DEVICE_SOFTWARE_URL = SERVER_URL_BASE + "/device/software";
        public static final String DEVICE_QRCODE_URL = SERVER_URL_BASE + "/device/qrcode";
        public static final String DEVICE_INIT_URL = SERVER_URL_BASE + "/device/init";

        public static final String SERVER_IP_PATH = "/device/ip";
        public static final String DEVICE_SOFTWARE_PATH = "/device/software";
        public static final String DEVICE_QRCODE_PATH = "/device/qrcode";
        public static final String DEVICE_INIT_PATH = "/device/init";
        public static final String RESOURCE_DOWNLOAD_PATH = "/resource/download";
        // public static final String SERVER_DOMAIN =
        // "http://www.ejialian365.com/";
        public static final String SERVER_DOMAIN = "120.25.160.36";
        public static final String SERVER_PORT = "6060";
        public static final String SERVER_KEY = "11111111222222223333333344444444";

    }
    
    // 所需的全部权限
    public static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    
    public static final String READ_PHONE_STATE_PERMISSION = Manifest.permission.READ_PHONE_STATE;
    public static final String WRITE_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String READ_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String ACCESS_FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String ACCESS_COARSE_LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION;
}
