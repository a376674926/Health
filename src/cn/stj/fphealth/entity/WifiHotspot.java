package cn.stj.fphealth.entity;

import org.litepal.crud.DataSupport;

public class WifiHotspot extends DataSupport{

    private String mac;
    private String macName;
    private int signal;
    public String getMac() {
        return mac;
    }
    public void setMac(String mac) {
        this.mac = mac;
    }
    public String getMacName() {
        return macName;
    }
    public void setMacName(String macName) {
        this.macName = macName;
    }
    public int getSignal() {
        return signal;
    }
    public void setSignal(int signal) {
        this.signal = signal;
    }

}
