/*
 * Copyright (C) 2015-2017 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/thistle
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.util.conversion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Byte处理工具
 * 
 * @author S.Violet
 */

public class ByteUtils {

    /**
     * 把两个byte[]前后拼接成一个byte[]
     *
     * @param left left bytes
     * @param right right bytes
     * @return jointed bytes
     */
    public static byte[] joint(byte[] left, byte[] right){
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        byte[] result = new byte[left.length + right.length];
        System.arraycopy(left, 0, result, 0, left.length);
        System.arraycopy(right, 0, result, left.length, right.length);
        return result;
    }

    /**
     * 把三个byte[]前后拼接成一个byte[]
     *
     * @param left left bytes
     * @param middle middle bytes
     * @param right right bytes
     * @return jointed bytes
     */
    public static byte[] joint(byte[] left, byte[] middle, byte[] right){
        if (left == null) {
            return joint(middle, right);
        }
        if (middle == null) {
            return joint(left, right);
        }
        if (right == null) {
            return joint(left, middle);
        }
        byte[] result = new byte[left.length + middle.length + right.length];
        System.arraycopy(left, 0, result, 0, left.length);
        System.arraycopy(middle, 0, result, left.length, middle.length);
        System.arraycopy(right, 0, result, left.length + middle.length, right.length);
        return result;
    }

    /**
     * 二进制数据长度调整到固定长度, 多余部分舍弃(左边的舍弃), 少的补0(在左边补0)
     *
     * @param data 二进制数据
     * @param length 目标长度
     */
    public static byte[] toLength(byte[] data, int length) {
        if (data == null) {
            return new byte[length];
        }
        if (data.length == length) {
            return data;
        }
        byte[] result = new byte[length];
        if (data.length > length) {
            System.arraycopy(data, data.length - length, result, 0, length);
        } else {
            System.arraycopy(data, 0, result, length - data.length, data.length);
        }
        return result;
    }

    /**
     * 从二进制数据中截取一段
     * @param data 二进制数据
     * @param start 起始位置(到末尾)
     */
    public static byte[] sub(byte[] data, int start){
        if (data == null) {
            return null;
        }
        if (start >= data.length) {
            return new byte[0];
        }
        if (start <= 0) {
            return data;
        }
        byte[] result = new byte[data.length - start];
        System.arraycopy(data, start, result, 0, result.length);
        return result;
    }

    /**
     * 从二进制数据中截取一段
     * @param data 二进制数据
     * @param start 起始位置(到末尾)
     * @param length 截取长度
     */
    public static byte[] sub(byte[] data, int start, int length){
        if (data == null) {
            return null;
        }
        if (start < 0) {
            start = 0;
        }
        if (length <= 0) {
            return new byte[0];
        }
        if (start + length > data.length) {
            length = data.length - start;
        }
        byte[] result = new byte[length];
        System.arraycopy(data, start, result, 0, length);
        return result;
    }

    /**
     * 去掉二进制数据头部(左边)的0x00
     *
     * @param data 二进制数据
     */
    public static byte[] trimHeader(byte[] data){
        if (data == null) {
            return null;
        }
        if (data.length <= 0) {
            return new byte[0];
        }
        int i = 0;
        for (; i < data.length ; i++) {
            if (data[i] != 0x00) {
                break;
            }
        }
        if (i <= 0) {
            return data;
        }
        byte[] result = new byte[data.length - i];
        System.arraycopy(data, i, result, 0, result.length);
        return result;
    }

    /**
     * bytes转为hexString
     * @param bytes bytes
     * @return upper case hex string
     */
    public static String bytesToUpperCaseHex(byte bytes) {
        return bytesToHex(bytes).toUpperCase();
    }

    /**
     * bytes转为hexString
     * @param bytes bytes
     * @return lower case hex string
     */
    public static String bytesToHex(byte bytes) {
        int unitInt = bytes & 0xFF;
        String unitHex = Integer.toHexString(unitInt);
        if (unitHex.length() < 2) {
            return '0' + unitHex;
        }
        return unitHex;
    }

    /**
     * bytes转为hexString
     * @param bytes bytes
     * @return upper case hex string
     */
    public static String bytesToUpperCaseHex(byte[] bytes){
        if (bytes == null) {
            return null;
        }
        return bytesToHex(bytes).toUpperCase();
    }

    /**
     * bytes转为hexString
     * @param bytes bytes
     * @return lower case hex string
     */
    public static String bytesToHex(byte[] bytes){
        if (bytes == null) {
            return null;
        }
        int bytesLength = bytes.length;
        if (bytesLength <= 0){
            return "";
        }
        char[] result = new char[bytesLength * 2];
        int resultIndex = 0;
        for (byte b : bytes) {
            result[resultIndex++] = BYTE_TO_HEX_CHAR_MAP[b >>> 4 & 0xF];
            result[resultIndex++] = BYTE_TO_HEX_CHAR_MAP[b & 0xF];
        }
        return new String(result);
    }

    /**
     * byte -> 十六进制字符 映射表
     */
    private static final char[] BYTE_TO_HEX_CHAR_MAP = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * hexString转为bytes
     * @param hexString hexString
     * @return bytes
     */
    public static byte[] hexToBytes(String hexString) {
        if (hexString == null) {
            return null;
        }
        int hexLength = hexString.length();
        if (hexLength <= 0){
            return new byte[0];
        }
        // Vars
        char[] hexChars = hexString.toCharArray();
        int hexIndex;
        byte[] result;
        int resultIndex = 0;
        // Check if the length is a multiple of 2
        if (hexLength % 2 != 0) {
            hexIndex = -1;
            result = new byte[(hexLength + 1) / 2];
        } else {
            hexIndex = 0;
            result = new byte[hexLength / 2];
        }
        // Convert
        for (; hexIndex < hexLength; hexIndex += 2) {
            char c0 = hexIndex >= 0 ? hexChars[hexIndex] : '0';
            char c1 = hexChars[hexIndex + 1];
            result[resultIndex++] = (byte) ((HEX_CHAR_TO_BYTE_MAP[hexCharToIndex(c0, hexString)] << 4) |
                    HEX_CHAR_TO_BYTE_MAP[hexCharToIndex(c1, hexString)]);
        }
        return result;
    }

    /**
     * 十六进制字符 -> byte 映射表
     */
    private static final byte[] HEX_CHAR_TO_BYTE_MAP = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f};

    /**
     * 十六进制字符 -> byte 映射表 的索引
     */
    private static int hexCharToIndex(char c, String hexString) {
        if (c < '0') {
            throw new IllegalArgumentException("[ByteUtils]hexToBytes: Illegal char '" + c + "' in hex string: " + hexString);
        }
        if (c <= '9') {
            return c - '0';
        }
        if (c < 'A') {
            throw new IllegalArgumentException("[ByteUtils]hexToBytes: Illegal char '" + c + "' in hex string: " + hexString);
        }
        if (c <= 'F') {
            return c - 'A' + 10;
        }
        if (c < 'a') {
            throw new IllegalArgumentException("[ByteUtils]hexToBytes: Illegal char '" + c + "' in hex string: " + hexString);
        }
        if (c <= 'f') {
            return c - 'a' + 10;
        }
        throw new IllegalArgumentException("[ByteUtils]hexToBytes: Illegal char '" + c + "' in hex string: " + hexString);
    }

    /**
     * 对象转数组(JDK序列化)
     * @param obj object
     * @return bytes
     */
    public static byte[] objectToBytes(Object obj) throws IOException {
        byte[] bytes;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.flush();
        bytes = bos.toByteArray();
        oos.close();
        bos.close();
        return bytes;
    }

    /**
     * 数组转对象(JDK反序列化)
     * @param bytes bytes
     * @return object
     */
    public static Object bytesToObject(byte[] bytes) throws IOException, ClassNotFoundException {
        Object obj;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        obj = ois.readObject();
        ois.close();
        bis.close();
        return obj;
    }

    /**
     * char[] 转 byte[]
     * @param chars char[]
     * @param charset charset
     */
    public static byte[] charsToBytes(char[] chars, String charset) {
        CharBuffer charBuffer = CharBuffer.allocate(chars.length);
        charBuffer.put(chars);
        charBuffer.flip();
        return Charset.forName(charset).encode(charBuffer).array();
    }

    /**
     * byte[] 转 char[]
     * @param bytes byte[]
     * @param charset charset
     */
    public static char[] bytesToChars(byte[] bytes, String charset) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        return Charset.forName(charset).decode(byteBuffer).array();
    }

    /**
     * 循环左移
     * @param b byte
     * @param n 左移位数[0, 8]
     */
    public static byte cyclicLeftShift(byte b, int n){
        n = n % 8;
        if (n < 0){
            return cyclicRightShift(b, -n);
        }
        //byte做移位时, 会先转为int, 必须与0xff, 否则会出问题
        int bi = b & 0xff;
        return (byte)(bi << n | bi >>> 8 - n);
    }

    /**
     * 循环右移
     * @param b byte
     * @param n 右移位数[0, 8]
     */
    public static byte cyclicRightShift(byte b, int n){
        n = n % 8;
        if (n < 0){
            return cyclicLeftShift(b, -n);
        }
        //byte做移位时, 会先转为int, 必须与0xff, 否则会出问题
        int bi = b & 0xff;
        return (byte)(bi >>> n | bi << 8 - n);
    }

    private static final String[] BIN_MAPPING = {"0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111"};

    /**
     * byte转二进制字符串
     * @param b byte
     */
    public static String byteToBin(byte b){
        //byte做移位时, 会先转为int, 必须与0xff, 否则会出问题
        return BIN_MAPPING[(b & 0xff) >>> 4] + BIN_MAPPING[b & 0x0F];
    }

    /**
     * bytes转long
     * @param bytes bytes
     * @param littleEndian 默认false
     */
    public static long bytesToLong(byte[] bytes, boolean littleEndian) {
        // 将byte[] 封装为 ByteBuffer
        ByteBuffer buffer = ByteBuffer.wrap(bytes, 0,8);
        if(littleEndian){
            buffer.order(ByteOrder.LITTLE_ENDIAN);
        }
        return buffer.getLong();
    }

    /**
     * long转bytes
     * @param longValue long
     * @param littleEndian 默认false
     */
    public static byte[] longToBytes(long longValue, boolean littleEndian){
        ByteBuffer buffer = ByteBuffer.allocate(8);
        if(littleEndian){
            buffer.order(ByteOrder.LITTLE_ENDIAN);
        }
        buffer.putLong(longValue);
        return buffer.array();
    }

}
