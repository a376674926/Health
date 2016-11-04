
package cn.stj.fphealth.model;

import java.util.ArrayList;
import java.util.List;

import cn.stj.fphealth.entity.HeartRate;
import cn.stj.fphealth.entity.Pedometer;
import cn.stj.fphealth.entity.RemindInfo;
import cn.stj.fphealth.service.HealthListener;
import cn.stj.fphealth.tcp.mina.TcpProtocol;

/**
 * @author hhj@20160804
 */
public interface HealthModel {

    /**
     * send heart beat data to server
     */
    public void sendHeartBeat(int status);

    /**
     * send heart rate data to server
     */
    public void sendHeartRate(Object data);
    
    /**
     * The device is first turned on (unbound) and say hello to the server, the
     * server returns the first binding information
     */
    public void sendDeviceHello(HealthListener listener);

    /**
     * send device bind confirm to server
     * 
     * @param status
     * @param listener
     */
    public void sendDeviceBind(int status, HealthListener listener);

    /**
     * send device unbind confirm to server
     */
    public void sendDeviceUnBind();

    /**
     * send pedometer info to server
     * 
     * @param pedometers
     */
    public void sendWalkUpload(List<Pedometer> pedometers);

    /**
     * send blood pressue upload to server
     */
    public void sendBloodPressueUpload(List<HeartRate> heartRates);
    
    /**
     * get the specified user info from server by the userId
     */
    public void sendUserInfoGet(int userId);
    
    /**
     * After Server push user info to the device,
     * send response data(status is 0,data is null) to server 
     */
    public void sendUserInfoPush();
    
    /**
     * server push one or more remind info to the device
     * @param receTcpProtocol
     */
    public void remindAdd(ArrayList<RemindInfo> remindInfos);
    
    /**
     * server push one or more remind info to the device
     * for edit the cached remind info
     * @param receTcpProtocol
     */
    public void remindEdit(ArrayList<RemindInfo> remindInfos);
    
    /**
     * server push one or more remind info to the device
     * for delete the cached remind info
     * @param receTcpProtocol
     */
    public void remindDel(ArrayList<RemindInfo> remindInfos);
    
    /**
     * After device remind success,
     * send the remind success notice to server
     * @param remindInfo
     */
    public void sendRemindSuccessNotice(RemindInfo remindInfo);
    
    /**
     * the device collect location data in accordance with the acquisition frequency,
     * and then upload the collected location data to server 
     * in accordance with the upload frequency
     */
    public void sendLocationUpload();
    
    public void sendLocationQuery();
    
    public void sendLocationTrrige();
    
    public void sendConstactsSet();
    
    public void sendConstactsQuery();
    
    public void sendMonitor();
    
    public void sendMonitorFinish();
    
    public void deviceParamSet(String paramCode,String paramValues);
    
    public void deviceParamGet(String paramCode);
    
    public void sendDeviceParamSet();
    
    public void sendDeviceOperation();

}
