package cn.stj.fphealth.http;

/**
 * 账户相关接口相应结果封装类
 */
public class ServerIpResponse {

    private int status;
    private String ip;
    private String errMsg;
    
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }
    public String getErrMsg() {
        return errMsg;
    }
    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    
}
