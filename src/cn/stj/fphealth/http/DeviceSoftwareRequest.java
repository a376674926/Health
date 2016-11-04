package cn.stj.fphealth.http;

import android.text.TextUtils;
import android.util.Log;

import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.util.JsonUtil;
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

/**
 * 请求封装类
 * Created by jackey on 2015/6/26.
 */
public class DeviceSoftwareRequest<T> extends Request<DeviceSoftwareResponse> {
    private Response.Listener<DeviceSoftwareResponse> mListener;
    private Map<String, String> mParams;
    private String url;

    public DeviceSoftwareRequest(int method, String url, Map<String, String> params, Response.Listener<DeviceSoftwareResponse> listener, Response.ErrorListener Errorlistener) {
        super(method, url, Errorlistener);
        this.url = url;
        mListener = listener;
        this.mParams = params;
    }


    @Override
    protected Response<DeviceSoftwareResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            DeviceSoftwareResponse deviceSoftwareResponse = parseJson(jsonString);
            return Response.success(deviceSoftwareResponse, HttpHeaderParser.parseCacheHeaders(response));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.d(VolleyClient.TAG,"parseNetworkResponse fail");
            Log.w(VolleyClient.TAG,""+e.getLocalizedMessage());
        }

        return null;
    }

    @Override
    protected void deliverResponse(DeviceSoftwareResponse response) {
        mListener.onResponse(response);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }

    private DeviceSoftwareResponse parseJson(String json) {
        showLog(url,mParams,json);
        int status = -1;
        String errMsg = "";
        if (TextUtils.isEmpty(json)){
            Log.d(VolleyClient.TAG, "the Request:"+url+" return json is null");
            DeviceSoftwareResponse response = new DeviceSoftwareResponse();
            response.setStatus(-1);
            response.setErrMsg("服务器访问失败");
            return response;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            status = jsonObject.has("status") ? jsonObject.getInt("status") : -1;
            errMsg = jsonObject.has("errMsg") ? jsonObject.getString("errMsg") : "";//只有异常时才有有note

        } catch (JSONException e) {
            e.printStackTrace();
        }

        DeviceSoftwareResponse response = new DeviceSoftwareResponse();
        response.setStatus(status);
        response.setErrMsg(errMsg);
        return response;

    }
    
    /**
     * 正式发布后，必须将DEBUGE 设置为false
     *
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
