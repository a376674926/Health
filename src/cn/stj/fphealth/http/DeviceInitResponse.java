package cn.stj.fphealth.http;


public class DeviceInitResponse {

    private int status;
    private String connect;
    private String common;
    private String frequency;
    private String health;
    private String errMsg;
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public String getConnect() {
        return connect;
    }
    public void setConnect(String connect) {
        this.connect = connect;
    }
    public String getCommon() {
        return common;
    }
    public void setCommon(String common) {
        this.common = common;
    }
    public String getFrequency() {
        return frequency;
    }
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
    public String getHealth() {
        return health;
    }
    public void setHealth(String health) {
        this.health = health;
    }
    public String getErrMsg() {
        return errMsg;
    }
    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
    @Override
    public String toString() {
        return "DeviceInitResponse [status=" + status + ", connect=" + connect + ", common="
                + common + ", frequency=" + frequency + ", health=" + health + ", errMsg=" + errMsg
                + "]";
    }

}
