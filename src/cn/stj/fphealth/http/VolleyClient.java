
package cn.stj.fphealth.http;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;

import cn.stj.fphealth.R;
import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.app.FPHealthApplication;
import cn.stj.fphealth.util.LogUtil;
import cn.stj.fphealth.util.NetworkUtil;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import java.util.Iterator;
import java.util.Map;

/**
 * 执行http请求处理类 Created by jackey
 */
public class VolleyClient {
    public static final String TAG = "VolleyClient";

    public static final boolean ISLOCAL = true;

    /**
     * The default socket timeout in milliseconds
     */
    public static final int DEFAULT_TIMEOUT_MS = 5000;

    /**
     * The default number of retries
     */
    public static final int DEFAULT_MAX_RETRIES = 2;

    /**
     * The default backoff multiplier
     */
    public static final float DEFAULT_BACKOFF_MULT = 1f;

    private static VolleyClient mInstance;

    public static VolleyClient getInstance() {
        if (mInstance == null) {
            mInstance = new VolleyClient();
        }
        return mInstance;
    }

    private VolleyClient() {
        VolleyUtil.newRequestQueue();
    }

    /**
     * get 方式请求服务器ip
     * 
     * @param url
     * @param map
     * @param listener
     */
    public void serverIpGet(String url, final RequestListener<ServerIpResponse> listener) {
        serverIpRequest(Request.Method.GET, url, listener,null);
    }
    
    /**
     * get 方式请求设备初始化参数
     * 
     * @param url
     * @param map
     * @param listener
     */
    public void deviceInitGet(String url, final RequestListener<DeviceInitResponse> listener) {
        deviceInitRequest(Request.Method.GET, url, listener,null);
    }
    
    /**
     * get 方式请求下载二维码
     * 
     * @param url
     * @param map
     * @param listener
     */
    public void deviceQrcodeGet(String url, final RequestListener<DeviceQrcodeResponse> listener) {
        deviceQrcodeRequest(Request.Method.GET, url, listener,null);
    }
    
    /**
     * get 方式请求下载提醒语音资源
     * 
     * @param url
     * @param map
     * @param listener
     */
    public void remindVoiceGet(String url, final RequestListener<DeviceDownloadResponse> listener) {
        remindVoiceDownloadRequest(Request.Method.GET, url, listener,null);
    }
    
    /**
     * get 方式请求 设备软件版本上传
     * 
     * @param url
     * @param map
     * @param listener
     */
    public void deviceSoftwareGet(String url, final RequestListener<DeviceSoftwareResponse> listener) {
        deviceSoftwareRequest(Request.Method.GET, url, listener,null);
    }

    private void deviceSoftwareRequest(int method, String url, final RequestListener<DeviceSoftwareResponse> listener,
            Object tag) {
        if (!NetworkUtil.checkNetwork(FPHealthApplication.getInstance())) {
            if (listener != null) {
                listener.onRequestFail(
                        Constants.RESULT_ERROR_NET,
                        FPHealthApplication.getInstance().getResources()
                                .getString(R.string.network_conn_is_unavailable));
            }
            return;
        }
        showLog(url, null);
        if (listener != null)
            listener.onPreRequest();
        DeviceSoftwareRequest<DeviceSoftwareResponse> request = new DeviceSoftwareRequest<DeviceSoftwareResponse>(method, url, null,
                new Response.Listener<DeviceSoftwareResponse>() {
                    @Override
                    public void onResponse(DeviceSoftwareResponse baseResponse) {
                        if (baseResponse.getStatus() == 0)
                            listener.onRequestSuccess(baseResponse);
                        else
                            listener.onRequestFail(-1, baseResponse.getErrMsg());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (volleyError != null) {
                            volleyError.printStackTrace();
                        }
                        try {
                            Log.d("", "error = " + volleyError);
                            String errMsg = null;
                            int errCode = -1;
                            if (volleyError == null) {
                                errMsg = FPHealthApplication.getInstance().getResources()
                                        .getString(R.string.unknown_error);
                            } else {
                                errMsg = VolleyErrorHelper.getMessage(
                                        FPHealthApplication.getInstance(), volleyError);
                                errCode = volleyError.networkResponse == null ? errCode
                                        : volleyError.networkResponse.statusCode;
                            }
                            if (listener != null) {
                                listener.onRequestError(errCode, errMsg);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        request.setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES,
                DEFAULT_BACKOFF_MULT));
        if (tag != null) {
            request.setTag(tag);
        }
        VolleyUtil.addToRequestQueue(request);
    }

    private void deviceQrcodeRequest(int method,
            String url,
            final RequestListener<DeviceQrcodeResponse> listener, Object tag) {
        if (!NetworkUtil.checkNetwork(FPHealthApplication.getInstance())) {
            if (listener != null) {
                listener.onRequestFail(
                        Constants.RESULT_ERROR_NET,
                        FPHealthApplication.getInstance().getResources()
                                .getString(R.string.network_conn_is_unavailable));
            }
            return;
        }
        showLog(url, null);
        if (listener != null)
            listener.onPreRequest();
        DeviceQrcodeRequest<DeviceQrcodeResponse> request = new DeviceQrcodeRequest<DeviceQrcodeResponse>(method, url, null,
                new Response.Listener<DeviceQrcodeResponse>() {
                    @Override
                    public void onResponse(DeviceQrcodeResponse deviceQrcodeResponse) {
//                        if (deviceQrcodeResponse.getStatus() == 0)
                            listener.onRequestSuccess(deviceQrcodeResponse);
//                        else
//                            listener.onRequestFail(-1, deviceQrcodeResponse.getErrMsg());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (volleyError != null) {
                            volleyError.printStackTrace();
                        }
                        try {
                            Log.d("", "error = " + volleyError);
                            String errMsg = null;
                            int errCode = -1;
                            if (volleyError == null) {
                                errMsg = FPHealthApplication.getInstance().getResources()
                                        .getString(R.string.unknown_error);
                            } else {
                                errMsg = VolleyErrorHelper.getMessage(
                                        FPHealthApplication.getInstance(), volleyError);
                                errCode = volleyError.networkResponse == null ? errCode
                                        : volleyError.networkResponse.statusCode;
                            }
                            if (listener != null) {
                                listener.onRequestError(errCode, errMsg);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        request.setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES,
                DEFAULT_BACKOFF_MULT));
        if (tag != null) {
            request.setTag(tag);
        }
        VolleyUtil.addToRequestQueue(request);
    }
    
    private void remindVoiceDownloadRequest(int method,
            String url,
            final RequestListener<DeviceDownloadResponse> listener, Object tag) {
        if (!NetworkUtil.checkNetwork(FPHealthApplication.getInstance())) {
            if (listener != null) {
                listener.onRequestFail(
                        Constants.RESULT_ERROR_NET,
                        FPHealthApplication.getInstance().getResources()
                                .getString(R.string.network_conn_is_unavailable));
            }
            return;
        }
        showLog(url, null);
        if (listener != null)
            listener.onPreRequest();
        DeviceDownloadRequest<DeviceDownloadResponse> request = new DeviceDownloadRequest<DeviceDownloadResponse>(method, url, null,
                new Response.Listener<DeviceDownloadResponse>() {
                    @Override
                    public void onResponse(DeviceDownloadResponse deviceDownloadResponse) {
                         listener.onRequestSuccess(deviceDownloadResponse);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (volleyError != null) {
                            volleyError.printStackTrace();
                        }
                        try {
                            Log.d("", "error = " + volleyError);
                            String errMsg = null;
                            int errCode = -1;
                            if (volleyError == null) {
                                errMsg = FPHealthApplication.getInstance().getResources()
                                        .getString(R.string.unknown_error);
                            } else {
                                errMsg = VolleyErrorHelper.getMessage(
                                        FPHealthApplication.getInstance(), volleyError);
                                errCode = volleyError.networkResponse == null ? errCode
                                        : volleyError.networkResponse.statusCode;
                            }
                            if (listener != null) {
                                listener.onRequestError(errCode, errMsg);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        request.setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES,
                DEFAULT_BACKOFF_MULT));
        if (tag != null) {
            request.setTag(tag);
        }
        VolleyUtil.addToRequestQueue(request);
    }

  
    private void deviceInitRequest(int method,
            String url,
            final RequestListener<DeviceInitResponse> listener, Object tag) {
        if (!NetworkUtil.checkNetwork(FPHealthApplication.getInstance())) {
            if (listener != null) {
                listener.onRequestFail(
                        Constants.RESULT_ERROR_NET,
                        FPHealthApplication.getInstance().getResources()
                                .getString(R.string.network_conn_is_unavailable));
            }
            return;
        }
        showLog(url, null);
        if (listener != null)
            listener.onPreRequest();
        DeviceInitRequest<DeviceInitResponse> request = new DeviceInitRequest<DeviceInitResponse>(method, url, null,
                new Response.Listener<DeviceInitResponse>() {
                    @Override
                    public void onResponse(DeviceInitResponse deviceInitResponse) {
                        if (deviceInitResponse.getStatus() == 0)
                            listener.onRequestSuccess(deviceInitResponse);
                        else
                            listener.onRequestFail(-1, deviceInitResponse.getErrMsg());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (volleyError != null) {
                            volleyError.printStackTrace();
                        }
                        try {
                            Log.d("", "error = " + volleyError);
                            String errMsg = null;
                            int errCode = -1;
                            if (volleyError == null) {
                                errMsg = FPHealthApplication.getInstance().getResources()
                                        .getString(R.string.unknown_error);
                            } else {
                                errMsg = VolleyErrorHelper.getMessage(
                                        FPHealthApplication.getInstance(), volleyError);
                                errCode = volleyError.networkResponse == null ? errCode
                                        : volleyError.networkResponse.statusCode;
                            }
                            if (listener != null) {
                                listener.onRequestError(errCode, errMsg);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        request.setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES,
                DEFAULT_BACKOFF_MULT));
        if (tag != null) {
            request.setTag(tag);
        }
        VolleyUtil.addToRequestQueue(request);
    }

    /**
     * 利用Volley 账户相关的接口请求数据
     * 
     * @param method
     * @param url
     * @param map
     * @param listener
     */
    private void serverIpRequest(int method,
            String url,
            final RequestListener<ServerIpResponse> listener, Object tag) {
        if (!NetworkUtil.checkNetwork(FPHealthApplication.getInstance())) {
            if (listener != null) {
                listener.onRequestFail(
                        Constants.RESULT_ERROR_NET,
                        FPHealthApplication.getInstance().getResources()
                                .getString(R.string.network_conn_is_unavailable));
            }
            return;
        }
        showLog(url, null);
        if (listener != null)
            listener.onPreRequest();
        ServiceIpRequest<ServerIpResponse> request = new ServiceIpRequest<ServerIpResponse>(method, url, null,
                new Response.Listener<ServerIpResponse>() {
                    @Override
                    public void onResponse(ServerIpResponse serverIpResponse) {
                        if (serverIpResponse.getStatus() == 0)
                            listener.onRequestSuccess(serverIpResponse);
                        else
                            listener.onRequestFail(-1, serverIpResponse.getErrMsg());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (volleyError != null) {
                            volleyError.printStackTrace();
                        }
                        try {
                            Log.d("", "error = " + volleyError);
                            String errMsg = null;
                            int errCode = -1;
                            if (volleyError == null) {
                                errMsg = FPHealthApplication.getInstance().getResources()
                                        .getString(R.string.unknown_error);
                            } else {
                                errMsg = VolleyErrorHelper.getMessage(
                                        FPHealthApplication.getInstance(), volleyError);
                                errCode = volleyError.networkResponse == null ? errCode
                                        : volleyError.networkResponse.statusCode;
                            }
                            if (listener != null) {
                                listener.onRequestError(errCode, errMsg);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        request.setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES,
                DEFAULT_BACKOFF_MULT));
        if (tag != null) {
            request.setTag(tag);
        }
        VolleyUtil.addToRequestQueue(request);
    }

    /**
     * 正式发布后，必须将DEBUGE 设置为false
     * 
     * @param url
     * @param params
     */
    private void showLog(String url, Map<String, String> params) {
        if (!Constants.IS_SHOW_REQUEST_LOG) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("--->>request url:");
        buffer.append(url);
        if (params != null) {
            buffer.append(" request params[");
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                buffer.append(entry.getKey());
                buffer.append("=");
                buffer.append(entry.getValue());
                buffer.append(" ");
            }
            buffer.append("]");
        }
        LogUtil.i("debug", buffer.toString());
    }

    public void cancleRequest(Object tag) {
        Log.d(TAG, "cancleRequest tag-->" + tag);
        VolleyUtil.cancleRequest(tag);
    }

    /**
     * 销毁VolleyClient
     */
    public void destroy() {
        Log.d(TAG, "onDestroy");
        VolleyUtil.cancleAllRequest();
        mInstance = null;
    }
    
    /**
     * 利用Volley 请求数据
     *
     * @param method   Request.Method
     * @param url      api
     * @param params   请求参数
     * @param listener 请求监听
     */
    /*public void request(int method,
                        String url,
                        Map<String, String> params,
                        final RequestListener listener, Object tag) {
        if (!NetworkUtil.checkNetwork(FPHealthApplication.getInstance())) {
            if (listener != null) {
                listener.onRequestFail(Constants.RESULT_ERROR_NET, FPHealthApplication.getInstance().getResources().getString(R.string.network_conn_is_unavailable));
            }
            return;
        }

        showLog(url, params);

        if (listener != null)
            listener.onPreRequest();
        BaseRequest<String> request = new BaseRequest(
                method,
                url,
                params,
                new Response.Listener<BaseResponse>() {
                    public void onResponse(BaseResponse response) {

                        if (listener != null) {
                            if (response.getStatus() == 0)
                                listener.onRequestSuccess(response);
                            else
                                listener.onRequestFail(-1, response.getErrMsg());
                        }
                    }
                },

                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        if (error != null) {
                            error.printStackTrace();
                        }
                        try {
                            Log.d("", "error = " + error);
                            String errMsg = null;
                            int errCode = -1;
                            if (error == null) {
                                errMsg = FPHealthApplication.getInstance().getResources().getString(R.string.unknown_error);
                            } else {
                                errMsg = VolleyErrorHelper.getMessage(FPHealthApplication.getInstance(), error);
                                errCode = error.networkResponse == null ? errCode : error.networkResponse.statusCode;
                            }
                            if (listener != null) {
                                listener.onRequestError(errCode, errMsg);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
        );
        //重试参数设置
        request.setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES, DEFAULT_BACKOFF_MULT));
        if (tag != null) {
            request.setTag(tag);
        }
        VolleyUtil.addToRequestQueue(request)
    };*/
    
    /**
     * get 方式请求数据
     * @param url  api
     * @param listener 请求监听
     */
    public void imageGet(String url,
                    final ImageRequestListener listener) {
        imageRequest(Request.Method.GET, url, listener, null);
    }

    private void imageRequest(int method, String url, final ImageRequestListener listener,
            Object tag) {
        if (!NetworkUtil.checkNetwork(FPHealthApplication.getInstance())) {
            if (listener != null) {
                listener.onRequestFail(Constants.RESULT_ERROR_NET, FPHealthApplication.getInstance().getResources().getString(R.string.network_conn_is_unavailable));
            }
            return;
        }

        showLog(url, null);
        
        if (listener != null)
            listener.onPreRequest();
        
        ImageRequest imgRequest=new ImageRequest(url, new Response.Listener<Bitmap>() {  
            @Override  
            public void onResponse(Bitmap bitmap) {  
                listener.onRequestSuccess(bitmap);
            }  
        }, 300, 200, Config.ARGB_8888, new ErrorListener(){  
            @Override  
            public void onErrorResponse(VolleyError error) {  
                if (error != null) {
                    error.printStackTrace();
                }
                try {
                    Log.d("", "error = " + error);
                    String errMsg = null;
                    int errCode = -1;
                    if (error == null) {
                        errMsg = FPHealthApplication.getInstance().getResources().getString(R.string.unknown_error);
                    } else {
                        errMsg = VolleyErrorHelper.getMessage(FPHealthApplication.getInstance(), error);
                        errCode = error.networkResponse == null ? errCode : error.networkResponse.statusCode;
                    }
                    if (listener != null) {
                        listener.onRequestError(errCode, errMsg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
 
            }             
        });  
        //重试参数设置
        imgRequest.setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES, DEFAULT_BACKOFF_MULT));
        if (tag != null) {
            imgRequest.setTag(tag);
        }
        VolleyUtil.addToRequestQueue(imgRequest);
    }
    
    

}
