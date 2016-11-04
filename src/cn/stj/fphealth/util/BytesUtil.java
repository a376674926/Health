
package cn.stj.fphealth.util;

import android.text.TextUtils;

import java.nio.ByteBuffer;

/**
 * @author jackey@20160804
 */
public class BytesUtil {

    private static ByteBuffer buffer = ByteBuffer.allocate(8);

    public static byte[] longToBytes(long x) {
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static byte[] stringToBytes(String data) {
        byte[] byteArray = new byte[0];
        if (!TextUtils.isEmpty(data)) {
            byteArray = data.getBytes();
        }
        return byteArray;
    }

    public static byte[] intToBytes(int data) {
        return new byte[] {
                (byte) ((data >> 24) & 0xFF),
                (byte) ((data >> 16) & 0xFF),
                (byte) ((data >> 8) & 0xFF),
                (byte) (data & 0xFF)
        };
    }

    public static byte intToByte(int x) {
        return (byte) x;
    }

    private byte[] shortToBytes(short n) {
        byte[] b = new byte[2];
        b[1] = (byte) (n & 0xff);
        b[0] = (byte) ((n >> 8) & 0xff);
        return b;
    }

    public static byte[] charToBytes(char c) {
        byte[] b = new byte[8];
        b[0] = (byte) (c >>> 8);
        b[1] = (byte) c;
        return b;
    }

    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        int byteLen1 = 0;
        int byteLen2 = 0;
        if (byte_1 != null) {
            byteLen1 = byte_1.length;
        }
        if (byte_2 != null) {
            byteLen2 = byte_2.length;
        }
        byte[] byte_3 = new byte[byteLen1 + byteLen2];
        if (byteLen1 > 0) {
            System.arraycopy(byte_1, 0, byte_3, 0, byteLen1);
        }

        if (byteLen2 > 0) {
            System.arraycopy(byte_2, 0, byte_3, byteLen1, byteLen2);
        }
        return byte_3;
    }

    public static int byteToInt(byte b) {
        return b & 0xFF;
    }
}
