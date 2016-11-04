package cn.stj.fphealth.http;

import android.text.TextUtils;
import android.util.Log;

import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.entity.ServerIp;
import cn.stj.fphealth.util.JsonUtil;
import cn.stj.fphealth.util.LogUtil;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

/**
 * 请求封装类
 * Created by jackey on 2015/6/26.
 */
public class ServiceIpRequest<T> extends Request<ServerIpResponse> {
    private Response.Listener<ServerIpResponse> mListener;
    private Map<String, String> mParams;
    private String url;

    public ServiceIpRequest(int method, String url, Map<String, String> params, Response.Listener<ServerIpResponse> listener, Response.ErrorListener Errorlistener) {
        super(method, url, Errorlistener);
        this.url = url;
        mListener = listener;
        this.mParams = params;
    }
    
    @Override
    protected Response<ServerIpResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            ServerIpResponse serverIpResponse = parseJson(jsonString);
            return Response.success(serverIpResponse, HttpHeaderParser.parseCacheHeaders(response));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.d(VolleyClient.TAG,"parseNetworkResponse fail");
            Log.w(VolleyClient.TAG,""+e.getLocalizedMessage());
        }

        return null;
    }

    @Override
    protected void deliverResponse(ServerIpResponse serverIpResponse) {
        mListener.onResponse(serverIpResponse);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }

    private ServerIpResponse parseJson(String json) {
        showLog(url,mParams,json);
        int status = -1;
        String ip = null;
        String errMsg = null;
        if (TextUtils.isEmpty(json)) {
            Log.d(VolleyClient.TAG, "the Request:" + url + " return json is null");
            ServerIpResponse response = new ServerIpResponse();
            response.setStatus(-1);
            response.setErrMsg("服务器访问失败");
            response.setIp("");
            return response;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            status = jsonObject.has("status") ? jsonObject.getInt("status") : -1;
            ip = jsonObject.has("ip") ? jsonObject.getString("ip") : "";
            errMsg = jsonObject.has("errMsg") ? jsonObject.getString("errMsg") : "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        ServerIpResponse response = new ServerIpResponse();
        response.setStatus(status);
        response.setErrMsg(errMsg);
        response.setIp(ip);
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
