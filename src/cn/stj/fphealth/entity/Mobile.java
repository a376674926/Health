package cn.stj.fphealth.entity;

import org.litepal.crud.DataSupport;

import java.util.List;

public class Mobile extends DataSupport {

    private int id;
    private String serverIp;
    private String network;
    private String mcc;
    private String mnc;
    private String time;
    private List<MobileBaseStation> mobileBaseStations;
    public String getServerIp() {
        return serverIp;
    }
    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }
    public String getNetwork() {
        return network;
    }
    public void setNetwork(String network) {
        this.network = network;
    }
    public String getMcc() {
        return mcc;
    }
    public void setMcc(String mcc) {
        this.mcc = mcc;
    }
    public String getMnc() {
        return mnc;
    }
    public void setMnc(String mnc) {
        this.mnc = mnc;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public List<MobileBaseStation> getMobileBaseStations() {
        return mobileBaseStations;
    }
    public void setMobileBaseStations(List<MobileBaseStation> mobileBaseStations) {
        this.mobileBaseStations = mobileBaseStations;
    }
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    @Override
    public String toString() {
        return "Mobile [serverIp=" + serverIp + ", network=" + network + ", mcc=" + mcc + ", mnc="
                + mnc + ", time=" + time + ", mobileBaseStations=" + mobileBaseStations + "]";
    }
    
}
