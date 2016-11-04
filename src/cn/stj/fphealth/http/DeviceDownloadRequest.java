package cn.stj.fphealth.http;

import android.text.TextUtils;
import android.util.Log;

import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.util.LogUtil;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

public class DeviceDownloadRequest<T> extends Request<DeviceDownloadResponse> {

    private Response.Listener<DeviceDownloadResponse> listener;
    private Map<String, String> mParams;
    private String url;

    public DeviceDownloadRequest(int method, String url, Map<String, String> params, Response.Listener listener, Response.ErrorListener errorlistener) {
        super(method, url, errorlistener);
        this.listener = listener;
        this.mParams = params;
        this.url = url;
    }

    @Override
    protected Response<DeviceDownloadResponse> parseNetworkResponse(NetworkResponse networkResponse) {
        DeviceDownloadResponse deviceDownloadResponse = null;
        Map<String, String> headerMap = networkResponse.headers;
        if(headerMap != null){
            deviceDownloadResponse = new DeviceDownloadResponse();
            if(headerMap.containsKey("Content-Disposition")){
                deviceDownloadResponse.setExitVoiceStream(true);
                deviceDownloadResponse.setOstream(networkResponse.data);
            }else{
                try {
                    String jsonString = new String(networkResponse.data,
                            HttpHeaderParser.parseCharset(networkResponse.headers));
                    deviceDownloadResponse = parseJson(jsonString);
                    deviceDownloadResponse.setExitVoiceStream(false);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Log.d(VolleyClient.TAG,"parseNetworkResponse fail");
                    Log.w(VolleyClient.TAG,""+e.getLocalizedMessage());
                }
            }
        }
        return Response.success(deviceDownloadResponse, HttpHeaderParser.parseCacheHeaders(networkResponse));
    }

    @Override
    protected void deliverResponse(DeviceDownloadResponse accountResponse) {
        listener.onResponse(accountResponse);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }
    
    private DeviceDownloadResponse parseJson(String json) {
        showLog(url,mParams,json);
        int status = -1;
        String prompt1 = "";
        String prompt2 = "";
        if (TextUtils.isEmpty(json)) {
            LogUtil.d("debug", "the Request:" + url + " return json is null");
            DeviceDownloadResponse deviceDownloadResponse = new DeviceDownloadResponse();
            deviceDownloadResponse.setStatus(-1);
            deviceDownloadResponse.setPrompt(prompt1);
            return deviceDownloadResponse;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            status = jsonObject.has("status") ? jsonObject.getInt("status") : -1;
            prompt1 = jsonObject.has("0") ? jsonObject.getString("0") : "";
            prompt2 = jsonObject.has("1") ? jsonObject.getString("1") : "";
            
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DeviceDownloadResponse deviceDownloadResponse = new DeviceDownloadResponse();
        deviceDownloadResponse.setStatus(status);
        deviceDownloadResponse.setPrompt(!prompt1.equals("")?prompt1:!prompt2.equals("")?prompt2:"");
        return deviceDownloadResponse;
    }
    
    /**
     * 显示请求log
     * @param url
     * @param params
     */
    private void showLog(String url, Map<String, String> params,String json) {
        if (!Constants.IS_SHOW_REQUEST_LOG) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("the Request:");
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
        buffer.append(" return json = " + json);
        LogUtil.i("debug", buffer.toString());
    }


}
