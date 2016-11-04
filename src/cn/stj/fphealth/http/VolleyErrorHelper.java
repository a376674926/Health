/*
*VolleyErrorHelper.java
*Created on 2014-9-24 下午11:03 by Ivan
*Copyright(c)2014 Guangzhou Onion Information Technology Co., Ltd.
*http://www.cniao5.com
*/
package cn.stj.fphealth.http;

import android.content.Context;

import cn.stj.fphealth.R;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

public class VolleyErrorHelper {

    /**
     * Returns appropriate message which is to be displayed to the user against
     * the specified error object.
     *
     * @param error
     * @param context
     * @return
     */
    public static String getMessage(Context context,VolleyError error) {
        if (error instanceof TimeoutError) {
            return context.getResources().getString(R.string.server_error);
        } else if (isServerProblem(error)) {
            return handleServerError(context,error);
        } else if (isNetworkProblem(error)) {
            return context.getResources().getString(R.string.no_internet);
        }
        return context.getResources().getString(R.string.network_error);
    }

    /**
     * Determines whether the error is related to network
     *
     * @param error
     * @return
     */
    private static boolean isNetworkProblem(VolleyError error) {
        return (error instanceof NetworkError)
                || (error instanceof NoConnectionError);
    }

    /**
     * Determines whether the error is related to server
     * @param error
     * @return
     */
    private static boolean isServerProblem(VolleyError error) {
        return (error instanceof ServerError)
                || (error instanceof AuthFailureError);
    }

    /**
     * Handles the server error, tries to determine whether to show a stock
     * message or to show a message retrieved from the server.
     *
     * @param error
     * @param context
     * @return
     */
    private static String handleServerError(Context context,VolleyError error) {

        NetworkResponse response = error.networkResponse;

        if (response != null) {
            switch (response.statusCode) {
                case 404:
                case 422:
                case 401:
                    return  context.getResources().getString(R.string.resourse_error);
                default:
                    return context.getResources().getString(
                            R.string.server_error);
            }
        }
        return context.getResources().getString(R.string.network_error);
    }
}
