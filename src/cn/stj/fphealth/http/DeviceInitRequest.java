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

import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

/**
 * 请求封装类
 * Created by jackey on 2015/6/26.
 */
public class DeviceInitRequest<T> extends Request<DeviceInitResponse> {
    private Response.Listener<DeviceInitResponse> mListener;
    private Map<String, String> mParams;
    private String url;

    public DeviceInitRequest(int method, String url, Map<String, String> params, Response.Listener<DeviceInitResponse> listener, Response.ErrorListener Errorlistener) {
        super(method, url, Errorlistener);
        this.url = url;
        mListener = listener;
        this.mParams = params;
    }


    @Override
    protected Response<DeviceInitResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            DeviceInitResponse deviceInitResponse = parseJson(jsonString);
            return Response.success(deviceInitResponse, HttpHeaderParser.parseCacheHeaders(response));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.d(VolleyClient.TAG,"parseNetworkResponse fail");
            Log.w(VolleyClient.TAG,""+e.getLocalizedMessage());
        }

        return null;
    }

    @Override
    protected void deliverResponse(DeviceInitResponse deviceInitResponse) {
        mListener.onResponse(deviceInitResponse);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }

    private DeviceInitResponse parseJson(String json) {
        showLog(url,mParams,json);
        int status = -1;
        String connect = "";
        String common = "";
        String frequency = "";
        String health = "";
        String errMsg = "";
        if (TextUtils.isEmpty(json)) {
            Log.d(VolleyClient.TAG, "the Request:" + url + " return json is null");
            DeviceInitResponse deviceInitResponse = new DeviceInitResponse();
            deviceInitResponse.setStatus(-1);
            deviceInitResponse.setCommon("");
            deviceInitResponse.setConnect("");
            deviceInitResponse.setFrequency("");
            deviceInitResponse.setHealth("");
            deviceInitResponse.setErrMsg("服务器访问失败");
            return deviceInitResponse;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            status = jsonObject.has("status") ? jsonObject.getInt("status") : -1;
            connect = jsonObject.has("connect") ? jsonObject.getString("connect") : "";
            common = jsonObject.has("common") ? jsonObject.getString("common") : "";
            frequency = jsonObject.has("frequency") ? jsonObject.getString("frequency") : "";
            health = jsonObject.has("health") ? jsonObject.getString("health") : "";
            errMsg = jsonObject.has("errMsg") ? jsonObject.getString("errMsg") : "";
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DeviceInitResponse deviceInitResponse = new DeviceInitResponse();
        deviceInitResponse.setStatus(status);
        deviceInitResponse.setCommon(common);
        deviceInitResponse.setConnect(connect);
        deviceInitResponse.setFrequency(frequency);
        deviceInitResponse.setHealth(health);
        deviceInitResponse.setErrMsg(errMsg);
        return deviceInitResponse;
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
