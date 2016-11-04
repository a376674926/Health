
package cn.stj.fphealth.tcp.mina;

import android.R.integer;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.app.FPHealthApplication;
import cn.stj.fphealth.util.BytesUtil;
import cn.stj.fphealth.util.LogUtil;
import cn.stj.fphealth.util.PreferencesUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lizhongyuan
 */
public class TcpProtocol implements Parcelable{
    private byte encode;
    private byte encrypt;
    private byte version;
    private byte clientType;
    private byte extend;
    private long sessionId;
    private int status;
    private int command;
    private String token;
    private int length;
    private List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
    private String end;
    private static final int ENCODE = 0;// data encoding（0：UTF-8，1：GBK）
    private static final int ENCRYPT = 0;// encryption（0：not 1：yes）
    private static final int VERSION = 1;// version
    private static final int CLIENTTYPE = 2;// clienttype
    private static final int EXTEND = 0;// extend（reserved）
    private static final long SESSIONID = 1;// sessionId
    private static final int STATUS = 0;// status（0：success，非0：fail）
    private static final int COMMAND = 20001;// command
    private static final String TOKEN = "22222222222222222222222222222222";// token
    private static final int LENGTH = 0;// dataLenth
    public static final String END = "##_**";
    public static final int MESSAGE_HEADER_LEN = 57;
    public static final int DATA_LENGTH = 4;
    public static final int DATA_KEY_LENGTH = 4;
    public static final int DATA_VALUE_LENGTH = 4;
    public static final int TOKEN_LENGTH = 32;
    public static final int END_LENGTH = 5;
    
    public TcpProtocol() {
        initTcpProtocolNoData();
    }

    private void initTcpProtocolNoData() {
        setEncode((byte) ENCODE);
        setEncrypt((byte) ENCRYPT);
        setVersion((byte) VERSION);
        setClientType((byte) CLIENTTYPE);
        setExtend((byte) EXTEND);
        setToken(PreferencesUtils.getString(FPHealthApplication.getContext(), Constants.TOKEN,TOKEN));
        setSessionId(System.currentTimeMillis());
        setCommand(COMMAND);
        setStatus(STATUS);
        setLength(LENGTH);
        setEnd(END);
    }

    public byte getEncode() {
        return encode;
    }

    public void setEncode(byte encode) {
        this.encode = encode;
    }

    public byte getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(byte encrypt) {
        this.encrypt = encrypt;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getClientType() {
        return clientType;
    }

    public void setClientType(byte clientType) {
        this.clientType = clientType;
    }

    public byte getExtend() {
        return extend;
    }

    public void setExtend(byte extend) {
        this.extend = extend;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public List<Map<String, Object>> getDatas() {
        return datas;
    }

    public void setDatas(List<Map<String, Object>> datas) {
        this.datas = datas;
    }
    
    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getEndFlag() {
        return END;
    }

    @Override
    public String toString() {
        return "TcpProtocol [encode=" + encode + ", encrypt=" + encrypt + ", version=" + version
                + ", clientType=" + clientType + ", extend=" + extend + ", sessionId=" + sessionId
                + ", status=" + status + ", command=" + command + ", token=" + token + ", length="
                + length + ", datas=" + datas +  ", end=" + end +"]";
    }

    public TcpProtocol(Parcel in){
        this.encode = in.readByte();
        this.encrypt = in.readByte();
        this.version = in.readByte();
        this.clientType = in.readByte();
        this.extend = in.readByte();
        this.sessionId = in.readLong();
        this.status = in.readInt();
        this.command = in.readInt();
        this.token = in.readString();
        this.length = in.readInt();
        this.datas = in.readArrayList(null);
        this.end = in.readString();
    }

    public static Parcelable.Creator<TcpProtocol> CREATOR = new Creator<TcpProtocol>() {
        @Override
        public TcpProtocol createFromParcel(Parcel source) {
            return new TcpProtocol(source);
        }

        @Override
        public TcpProtocol[] newArray(int size) {
            return new TcpProtocol[size];
        }
    };
    
    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(encode);
        dest.writeByte(encrypt);
        dest.writeByte(version);
        dest.writeByte(clientType);
        dest.writeByte(extend);
        dest.writeLong(sessionId);
        dest.writeInt(status);
        dest.writeInt(command);
        dest.writeString(token);
        dest.writeInt(length);
        dest.writeList(datas);
        dest.writeString(end);
    }
    
    /**
     * TcpProtocol对象转换成字节流
     * @return
     */
    public byte[] messageToBytes() {
        int position = 0;
        int dataLength = 0;
        byte[] byteData = null;
        List<Map<String, Object>> datas = getDatas();
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
        byte[] byteEnds = BytesUtil.stringToBytes(getEndFlag());

        int sendDataLength = MESSAGE_HEADER_LEN + byteEnds.length;
        if (dataLength != 0) {
            sendDataLength += dataLength;
        }
        byte[] mSendData = new byte[sendDataLength];

        byteData = new byte[] {
                BytesUtil.intToByte(getEncode())
        };
        System.arraycopy(byteData, 0, mSendData, position, byteData.length);
        position += byteData.length;

        byteData = new byte[] {
                BytesUtil.intToByte(getEncrypt())
        };
        System.arraycopy(byteData, 0, mSendData, position, byteData.length);
        position += byteData.length;

        byteData = new byte[] {
                BytesUtil.intToByte(getVersion())
        };
        System.arraycopy(byteData, 0, mSendData, position, byteData.length);
        position += byteData.length;

        byteData = new byte[] {
                BytesUtil.intToByte(getClientType())
        };
        System.arraycopy(byteData, 0, mSendData, position, byteData.length);
        position += byteData.length;

        byteData = new byte[] {
                BytesUtil.intToByte(getExtend())
        };
        System.arraycopy(byteData, 0, mSendData, position, byteData.length);
        position += byteData.length;

        byteData = BytesUtil.longToBytes(getSessionId());
        System.arraycopy(byteData, 0, mSendData, position, byteData.length);
        position += byteData.length;

        byteData = BytesUtil.intToBytes(getStatus());
        System.arraycopy(byteData, 0, mSendData, position, byteData.length);
        position += byteData.length;

        byteData = BytesUtil.intToBytes(getCommand());
        System.arraycopy(byteData, 0, mSendData, position, byteData.length);
        position += byteData.length;

        byteData = BytesUtil.stringToBytes(getToken());
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
    
    /**
     * 字节流转换成TcpProtocol对象主体数据
     * @param is
     * @return
     * @throws IOException
     * @throws Exception
     */
    public void datasFromBytes(byte[] bytes) throws IOException,Exception {
        InputStream is = new ByteArrayInputStream(bytes);
        DataInputStream mInputStream = new DataInputStream(is);
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
    }

}
