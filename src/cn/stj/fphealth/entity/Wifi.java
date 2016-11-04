package cn.stj.fphealth.entity;

import org.litepal.crud.DataSupport;

import java.util.List;

public class Wifi extends DataSupport{

    private int id;
    private String time;
    private List<WifiHotspot> wifiHotspots;
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public List<WifiHotspot> getWifiHotspots() {
        return wifiHotspots;
    }
    public void setWifiHotspots(List<WifiHotspot> wifiHotspots) {
        this.wifiHotspots = wifiHotspots;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    @Override
    public String toString() {
        return "Wifi [time=" + time + ", wifiHotspots=" + wifiHotspots + "]";
    }
}
