
package cn.stj.fphealth.entity;

import org.litepal.crud.DataSupport;

public class MobileBaseStation extends DataSupport {

    private String lac;
    private String ci;
    private String rssi;

    public String getLac() {
        return lac;
    }

    public void setLac(String lac) {
        this.lac = lac;
    }

    public String getCi() {
        return ci;
    }

    public void setCi(String ci) {
        this.ci = ci;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

}
