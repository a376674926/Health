package cn.stj.fphealth.http;

import android.text.TextUtils;

import cn.stj.fphealth.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 响应结果封装类
 * Created by jackey on 2015/6/26.
 */
public class DeviceSoftwareResponse {

    //异常
    public static final int ERROR = -1 ;
    //成功
    public static final int SUCCESS = 1 ;
    //失败
    public static final int FAIL = 0 ;

    //响应结果的返回状态
    private Integer status;
    //响应异常信息
    private String errMsg;
    
    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public String getErrMsg() {
        return errMsg;
    }
    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    
}
