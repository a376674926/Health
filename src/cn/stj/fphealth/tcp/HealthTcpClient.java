
package cn.stj.fphealth.tcp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.db.Database;
import cn.stj.fphealth.db.LitepalDatabaseImpl;
import cn.stj.fphealth.entity.RemindInfo;
import cn.stj.fphealth.model.HealthModel;
import cn.stj.fphealth.model.HealthModelImpl;
import cn.stj.fphealth.tcp.mina.TcpProtocol;
import cn.stj.fphealth.util.BytesUtil;
import cn.stj.fphealth.util.JsonUtil;
import cn.stj.fphealth.util.LogUtil;
import cn.stj.fphealth.util.NetworkUtil;
import cn.stj.fphealth.util.PreferencesUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hhj@20160808
 */
public class HealthTcpClient {
    private static final String TAG = HealthTcpClient.class.getSimpleName();
    private static final String HOST = "120.25.160.36";
    private static final int PORT = 6998;
    private static final int READ_TIME_OUT = 60 * 1000;

    private static final int MESSAGE_HEADER_LEN = 57;
    private static final int DATA_KEY_LENGTH = 4;
    private static final int DATA_VALUE_LENGTH = 4;

    private volatile static HealthTcpClient mInstance;
    private Context mContext;
    private static Socket mSocket;
    private static final String END = "##_**";
    private List<TcpClientListener> mListeners = new ArrayList<TcpClientListener>();
    private boolean running = false;
    private HealthModel mHealthModel;
    private boolean mHasNewExcetption;
    private boolean mHasRequestExcetption;
    private boolean mHasReceExcetption;
    private Database mDatabase = new LitepalDatabaseImpl();

    public static HealthTcpClient getInstance(Context context) {
        if (mInstance == null || mSocket == null) {
            synchronized (HealthTcpClient.class) {
                if (mInstance == null || mSocket == null) {
                    mInstance = new HealthTcpClient(context);
                }
            }
        }
        return mInstance;
    }

    private HealthTcpClient(Context context) {
        this.mContext = context;
        mHealthModel = new HealthModelImpl(context);
        try {
            mSocket = new Socket(HOST, PORT);
            mSocket.setSoTimeout(READ_TIME_OUT);
            running = true;
            new Thread(new ReceiveMessageThread()).start();
            mHasNewExcetption = false;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            mHasNewExcetption = true;
            LogUtil.i("debug", "==========HealthTcpClient==========UnknownHostException======e:" + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            mHasNewExcetption = true;
            LogUtil.i("debug", "==========HealthTcpClient==========IOException======e:" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mHasNewExcetption = true;
            LogUtil.i("debug", "==========HealthTcpClient==========Exception======e:" + e.getMessage());
        }
    }

    public synchronized void sendHeartBeat(TcpProtocol tcpProtocol) {
        LogUtil.i("debug", "==========HealthTcpClient==========sendHeartBeat======");
        tcpRequest(tcpProtocol, null);
    }

    public synchronized void sendHeartRate(TcpProtocol tcpProtocol) {
        tcpRequest(tcpProtocol, null);
    }

    public synchronized void sendDeviceHello(TcpProtocol tcpProtocol, TcpClientListener listener) {
        LogUtil.i("debug", "==========HealthTcpClient==========sendDeviceHello======");
        tcpRequest(tcpProtocol, listener);
    }

    public synchronized void sendDeviceParamSet(TcpProtocol tcpProtocol) {
        tcpRequest(tcpProtocol, null);
    }

    public synchronized void sendDeviceBind(TcpProtocol tcpProtocol, TcpClientListener listener) {
        LogUtil.i("debug", "==========HealthTcpClient==========sendDeviceBind======");
        tcpRequest(tcpProtocol, listener);
    }

    public synchronized void sendDeviceUnBind(TcpProtocol tcpProtocol) {
        LogUtil.i("debug", "==========HealthTcpClient==========sendDeviceUnBind======");
        tcpRequest(tcpProtocol, null);
    }

    public synchronized void sendWalkUpload(TcpProtocol tcpProtocol) {
        LogUtil.i("debug", "==========HealthTcpClient==========sendWalkUpload======");
        tcpRequest(tcpProtocol, null);
    }

    public synchronized void sendBloodPressueUpload(TcpProtocol tcpProtocol,
            TcpClientListener listener) {
        LogUtil.i("debug", "==========HealthTcpClient==========sendBloodPressueUpload======");
        tcpRequest(tcpProtocol, listener);
    }
    
    public synchronized void sendUserInfoGet(TcpProtocol tcpProtocol) {
        tcpRequest(tcpProtocol, null);
    }
    
    public synchronized void sendUserInfoPush(TcpProtocol tcpProtocol) {
        tcpRequest(tcpProtocol, null);
    }
    
    public synchronized void sendRemindSuccessNotice(TcpProtocol tcpProtocol) {
        tcpRequest(tcpProtocol, null);
    }
    
    public synchronized void sendLocationUpload(TcpProtocol tcpProtocol) {
        LogUtil.i("debug", "==========HealthTcpClient==========sendLocationUpload======");
        tcpRequest(tcpProtocol, null);
    }
    
    public synchronized void sendLocationQuery(TcpProtocol tcpProtocol) {
        LogUtil.i("debug", "==========HealthTcpClient==========sendLocationQuery======");
        tcpRequest(tcpProtocol, null);
    }
    
    public synchronized void sendLocationTrrige(TcpProtocol tcpProtocol) {
        LogUtil.i("debug", "==========HealthTcpClient==========sendLocationTrrige======");
        tcpRequest(tcpProtocol, null);
    }
    
    public synchronized void sendConstactsSet(TcpProtocol tcpProtocol) {
        tcpRequest(tcpProtocol, null);
    }
    
    public synchronized void sendConstactsQuery(TcpProtocol tcpProtocol) {
        tcpRequest(tcpProtocol, null);
    }
    
    public synchronized void sendMonitor(TcpProtocol tcpProtocol) {
        tcpRequest(tcpProtocol, null);
    }
    
    public synchronized void sendMonitorFinish(TcpProtocol tcpProtocol) {
        tcpRequest(tcpProtocol, null);
    }
    
    public synchronized void sendDeviceParamGet(TcpProtocol tcpProtocol) {
        tcpRequest(tcpProtocol, null);
    }

    public void tcpRequest(TcpProtocol tcpProtocol, TcpClientListener listener) {
        LogUtil.i("debug", "==========HealthTcpClient==========tcpRequest======");
        if (mHasNewExcetption && listener != null) {
            if (!NetworkUtil.checkNetwork(mContext)) {
                listener.onErrorCallBack(Constants.RESULT_ERROR_NET);
            } else {
                listener.onErrorCallBack(Constants.RESULT_ERROR_EXCEPTION);
            }
            return;
        }
        if (listener != null) {
            mListeners.add(listener);
        }
        if (mSocket != null) {
            try {
                OutputStream os = mSocket.getOutputStream();
                DataOutputStream out = new DataOutputStream(os);
                LogUtil.i("debug", "=======tcpRequest==============isConnected:" + mSocket.isConnected());
                if (mSocket.isConnected()) {
                    byte[] dataBytes = packetSendMessage(tcpProtocol);
                    out.write(dataBytes);
                    out.flush();
                }
                mHasRequestExcetption = false;
            } catch (UnknownHostException e) {
                e.printStackTrace();
                mHasRequestExcetption = true;
                LogUtil.i("debug", "=====HealthTcpClient===============UnknownHostException:" + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                mHasRequestExcetption = true;
                LogUtil.i("debug", "=====HealthTcpClient===============IOException:" + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                mHasRequestExcetption = true;
                LogUtil.i("debug", "=====HealthTcpClient===============Exception:" + e.getMessage());
            }
            if (mHasRequestExcetption) {
                stopReceive();
                if(listener != null){
                    listener.onErrorCallBack(Constants.RESULT_ERROR_EXCEPTION);
                    mListeners.remove(listener);
                    return;  
                }
            }
        }
    }

    class ReceiveMessageThread implements Runnable {
        public void run() {
            while (running) {
                    TcpClientListener listener = null;
                    try {
                        if (mSocket != null) {
                            InputStream is = mSocket.getInputStream();
                            if (is.available() > 0) {
                                TcpProtocol receTcpProtocol = unpackReceiveMessage(is);
                                if (mListeners.size() > 0) {
                                    listener = mListeners.get(0);
                                }
                                if (listener != null) {
                                    listener.onSuccessCallBack(receTcpProtocol);
                                    mListeners.remove(listener);
                                }
                                switch (receTcpProtocol.getCommand()) {
                                    case Constants.TCP.COMMAND_USER_INFO_PUSH:
                                        mHealthModel.sendUserInfoPush();
                                        break;
                                    case Constants.TCP.COMMAND_REMIND_ADD:
                                        LogUtil.i("debug", "===@@@=HealthTcpClient=====COMMAND_REMIND_ADD===@@@@==");
                                        remindAdd(receTcpProtocol);
                                        break;
                                    case Constants.TCP.COMMAND_REMIND_EDIT:
                                        remindEdit(receTcpProtocol);
                                        break;
                                    case Constants.TCP.COMMAND_REMIND_DEL:
                                        remindDelete(receTcpProtocol);
                                        break;
                                    case Constants.TCP.COMMAND_LOCATION_QUERY:
                                        mHealthModel.sendLocationQuery();
                                        cleanLocationData();
                                        break;
                                    case Constants.TCP.COMMAND_LOCATION_TRRIGE:
                                        mHealthModel.sendLocationTrrige();
                                        cleanLocationData();
                                        break;
                                    case Constants.TCP.COMMAND_LOCATION_UPLOAD:
                                        cleanLocationData();
                                        break;
                                    case Constants.TCP.COMMAND_CONTACTS_SET:
                                        mHealthModel.sendConstactsSet();
                                        break;
                                    case Constants.TCP.COMMAND_CONTACTS_QUERY:
                                        mHealthModel.sendConstactsQuery();
                                        break;
                                    case Constants.TCP.COMMAND_MONITOR:
                                		startMonitor(receTcpProtocol);
                                        break;
                                    case Constants.TCP.COMMAND_DEVICE_UNBIND:
                                        clearUserInfo();
                                        mHealthModel.sendDeviceUnBind();
                                        break;
                                    case Constants.TCP.COMMAND_PARAM_GET:
                                        deviceParamsGet(receTcpProtocol);
                                        break;
                                    case Constants.TCP.COMMAND_PARAM_SET:
                                        deviceParamsSet(receTcpProtocol);
                                        break;
                                    default:
                                        break;
                                }
                            } else {
                                Thread.sleep(10);
                            }
                        }
                        mHasReceExcetption = false;
                    } catch (Exception e) {
                        mHasReceExcetption = true;
                        LogUtil.i("debug", "=======HttpTcpClient===========ReceiveMessage======Exception:" + e.getMessage());
                        e.printStackTrace();
                    }
                    if (mHasReceExcetption && listener != null) {
                        listener.onErrorCallBack(Constants.RESULT_ERROR_EXCEPTION);
                        mListeners.remove(listener);
                    }
                }
            }
    }

    public byte[] packetSendMessage(TcpProtocol tcpProtocol) {
        int position = 0;
        int dataLength = 0;
        byte[] byteData = null;
        List<Map<String, Object>> datas = tcpProtocol.getDatas();
        List<byte[]> dataKeys = new ArrayList<byte[]>();
        List<byte[]> dataValues = new ArrayList<byte[]>();
        for (int i = 0; i < datas.size(); i++) {
            Map<String, Object> dataMap = datas.get(i);
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                byte[] byteKeys = BytesUtil.stringToBytes(entry.getKey());
                byte[] byteValues = BytesUtil.stringToBytes(String.valueOf(entry.getValue()));
                if (byteKeys.length != 0 && byteValues.length != 0) {
                    dataLength += DATA_KEY_LENGTH + DATA_VALUE_LENGTH
                            + byteKeys.length + byteValues.length;
                    dataKeys.add(byteKeys);
                    dataValues.add(byteValues);
                }

            }
        }
        byte[] byteEnds = BytesUtil.stringToBytes(tcpProtocol.getEndFlag());

        int sendDataLength = MESSAGE_HEADER_LEN + byteEnds.length;
        if (dataLength != 0) {
            sendDataLength += dataLength;
        }
        byte[] mSendData = new byte[sendDataLength];

        byteData = new byte[] {
                BytesUtil.intToByte(tcpProtocol.getEncode())
        };
        System.arraycopy(byteData, 0, mSendData, position, byteData.length);
        position += byteData.length;

        byteData = new byte[] {
                BytesUtil.intToByte(tcpProtocol.getEncrypt())
        };
        System.arraycopy(byteData, 0, mSendData, position, byteData.length);
        position += byteData.length;

        byteData = new byte[] {
                BytesUtil.intToByte(tcpProtocol.getVersion())
        };
        System.arraycopy(byteData, 0, mSendData, position, byteData.length);
        position += byteData.length;

        byteData = new byte[] {
                BytesUtil.intToByte(tcpProtocol.getClientType())
        };
        System.arraycopy(byteData, 0, mSendData, position, byteData.length);
        position += byteData.length;

        byteData = new byte[] {
                BytesUtil.intToByte(tcpProtocol.getExtend())
        };
        System.arraycopy(byteData, 0, mSendData, position, byteData.length);
        position += byteData.length;

        byteData = BytesUtil.longToBytes(tcpProtocol.getSessionId());
        System.arraycopy(byteData, 0, mSendData, position, byteData.length);
        position += byteData.length;

        byteData = BytesUtil.intToBytes(tcpProtocol.getStatus());
        System.arraycopy(byteData, 0, mSendData, position, byteData.length);
        position += byteData.length;

        byteData = BytesUtil.intToBytes(tcpProtocol.getCommand());
        System.arraycopy(byteData, 0, mSendData, position, byteData.length);
        position += byteData.length;

        byteData = BytesUtil.stringToBytes(tcpProtocol.getToken());
        System.arraycopy(byteData, 0, mSendData, position, byteData.length);
        position += byteData.length;

        byteData = BytesUtil.intToBytes(dataLength);
        System.arraycopy(byteData, 0, mSendData, position, byteData.length);
        position += byteData.length;

        for (int i = 0; i < dataKeys.size(); i++) {
            byteData = BytesUtil.intToBytes(dataKeys.get(i).length);
            System.arraycopy(byteData, 0, mSendData, position, byteData.length);
            position += byteData.length;

            byteData = dataKeys.get(i);
            System.arraycopy(byteData, 0, mSendData, position, byteData.length);
            position += byteData.length;

            byteData = BytesUtil.intToBytes(dataValues.get(i).length);

            System.arraycopy(byteData, 0, mSendData, position, byteData.length);
            position += byteData.length;

            byteData = dataValues.get(i);
            System.arraycopy(byteData, 0, mSendData, position, byteData.length);
            position += byteData.length;
        }

        byteData = byteEnds;
        System.arraycopy(byteData, 0, mSendData, position, byteData.length);

        return mSendData;
    }

    private TcpProtocol unpackReceiveMessage(InputStream is) throws IOException,Exception {
        DataInputStream mInputStream = new DataInputStream(is);

        byte encode = mInputStream.readByte();
        byte encrypt = mInputStream.readByte();
        byte version = mInputStream.readByte();
        byte clientType = mInputStream.readByte();
        byte extend = mInputStream.readByte();
        long sessionId = mInputStream.readLong();
        int status = mInputStream.readInt();
        int command = mInputStream.readInt();

        byte[] token = new byte[32];
        mInputStream.read(token, 0, 32);
        String tokenStr = new String(token, 0, 32);
        int length = mInputStream.readInt();
        
        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        while (mInputStream.available() > END.length()) {
            // key
            int keyLen = mInputStream.readInt();
            byte[] keyBytes = new byte[keyLen];
            mInputStream.read(keyBytes, 0, keyLen);
            String key = new String(keyBytes);

            // value
            int valLen = mInputStream.readInt();
            byte[] valueBytes = new byte[valLen];
            mInputStream.read(valueBytes, 0, valLen);
            String value = new String(valueBytes);
            if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(key, value);
                datas.add(map);
            }
        }

        byte[] end = new byte[5];
        mInputStream.read(end, 0, 5);
        String endStr = new String(end, 0, 5);

        TcpProtocol tcpProtocol = new TcpProtocol();
        tcpProtocol.setClientType(clientType);
        tcpProtocol.setCommand(command);
        tcpProtocol.setDatas(datas);
        tcpProtocol.setEncode(encode);
        tcpProtocol.setEncrypt(encrypt);
        tcpProtocol.setExtend(extend);
        tcpProtocol.setLength(length);
        tcpProtocol.setSessionId(sessionId);
        tcpProtocol.setStatus(status);
        tcpProtocol.setToken(tokenStr);
        tcpProtocol.setVersion(version);
        LogUtil.i("debug", "=======unpackReceiveMessage======tcpProtocol:" + tcpProtocol.toString());
        return tcpProtocol;

    }

    public interface TcpClientListener {
        void onSuccessCallBack(TcpProtocol tcpProtocol);

        void onErrorCallBack(int errorCode);
    }

    public void stopReceive() {
        if (running)
            running = false;
        mInstance = null;
        if(mSocket != null && mSocket.isConnected()){
            LogUtil.i("debug", "======HealthTcpClient=======@@@@===stopReceive===");
            try {
                mSocket.close();
                mSocket = null;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void clearUserInfo() {
        PreferencesUtils.putInt(mContext, Constants.FROMID, -1);
        PreferencesUtils.putInt(mContext, Constants.CHATROOMID, -1);
        PreferencesUtils.putString(mContext, Constants.PHONENUMBER, null);
        PreferencesUtils.putString(mContext, Constants.NICKNAME, null);
        PreferencesUtils.putInt(mContext, Constants.USERID, -1);
        PreferencesUtils.putInt(mContext, Constants.DEVICEID, -1);
        PreferencesUtils.putString(mContext, Constants.FAMILYNAME, null);
        PreferencesUtils.putString(mContext, Constants.TOKEN, null);
        PreferencesUtils.putBoolean(mContext, Constants.IS_CONFIRM_BIND, false);
    }
    
    public ArrayList<RemindInfo> getRemindInfos(TcpProtocol receTcpProtocol){
        List<RemindInfo> remindInfos = new ArrayList<RemindInfo>();
        List<Map<String, Object>> datas = receTcpProtocol.getDatas();
        for (int i = 0; i < datas.size(); i++) {
            Map<String, Object> dataMap = datas.get(i);
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                if("data".equals(entry.getKey())){
                    remindInfos = JsonUtil.getDatas(entry.getValue().toString(), RemindInfo.class);
                }
            }
        }
        ArrayList<RemindInfo> remindInfoList = new ArrayList<RemindInfo>();
        for(int i= 0; i < remindInfos.size();i++){
            RemindInfo remindInfo = remindInfos.get(i);
            remindInfo.setRemindId(remindInfo.getId());
            remindInfoList.add(remindInfo);
        }
        LogUtil.i("debug", "===getRemindInfos=====RemindInfo=======" + remindInfos.get(0).toString());
        return remindInfoList;
    }
    
	private void startMonitor(TcpProtocol receTcpProtocol) {
		List<Map<String, Object>> datas = receTcpProtocol.getDatas();
		String phoneNumber = "" ;
		if(datas.size() > 0){
	        for (int i = 0; i < datas.size(); i++) {
	            Map<String, Object> dataMap = datas.get(i);
	            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
	                if(Constants.USERID.equals(entry.getKey())){
	                    PreferencesUtils.putInt(mContext, Constants.MONITOR_USERID, Integer.valueOf(entry.getValue().toString()));
	                }else{
	                	phoneNumber = entry.getValue().toString();
	                	PreferencesUtils.putString(mContext, Constants.MONITOR_PHONENUMBER, phoneNumber);
	                }
	            }
	        }
		}
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
        mContext.startActivity(intent);
	}
	
	private void deviceParamsSet(TcpProtocol receTcpProtocol){
	    LogUtil.i("debug", "====HttpTcpClient==================deviceParamsSet===");
	    List<Map<String, Object>> datas = receTcpProtocol.getDatas();
        String paramCode = "" ;
        String paramValue = "";
        if(datas.size() > 0){
            for (int i = 0; i < datas.size(); i++) {
                Map<String, Object> dataMap = datas.get(i);
                for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                    if(Constants.DEVICE_PARAM.CODE.equals(entry.getKey())){
                        paramCode = (String) entry.getValue();
                    }else{
                        paramValue = (String) entry.getValue();
                    }
                }
            }
        }
        if(!TextUtils.isEmpty(paramCode) && !TextUtils.isEmpty(paramValue)){
            mHealthModel.deviceParamSet(paramCode, paramValue);
        }
	}
	
	private void deviceParamsGet(TcpProtocol receTcpProtocol){
	    LogUtil.i("debug", "====HttpTcpClient==================deviceParamsGet===");
        List<Map<String, Object>> datas = receTcpProtocol.getDatas();
        String paramCode = "" ;
        if(datas.size() > 0){
            for (int i = 0; i < datas.size(); i++) {
                Map<String, Object> dataMap = datas.get(i);
                for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                    if(Constants.DEVICE_PARAM.CODE.equals(entry.getKey())){
                        paramCode = (String) entry.getValue();
                    }
                }
            }
        }
        if(!TextUtils.isEmpty(paramCode)){
            mHealthModel.deviceParamGet(paramCode);
        }
    }
	
	public void cleanLocationData(){
	    mDatabase.deleteGPSInfo();
	    mDatabase.deleteMobileInfo();
	    mDatabase.deleteWifiInfo();	
	}
	
	private void remindAdd(TcpProtocol receTcpProtocol) {
	    mHealthModel.remindAdd(getRemindInfos(receTcpProtocol));
    }
	
    private void remindDelete(TcpProtocol receTcpProtocol) {
        mHealthModel.remindDel(getRemindInfos(receTcpProtocol));
    }

    private void remindEdit(TcpProtocol receTcpProtocol) {
        mHealthModel.remindEdit(getRemindInfos(receTcpProtocol));
    }

}
