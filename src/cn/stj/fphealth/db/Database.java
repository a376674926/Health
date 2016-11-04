package cn.stj.fphealth.db;

import cn.stj.fphealth.entity.HeartRate;
import cn.stj.fphealth.entity.LocationInfo;
import cn.stj.fphealth.entity.Mobile;
import cn.stj.fphealth.entity.Pedometer;
import cn.stj.fphealth.entity.RemindInfo;
import cn.stj.fphealth.entity.Wifi;

import java.util.List;

public interface Database {
    public List<LocationInfo> getGPSInfo();
    public List<Wifi> getWifiInfo();
    public List<Mobile> getMobileInfo();
    public void deleteGPSInfo();
    public void deleteWifiInfo();
    public void deleteMobileInfo();
    public void saveRemindInfo(List<RemindInfo> remindInfos);
    public void updateRemindInfo(List<RemindInfo> remindInfos);
    public void deleteRemindInfo(List<RemindInfo> remindInfos);
    //获取所有提醒信息
    public List<RemindInfo> getRemindInfos();
    //获取已经发起提醒的提醒信息
    public List<RemindInfo> getRemindedInfos();
    public List<RemindInfo> getRemindedInfoById(int remindId);
    public void deleteAllRemindInfo();
    public void deleteAllPedometer();
    //获取心率数据
    public List<HeartRate> getHeartRates();
    public void deleteAllHeartRate();
    //获取计步数据
    public List<Pedometer> getPedometers();
}
