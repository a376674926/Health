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
public class BaseRequest<T> extends Request<T> {
    private Response.Listener<T> mListener;
    private Map<String, String> mParams;
    private String url;
    private final Class<T> mClazz; 

    public BaseRequest(int method, String url, Map<String, String> params, Class<T> clazz, Response.Listener<T> listener, Response.ErrorListener Errorlistener) {
        super(method, url, Errorlistener);
        this.url = url;
        mListener = listener;
        this.mParams = params;
        this.mClazz = clazz;
    }


    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            showLog(url,mParams,jsonString);
            return Response.success(JsonUtil.getData(jsonString, mClazz), HttpHeaderParser.parseCacheHeaders(response));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.d(VolleyClient.TAG,"parseNetworkResponse fail");
            Log.w(VolleyClient.TAG,""+e.getLocalizedMessage());
        }

        return null;
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
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
