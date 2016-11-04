package cn.stj.fphealth.db;

import org.litepal.crud.DataSupport;

import cn.stj.fphealth.entity.HeartRate;
import cn.stj.fphealth.entity.LocationInfo;
import cn.stj.fphealth.entity.Mobile;
import cn.stj.fphealth.entity.MobileBaseStation;
import cn.stj.fphealth.entity.Pedometer;
import cn.stj.fphealth.entity.RemindInfo;
import cn.stj.fphealth.entity.Wifi;
import cn.stj.fphealth.entity.WifiHotspot;
import cn.stj.fphealth.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class LitepalDatabaseImpl implements Database{
    
    public List<LocationInfo> getGPSInfo(){
        List<LocationInfo> gpsInfos = DataSupport.findAll(LocationInfo.class);
        return gpsInfos;
    }
    
    public List<Wifi> getWifiInfo(){
        List<Wifi> wifiInfos = DataSupport.findAll(Wifi.class); 
        for(int i = 0 ; i < wifiInfos.size();i++){
            Wifi wifi = wifiInfos.get(i);
            wifi.setWifiHotspots(getWifiHotspots(wifi.getId()));
        }
        return wifiInfos;
    }
    
    public List<Mobile> getMobileInfo(){
        List<Mobile> mobileInfos = DataSupport.findAll(Mobile.class); 
        for(int i = 0 ; i < mobileInfos.size();i++){
            Mobile mobile = mobileInfos.get(i);
            mobile.setMobileBaseStations(getMobiBaseStations(mobile.getId()));
        }
        return mobileInfos;
    }
    
    public List<WifiHotspot> getWifiHotspots(int id){
        List<WifiHotspot> wifiHotspots = DataSupport.where("wifi_id = ?", String.valueOf(id)).find(WifiHotspot.class); 
        return wifiHotspots;
    }
    
    public List<MobileBaseStation> getMobiBaseStations(int id){
        List<MobileBaseStation> mobileBaseStations = DataSupport.where("mobile_id = ?", String.valueOf(id)).find(MobileBaseStation.class); 
        return mobileBaseStations;
    }

    @Override
    public void deleteGPSInfo() {
        DataSupport.deleteAll(LocationInfo.class); 
    }

    @Override
    public void deleteWifiInfo() {
        DataSupport.deleteAll(Wifi.class);
    }

    @Override
    public void deleteMobileInfo() {
        DataSupport.deleteAll(Mobile.class);
    }

    @Override
    public void saveRemindInfo(List<RemindInfo> remindInfos) {
        for(int i = 0;i < remindInfos.size();i++){
            RemindInfo remindInfo = remindInfos.get(i);
            remindInfo.save();
        }
    }

    @Override
    public void updateRemindInfo(List<RemindInfo> remindInfos) {
        for(int i = 0;i < remindInfos.size();i++){
            RemindInfo newRemindInfo = remindInfos.get(i);
            RemindInfo remindInfo = new RemindInfo();
            remindInfo.setName(newRemindInfo.getName());
            remindInfo.setPeriodType(newRemindInfo.getPeriodType());
            remindInfo.setRemindId(newRemindInfo.getRemindId());
            remindInfo.setContent(newRemindInfo.getContent());
            remindInfo.setUserId(newRemindInfo.getUserId());
            remindInfo.setEndTime(newRemindInfo.getEndTime());
            remindInfo.setTime(newRemindInfo.getTime());
            remindInfo.setType(newRemindInfo.getType());
            remindInfo.updateAll("remindId = ?",newRemindInfo.getRemindId()+"");
        }
    }

    @Override
    public void deleteRemindInfo(List<RemindInfo> remindInfos) {
        for(int i = 0;i < remindInfos.size();i++){
            RemindInfo remindInfo = remindInfos.get(i);
            DataSupport.deleteAll(RemindInfo.class, "remindId = ?",remindInfo.getRemindId()+"");
        }
    }

    @Override
    public List<RemindInfo> getRemindInfos() {
        List<RemindInfo> remindInfos = DataSupport.findAll(RemindInfo.class); 
        return remindInfos;
    }

    @Override
    public List<RemindInfo> getRemindedInfos() {
        List<RemindInfo> remindInfos = DataSupport.where("time <= ?",System.currentTimeMillis()+"").order("time desc").find(RemindInfo.class); 
        return remindInfos;
    }

	@Override
	public List<RemindInfo> getRemindedInfoById(int remindId) {
		List<RemindInfo> remindInfos = DataSupport.where("remindId == ?",remindId+"").find(RemindInfo.class);
		return remindInfos;
	}

    @Override
    public void deleteAllRemindInfo() {
        DataSupport.deleteAll(RemindInfo.class);
    }

    @Override
    public void deleteAllPedometer() {
        DataSupport.deleteAll(Pedometer.class);
    }

	@Override
	public List<HeartRate> getHeartRates() {
		List<HeartRate> heartRates = new ArrayList<HeartRate>();
		heartRates = DataSupport.findAll(HeartRate.class); 
        return heartRates;
	}

	@Override
	public void deleteAllHeartRate() {
		DataSupport.deleteAll(HeartRate.class);
	}

	@Override
	public List<Pedometer> getPedometers() {
		List<Pedometer> pedometers = new ArrayList<Pedometer>();
		pedometers = DataSupport.findAll(Pedometer.class); 
        return pedometers;
	}
    
    
}
