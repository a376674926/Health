package cn.stj.fphealth.http;

import cn.stj.fphealth.app.FPHealthApplication;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * 获取Volley的RequestQueue实例，为了确保登录了都有sessioid
 */
public class VolleyUtil {
    private static RequestQueue requestQueue;

    public static RequestQueue newRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(FPHealthApplication.getInstance(), null);
        }
        return requestQueue;
    }

    public static <T> void addToRequestQueue(Request<T> req) {
        if (requestQueue == null)
            newRequestQueue();
        requestQueue.add(req);
    }

    /**
     * 取消指定标记的请求
     * @param tag
     */
    public static void cancleRequest(Object tag) {
        newRequestQueue().cancelAll(tag);
    }

    /**
     * 取消所有请求
     */
    public static void cancleAllRequest() {
        newRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }
}
