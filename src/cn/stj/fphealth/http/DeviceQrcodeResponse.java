package cn.stj.fphealth.http;

/**
 * 账户相关接口相应结果封装类
 */
public class DeviceQrcodeResponse {

    private int status;
    private String prompt;
    private boolean isExitQrcodeStream;
    private byte[] ostream;

    public byte[] getOstream() {
        return ostream;
    }

    public void setOstream(byte[] ostream) {
        this.ostream = ostream;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public boolean isExitQrcodeStream() {
        return isExitQrcodeStream;
    }

    public void setExitQrcodeStream(boolean isExitQrcodeStream) {
        this.isExitQrcodeStream = isExitQrcodeStream;
    }
}
