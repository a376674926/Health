package cn.stj.fphealth.http;


public class DeviceDownloadResponse {

    private int status;
    private String prompt;
    private boolean isExitVoiceStream;
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

    public boolean isExitVoiceStream() {
        return isExitVoiceStream;
    }

    public void setExitVoiceStream(boolean isExitVoiceStream) {
        this.isExitVoiceStream = isExitVoiceStream;
    }

}
