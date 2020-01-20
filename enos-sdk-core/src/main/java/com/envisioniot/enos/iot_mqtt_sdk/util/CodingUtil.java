package com.envisioniot.enos.iot_mqtt_sdk.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CodingUtil {
    public static final String DEFAULT_ENCODE = "utf-8";
    public static final int UNSIGNEDINT_MAX = 1073741823;

    public static void paddingBytes(ByteArrayOutputStream bos, byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }

        try {
            // write len
            bos.write(encodeUnsignInt(data.length));
            // write content
            bos.write(data);
        } catch (IOException e) { // not expected
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void paddingInt(ByteArrayOutputStream bos, int data) {
        paddingBytes(bos, encodeInt(data));
    }

    public static void paddingUnsignedInt(ByteArrayOutputStream bos, int data) {
        paddingBytes(bos, encodeUnsignInt(data));
    }

    public static void paddingString(ByteArrayOutputStream bos, String data) {
//        paddingUnsignedInt(bos, data.length());
        paddingBytes(bos, data.getBytes());
    }


    public static byte[] encodeBytes(byte[] data) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // null or empty string, directly represented by 0
        if (data == null || data.length == 0) {
            return new byte[]{0};
        }

        try {
            // write length
            bos.write(encodeUnsignInt(data.length));
            // write content
            bos.write(data);
        } catch (IOException e) {// not expected
            throw new RuntimeException(e.getMessage());
        }
        return bos.toByteArray();
    }

    public static byte[] decodeBytes(byte[] data) {
        return readBytes(ByteBuffer.wrap(data));
    }

    public static byte[] readBytes(ByteBuffer bf) {
        int len = readUnsignInt(bf);
        byte[] data = new byte[len];
        bf.get(data);
        return data;
    }

    public static byte[] encodeString(String k) {
        if (k == null || k.length() == 0) return new byte[]{0};
        else {
            try {
                return encodeBytes(k.getBytes(DEFAULT_ENCODE));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UnsupportedEncoding " + DEFAULT_ENCODE + ", encodeString fail.");
            }
        }
    }

    public static String readString(ByteBuffer bf) {
        try {
            return new String(readBytes(bf), DEFAULT_ENCODE);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UnsupportedEncoding " + DEFAULT_ENCODE + ", readString fail.");
        }
    }

    public static String decodeString(byte[] data) {
        return readString(ByteBuffer.wrap(data));
    }

    public static byte[] encodeUnsignInt(int value) {
        // first 2bit means to use several bytes to store, only non-negative numbers can be stored, and the maximum value is 1,073,741,824
        int len = 0;
        if (value < 0) {
            throw new RuntimeException("Value must > 0.");
        } else if (value <= 63) {// use 1 byte
            len = 1;
        } else if (value <= 16383) {// use 2 bytes
            len = 2;
        } else if (value <= 4194303) {// use 3 bytes
            len = 3;
        } else if (value <= 1073741823) {// use 4 bytes
            len = 4;
        } else {// exceed max
            throw new RuntimeException("Value too large!");
        }

        byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            data[len - i - 1] = (byte) (value >> 8 * i & 0xFF);
        }
        data[0] |= (len - 1) << 6;
        return data;
    }

    public static int readUnsignInt(ByteBuffer bf) {
        int result = 0;
        byte b = bf.get();
        int len = (b & 0xC0) >>> 6;
        // System.out.println("len:"+ len +" "+ b);
        b &= 0x3F;
        for (int i = len; ; i--) {
            result += (b & 0xFF) << (8 * i);

            if (i == 0) break;
            else b = bf.get();
        }
        return result;
    }

    public static int decodeUnsignInt(byte[] data) {
        int result = 0;
        int len = (data[0] & 0xC0) >>> 6;
        // System.out.println("len:"+ len +" "+ data[0]);
        data[0] &= 0x3F;
        byte b;
        for (int i = 0; i <= len; i++) {
            b = data[len - i];
            result += (b & 0xFF) << (8 * i);
        }
        return result;
    }

    //A tool for converting unsigned int to long, specially designed for QQ number 2.1 billion switching
    public static int uin21LongToInt(long uinL) {
        return (int) uinL;
    }

    public static List<Integer> uin21LongToInt(List<Long> uins) {
        List<Integer> intList = new ArrayList<Integer>();
        for (long uin : uins) {
            intList.add(uin21LongToInt(uin));
        }
        return intList;
    }

    //Convert long to unsigned int, a tool for switching 2.1 billion QQ numbers
    public static long uin21IntToLong(int uin) {
        return uin & 0xFFFFFFFFL;
    }

    public static List<Long> uin21IntToLong(List<Integer> uins) {
        List<Long> longList = new ArrayList<Long>();
        for (int uin : uins) {
            longList.add(uin21IntToLong(uin));
        }
        return longList;
    }

    public static byte[] encodeInt(int v) {
        // The first 3 bits indicate how many bytes to store, and the 4th bit indicates the symbol
        int flag = v < 0 ? 1 : 0;
        int value = Math.abs(v);// Integer.MIN_VALUE is treated specially, represented by -0
        int len = value <= 15 ? 1 : value <= 4095 ? 2 : value <= 1048575 ? 3 : value <= 268435455 ? 4 : 5;
        byte[] data = new byte[len];
        for (int i = 0; i < len && i < 4; i++) {
            data[len - i - 1] = (byte) (value >> 8 * i & 0xFF);
        }
        data[0] |= (((len - 1) << 1) + flag) << 4;
        return data;
    }

    public static int decodeInt(byte[] data) {
        int result = 0;
        int len = (data[0] & 0xE0) >>> 5;
        boolean flag = ((data[0] & 0x10) != 0);
        data[0] &= 0x0F;
        byte b;
        for (int i = 0; i <= len & i < 4; i++) {
            b = data[len - i];
            result += (b & 0xFF) << (8 * i);
        }
        if (flag) {
            if (result == 0) result = Integer.MIN_VALUE;
            else result *= -1;
        }
        return result;
    }

    public static int readInt(ByteBuffer bf) {
        int result = 0;
        byte b = bf.get();
        int len = (b & 0xE0) >>> 5;

        boolean flag = ((b & 0x10) != 0);
        b &= 0x0F;
        for (int i = len; ; i--) {
            result += (b & 0xFF) << (8 * i);

            if (i == 0) break;
            else b = bf.get();
        }
        if (flag) {
            if (result == 0) result = Integer.MIN_VALUE;
            else result *= -1;
        }
        return result;
    }

    public static long decodeLong(byte[] data) {
        long result = 0;
        int len = (data[0] & 0xF8) >>> 4;
        boolean flag = ((data[0] & 0x08) != 0);
        data[0] &= 0x07;
        byte b;
        for (int i = 0; i <= len & i < 8; i++) {
            b = data[len - i];
            result += (long) (b & 0xFF) << (8 * i);
        }
        if (flag) {
            if (result == 0) result = Long.MIN_VALUE;
            else result *= -1;
        }
        return result;
    }

    public static long readLong(ByteBuffer bf) {
        long result = 0;
        byte b = bf.get();
        int len = (b & 0xF8) >>> 4;
        boolean flag = ((b & 0x08) != 0);
        b &= 0x07;
        for (int i = len; ; i--) {
            result += (long) (b & 0xFF) << (8 * i);
            if (i == 0) break;
            else b = bf.get();
        }
        if (flag) {
            if (result == 0) result = Long.MIN_VALUE;
            else result *= -1;
        }
        return result;
    }

    public static byte[] encodeLong(long v) {// 4bit means to use several bytes for storage, and 5bit means the symbol
        int flag = v < 0 ? 1 : 0;
        long value = Math.abs(v);
        int len = value <= 7L ? 1 : value <= 2047L ? 2 : value <= 524287L ? 3 : value <= 134217727L ? 4 : value <= 34359738367L ? 5 : value <= 8796093022207L ? 6 : value <= 2251799813685247L ? 7 : value <= 576460752303423487L ? 8 : 9;

        byte[] data = new byte[len];
        for (int i = 0; i < len && i < 8; i++) {
            data[len - i - 1] = (byte) (value >> 8 * i & 0xFF);
        }
        data[0] |= (((len - 1) << 1) + flag) << 3;
        return data;
    }

    public static boolean decodeBoolean(byte b) {
        return b != 0;
    }

    public static byte[] encodeBoolean(boolean value) {
        return new byte[]{value ? (byte) 1 : (byte) 0};
    }

}
