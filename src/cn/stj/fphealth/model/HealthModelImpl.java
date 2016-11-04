
package cn.stj.fphealth.model;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.telephony.TelephonyManager;

import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.app.Utils;
import cn.stj.fphealth.db.Database;
import cn.stj.fphealth.db.LitepalDatabaseImpl;
import cn.stj.fphealth.entity.Contact;
import cn.stj.fphealth.entity.Device;
import cn.stj.fphealth.entity.HeartRate;
import cn.stj.fphealth.entity.LocationInfo;
import cn.stj.fphealth.entity.Mobile;
import cn.stj.fphealth.entity.Pedometer;
import cn.stj.fphealth.entity.RemindInfo;
import cn.stj.fphealth.entity.Wifi;
import cn.stj.fphealth.service.HealthListener;
import cn.stj.fphealth.service.RemindService;
import cn.stj.fphealth.tcp.HealthTcpClient;
import cn.stj.fphealth.tcp.mina.TcpProtocol;
import cn.stj.fphealth.util.JsonUtil;
import cn.stj.fphealth.util.LogUtil;
import cn.stj.fphealth.util.PreferencesUtils;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hhj@20160804
 */
public class HealthModelImpl implements HealthModel {

    private Context mContext;
    private List<HeartRate> mCacheHeartRates = new ArrayList<HeartRate>();
    private Database mDatabase;

    public HealthModelImpl(Context context) {
        super();
        this.mContext = context;
        mDatabase = new LitepalDatabaseImpl();
    }

    @Override
    public void sendHeartRate(Object data) {
        TcpProtocol tcpProtocol = new TcpProtocol();
        tcpProtocol.setCommand(Constants.TCP.COMMAND_HEART_UPLOAD);
//        HealthTcpClient.getInstance(mContext).sendHeartRate(tcpProtocol);
    }

    @Override
    public void sendDeviceHello(final HealthListener listener) {
        LogUtil.i("debug", "=======HealthModelImpl=============sendDeviceHello=====");
        TcpProtocol tcpProtocol = new TcpProtocol();
        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("data", getDeviceHelloValue(mContext));
        datas.add(data);
        tcpProtocol.setDatas(datas);
        tcpProtocol.setCommand(Constants.TCP.COMMAND_DEVICE_HELLO);
    }

    public String getDeviceHelloValue(Context context) {
        TelephonyManager tm = (TelephonyManager)
        mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = Utils.getImei(context);
        String imsi = tm.getSubscriberId();
        Device device = new Device();
        device.setImei(imei);
        device.setImsi(imsi);
        return JsonUtil.getGson().toJson(device);
    }

    public void sendDeviceParamSet(Object data) {
        TcpProtocol tcpProtocol = new TcpProtocol();
        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        Map<String, Object> data1 = new HashMap<String, Object>();
        data1.put("code", 400017);
        datas.add(data1);
        Map<String, Object> data2 = new HashMap<String, Object>();
        data2.put("value", "1");
        datas.add(data2);
        tcpProtocol.setDatas(datas);
        tcpProtocol.setCommand(Constants.TCP.COMMAND_PARAM_SET);
//        HealthTcpClient.getInstance(mContext).sendDeviceParamSet(tcpProtocol);
    }

    @Override
    public void sendHeartBeat(int status) {
        TcpProtocol tcpProtocol = new TcpProtocol();
        tcpProtocol.setStatus(status);
        tcpProtocol.setCommand(Constants.TCP.COMMAND_DEVICE_HEARTBEAT);
//        HealthTcpClient.getInstance(mContext).sendHeartBeat(tcpProtocol);
    }

    @Override
    public void sendDeviceBind(final int status, final HealthListener listener) {
        TcpProtocol tcpProtocol = new TcpProtocol();
        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        Map<String, Object> data1 = new HashMap<String, Object>();
        data1.put("toId", PreferencesUtils.getInt(mContext, Constants.FROMID));
        datas.add(data1);
        Map<String, Object> data2 = new HashMap<String, Object>();
        data2.put("status", status);
        datas.add(data2);
        tcpProtocol.setDatas(datas);
        tcpProtocol.setCommand(Constants.TCP.COMMAND_DEVICE_BIND);
//        tcpProtocol.setToken("b1531c37b2cc4805adccfb26f88b9e19");
    }

    @Override
    public void sendDeviceUnBind() {
        TcpProtocol tcpProtocol = new TcpProtocol();
        tcpProtocol.setCommand(Constants.TCP.COMMAND_DEVICE_UNBIND);
//        HealthTcpClient.getInstance(mContext).sendDeviceUnBind(tcpProtocol);
    }

    @Override
    public void sendWalkUpload(List<Pedometer> pedometers) {
        TcpProtocol tcpProtocol = new TcpProtocol();
        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("data", JsonUtil.getGson().toJson(pedometers));
        datas.add(data);
        tcpProtocol.setDatas(datas);
        tcpProtocol.setCommand(Constants.TCP.COMMAND_WALK_UPLOAD);
        LogUtil.i("debug", "=====HealthModelImpl=====sendWalkUpload(20844)==========tcpProtocol:" + tcpProtocol.toString());
//        HealthTcpClient.getInstance(mContext).sendWalkUpload(tcpProtocol);
    }

    @Override
    public void sendBloodPressueUpload(List<HeartRate> heartRates) {
        mCacheHeartRates.add(heartRates.get(heartRates.size() - 1));
        TcpProtocol tcpProtocol = new TcpProtocol();
        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("data", JsonUtil.getGson().toJson(heartRates));
        LogUtil.i("debug", "=====HealthModelImpl=============sendBloodPressueUpload(20851)=====" + JsonUtil.getGson().toJson(heartRates));
        datas.add(data);
        tcpProtocol.setDatas(datas);
        tcpProtocol.setCommand(Constants.TCP.COMMAND_BLOOD_PRESSUE_UPLOAD);
    }

    @Override
    public void sendUserInfoGet(int userId) {
        LogUtil.i("debug", "=====HealthModelImpl================sendUserInfoGet======");
        TcpProtocol tcpProtocol = new TcpProtocol();
        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("userId", 1355);
        datas.add(data);
        tcpProtocol.setDatas(datas);
        tcpProtocol.setCommand(Constants.TCP.COMMAND_USER_INFO_GET);
//        HealthTcpClient.getInstance(mContext).sendWalkUpload(tcpProtocol);
    }

    @Override
    public void sendUserInfoPush() {
        LogUtil.i("debug", "=====HealthModelImpl================sendUserInfoPush======");
        TcpProtocol tcpProtocol = new TcpProtocol();
        tcpProtocol.setCommand(Constants.TCP.COMMAND_USER_INFO_PUSH);
//        HealthTcpClient.getInstance(mContext).sendWalkUpload(tcpProtocol);
    }

    @Override
    public void remindAdd(ArrayList<RemindInfo> remindInfos) {
        LogUtil.i("debug", "====HealthModelImpl===========remindAdd=======");
        mDatabase.saveRemindInfo(remindInfos);
        Intent intent = new Intent(mContext,RemindService.class);
        intent.putExtra(RemindService.REMIND_FLAG, RemindService.REMIND_ADD);
        intent.putParcelableArrayListExtra(RemindService.REMIND_INFO, remindInfos);
        mContext.startService(intent);
    }

    @Override
    public void remindEdit(ArrayList<RemindInfo> remindInfos) {
        LogUtil.i("debug", "====HealthModelImpl===========remindEdit=======");
        mDatabase.updateRemindInfo(remindInfos);
        Intent intent = new Intent(mContext,RemindService.class);
        intent.putExtra(RemindService.REMIND_FLAG, RemindService.REMIND_EDIT);
        intent.putParcelableArrayListExtra(RemindService.REMIND_INFO, remindInfos);
        mContext.startService(intent);
    }

    @Override
    public void remindDel(ArrayList<RemindInfo> remindInfos) {
        LogUtil.i("debug", "====HealthModelImpl===========remindDel=======");
        mDatabase.deleteRemindInfo(remindInfos);
        Intent intent = new Intent(mContext,RemindService.class);
        intent.putExtra(RemindService.REMIND_FLAG, RemindService.REMIND_DEL);
        intent.putParcelableArrayListExtra(RemindService.REMIND_INFO, remindInfos);
        mContext.startService(intent);
    }

    @Override
    public void sendRemindSuccessNotice(RemindInfo remindInfo) {
        LogUtil.i("debug", "=====HealthModelImpl================sendRemindSuccessNotice======");
        TcpProtocol tcpProtocol = new TcpProtocol();
        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        Map<String, Object> timeMap = new HashMap<String, Object>();
        timeMap.put("time", remindInfo.getTime());
        datas.add(timeMap);
        Map<String, Object> remindIdMap = new HashMap<String, Object>();
        remindIdMap.put("remindId", remindInfo.getRemindId());
        datas.add(remindIdMap);
        tcpProtocol.setDatas(datas);
        tcpProtocol.setCommand(Constants.TCP.COMMAND_REMIND_SEND_NOTICE);
//        HealthTcpClient.getInstance(mContext).sendRemindSuccessNotice(tcpProtocol);
    }

    @Override
    public void sendLocationUpload() {
        Utils.requestLocation();
        List<LocationInfo> locationInfos = mDatabase.getGPSInfo();
        List<Wifi> wifis = mDatabase.getWifiInfo();
        List<Mobile> mobiles = mDatabase.getMobileInfo();
        TcpProtocol tcpProtocol = new TcpProtocol();
        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        Map<String, Object> wifiMap = new HashMap<String, Object>();
        wifiMap.put("wifi", wifis.size() > 0?JsonUtil.getWifiJson(wifis):"");
        datas.add(wifiMap);
        Map<String, Object> mobileMap = new HashMap<String, Object>();
        mobileMap.put("mobile", mobiles.size() > 0?JsonUtil.getGSMCellLocationJson(mobiles):"");
        datas.add(mobileMap);
        Map<String, Object> gpsMap = new HashMap<String, Object>();
        gpsMap.put("gps",locationInfos.size() > 0 ?JsonUtil.getGson().toJson(locationInfos):"");
        
        datas.add(gpsMap);
        tcpProtocol.setDatas(datas);
        tcpProtocol.setCommand(Constants.TCP.COMMAND_LOCATION_UPLOAD);
//        HealthTcpClient.getInstance(mContext).sendLocationUpload(tcpProtocol);
    }

	@Override
	public void sendLocationQuery() {
	    List<LocationInfo> locationInfos = mDatabase.getGPSInfo();
        List<Wifi> wifis = mDatabase.getWifiInfo();
        List<Mobile> mobiles = mDatabase.getMobileInfo();
        LogUtil.i("debug", "=====HealthModelImpl================sendLocationQuery======20647");
        TcpProtocol tcpProtocol = new TcpProtocol();
        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        Map<String, Object> timeMap = new HashMap<String, Object>();
        timeMap.put("time", System.currentTimeMillis());
        datas.add(timeMap);
        Map<String, Object> wifiMap = new HashMap<String, Object>();
        wifiMap.put("wifi", wifis.size() > 0?JsonUtil.getWifiJson(wifis):"");
        datas.add(wifiMap);
        Map<String, Object> mobileMap = new HashMap<String, Object>();
        mobileMap.put("mobile", mobiles.size() > 0?JsonUtil.getGSMCellLocationJson(mobiles):"");
        datas.add(mobileMap);
        Map<String, Object> gpsMap = new HashMap<String, Object>();
        gpsMap.put("gps",locationInfos.size() > 0 ?JsonUtil.getGson().toJson(locationInfos):"");
        
        datas.add(gpsMap);
        tcpProtocol.setDatas(datas);
        tcpProtocol.setCommand(Constants.TCP.COMMAND_LOCATION_QUERY);
        LogUtil.i("debug", "=====HealthModelImpl================sendLocationQuery======tcpProtocol:" + tcpProtocol.toString());
//        HealthTcpClient.getInstance(mContext).sendLocationQuery(tcpProtocol);
	}

	@Override
	public void sendLocationTrrige() {
	    List<LocationInfo> locationInfos = mDatabase.getGPSInfo();
        List<Wifi> wifis = mDatabase.getWifiInfo();
        List<Mobile> mobiles = mDatabase.getMobileInfo();
        locationInfos.add(Utils.getGPSLocation(mContext));
        wifis.add(Utils.getWifiInfo(mContext));
        mobiles.add(Utils.getMobileBaseStation(mContext));
        LogUtil.i("debug", "=====HealthModelImpl================sendLocationTrrige======20626");
        TcpProtocol tcpProtocol = new TcpProtocol();
        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        Map<String, Object> timeMap = new HashMap<String, Object>();
        timeMap.put("time", System.currentTimeMillis());
        datas.add(timeMap);
        Map<String, Object> wifiMap = new HashMap<String, Object>();
        wifiMap.put("wifi", wifis.size() > 0?JsonUtil.getWifiJson(wifis):"");
        datas.add(wifiMap);
        Map<String, Object> mobileMap = new HashMap<String, Object>();
        mobileMap.put("mobile", mobiles.size() > 0?JsonUtil.getGSMCellLocationJson(mobiles):"");
        datas.add(mobileMap);
        Map<String, Object> gpsMap = new HashMap<String, Object>();
        gpsMap.put("gps",locationInfos.size() > 0 ?JsonUtil.getGson().toJson(locationInfos):"");
        datas.add(gpsMap);
        tcpProtocol.setDatas(datas);
        tcpProtocol.setCommand(Constants.TCP.COMMAND_LOCATION_TRRIGE);
        LogUtil.i("debug", "=====HealthModelImpl================sendLocationTrrige======tcpProtocol:" + tcpProtocol.toString());
//        HealthTcpClient.getInstance(mContext).sendLocationTrrige(tcpProtocol);
	}

	@Override
	public void sendConstactsSet() {
		LogUtil.i("debug", "=====HealthModelImpl================sendConstactsSet======");
        TcpProtocol tcpProtocol = new TcpProtocol();
        tcpProtocol.setCommand(Constants.TCP.COMMAND_CONTACTS_SET);
//        HealthTcpClient.getInstance(mContext).sendConstactsSet(tcpProtocol);
	}

	@Override
	public void sendConstactsQuery() {
		List<Contact> contacts = new ArrayList<Contact>();
		for(int i = 0;i < 2;i++){
			Contact contact = new Contact();
			contact.setHeadImage("mediaId");
			contact.setIsSos(i);
			contact.setNickName(i==0?"孙子":"儿媳");
			contact.setPhoneNumber("12312345678");
			contact.setUserId(1001 + i);
			contacts.add(contact);
		}
		TcpProtocol tcpProtocol = new TcpProtocol();
		List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        Map<String, Object> timeMap = new HashMap<String, Object>();
        timeMap.put("time", System.currentTimeMillis());
        datas.add(timeMap);
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("data", JsonUtil.getGson().toJson(contacts));
        LogUtil.i("debug", "=====HealthModelImpl================sendConstactsQuery======data:" + JsonUtil.getGson().toJson(contacts));
        datas.add(dataMap);
        tcpProtocol.setDatas(datas);
        tcpProtocol.setCommand(Constants.TCP.COMMAND_CONTACTS_QUERY);
//        HealthTcpClient.getInstance(mContext).sendConstactsQuery(tcpProtocol);
	}

	@Override
	public void sendMonitor() {
		LogUtil.i("debug", "=====HealthModelImpl================sendMonitor======");
		TcpProtocol tcpProtocol = new TcpProtocol();
		List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        Map<String, Object> timeMap = new HashMap<String, Object>();
        timeMap.put("time", System.currentTimeMillis());
        datas.add(timeMap);
        tcpProtocol.setDatas(datas);
		tcpProtocol.setCommand(Constants.TCP.COMMAND_MONITOR);
//        HealthTcpClient.getInstance(mContext).sendMonitor(tcpProtocol);
	}

	@Override
	public void sendMonitorFinish() {
		LogUtil.i("debug", "=====HealthModelImpl================sendMonitorFinish======");
		TcpProtocol tcpProtocol = new TcpProtocol();
		List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        Map<String, Object> useridMap = new HashMap<String, Object>();
        useridMap.put("userId", 1355);
        datas.add(useridMap);
        tcpProtocol.setDatas(datas);
		tcpProtocol.setCommand(Constants.TCP.COMMAND_MONITOR_FINISH);
//        HealthTcpClient.getInstance(mContext).sendMonitorFinish(tcpProtocol);
	}

    @Override
    public void deviceParamSet(String paramCode,String paramValues) {
        LogUtil.i("debug", "====HealthModelImpl==================deviceParamsSet===paramCode:" 
    + paramCode + " ---paramValues:" + paramValues);
        if(paramCode.equals(Constants.DEVICE_PARAM.COMMON_CODE)){
            Utils.saveDeviceCommonParams(mContext, paramValues);
        }else if(paramCode.equals(Constants.DEVICE_PARAM.CONNECT_CODE)){
            Utils.saveDeviceConnectParams(mContext, paramValues);
        }else if(paramCode.equals(Constants.DEVICE_PARAM.FREQUENCY_CODE)){
            Utils.saveDeviceFrequencyParams(mContext, paramValues);
        }else if(paramCode.equals(Constants.DEVICE_PARAM.HEALTH_CODE)){
            Utils.saveDeviceHealthParams(mContext, paramValues);
        }else if(paramCode.equals(Constants.DEVICE_PARAM.ALL_CODE)){
            Utils.saveDeviceAllParams(mContext, paramValues);
        }
        sendDeviceParamSet();
    }

    @Override
    public void deviceParamGet(String paramCode) {
        LogUtil.i("debug", "=====HealthModelImpl================deviceParamGet======paramCode:" + paramCode);
        TcpProtocol tcpProtocol = new TcpProtocol();
        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        Map<String, Object> timeMap = new HashMap<String, Object>();
        timeMap.put("time", System.currentTimeMillis());
        datas.add(timeMap);
        Map<String, Object> codeMap = new HashMap<String, Object>();
        codeMap.put("code", paramCode);
        LogUtil.i("debug", "=====HealthModelImpl================deviceParamGet======paramCode:" + paramCode);
        datas.add(codeMap);
        Map<String, Object> valueMap = new HashMap<String, Object>();
        valueMap.put("value", PreferencesUtils.getString(mContext, paramCode, ""));
        LogUtil.i("debug", "=====HealthModelImpl================deviceParamGet======paramValue:" + PreferencesUtils.getString(mContext, paramCode, ""));
        datas.add(valueMap);
        tcpProtocol.setDatas(datas);
        tcpProtocol.setCommand(Constants.TCP.COMMAND_PARAM_GET);
//        HealthTcpClient.getInstance(mContext).sendDeviceParamGet(tcpProtocol);
    }

    @Override
    public void sendDeviceParamSet() {
        LogUtil.i("debug", "=====HealthModelImpl================sendDeviceParamSet======");
        TcpProtocol tcpProtocol = new TcpProtocol();
        tcpProtocol.setCommand(Constants.TCP.COMMAND_PARAM_SET);
//        HealthTcpClient.getInstance(mContext).sendDeviceParamSet(tcpProtocol);
    }

    @Override
    public void sendDeviceOperation() {
        TcpProtocol tcpProtocol = new TcpProtocol();
        tcpProtocol.setCommand(Constants.TCP.COMMAND_PARAM_SET);
//        HealthTcpClient.getInstance(mContext).sendDeviceParamSet(tcpProtocol);
    }

}
