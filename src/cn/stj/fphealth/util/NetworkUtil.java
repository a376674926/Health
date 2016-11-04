
package cn.stj.fphealth.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.entity.Mobile;
import cn.stj.fphealth.entity.MobileBaseStation;
import cn.stj.fphealth.entity.WifiHotspot;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author hhj@20160823
 */
public class NetworkUtil {

    /**
     * check network
     * 
     * @param context
     * @return true:network false:no network
     */
    public static boolean checkNetwork(Context context) {
        try {
            if (context == null)
                return false;
            ConnectivityManager mConnMgr = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (mConnMgr == null)
                return false;
            NetworkInfo aActiveInfo = mConnMgr.getActiveNetworkInfo();

            if (aActiveInfo != null && aActiveInfo.isConnected()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            // TODO: handle exception
            return false;
        }

    }

    /**
     * get wifi hotpspot list
     * 
     * @param context
     * @return
     */
    public static List<WifiHotspot> getWifiHotspots(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> scanResults = wifiManager.getScanResults();
        List<WifiHotspot> wifiHotspots = new ArrayList<WifiHotspot>();
        if(scanResults != null){
            for(int i = 0;i < scanResults.size();i++){
                ScanResult scanResult = scanResults.get(i);
                WifiHotspot wifiHotspot = new WifiHotspot();
                wifiHotspot.setMac(scanResult.BSSID);
                wifiHotspot.setMacName(scanResult.SSID);
                wifiHotspot.setSignal(scanResult.level);
                wifiHotspots.add(wifiHotspot);
            } 
        }
        return wifiHotspots;
    }

    /***
     * the gateway IP address
     * 
     * @return
     */
    public static String getGateWayIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> ipAddr = intf.getInetAddresses(); ipAddr
                        .hasMoreElements();) {
                    InetAddress inetAddress = ipAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress.getHostAddress().length() <= 16) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
}
