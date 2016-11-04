package cn.stj.fphealth.entity;

public class ServerIp {

    private int status;
    private String ip;
    
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
    @Override
    public String toString() {
        return "ServerIp [status=" + status + ", ip=" + ip + "]";
    }
    
    
}
