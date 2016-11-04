package cn.stj.fphealth.http;

import android.graphics.Bitmap;

/**
 * 对外提供执行请求回调接口
 */
public interface ImageRequestListener {

    /**
     * 在请求之前调用的方法
     */
    public  void onPreRequest();

    /**
     * 请求成功调用
     * @param response
     */
    public  void onRequestSuccess(Bitmap bitmap);

    /**
     * 请求失败调用 404等
     * @param code
     * @param msg
     */
    public  void onRequestError(int code, String msg);

    /**
     * 根据服务器返回的code,判断失败时调用，1-成功 0-失败 -1-异常 -2-未登陆
     * @param code
     * @param msg
     */
    public  void onRequestFail(int code, String msg);
}
