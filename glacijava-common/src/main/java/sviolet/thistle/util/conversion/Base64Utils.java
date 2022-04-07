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

import sviolet.thistle.compat.util.CompatBase64;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Base64工具<br/>
 *
 * @author S.Violet
 */
public class Base64Utils {

    private static final byte CR = '\r';
    private static final byte LF = '\n';
    private static final char CHAR_CR = '\r';
    private static final char CHAR_LF = '\n';
    private static final byte[] CRLF = new byte[] {'\r', '\n'};

    private static final CompatBase64.Encoder MINE_ENCODER = CompatBase64.getMimeEncoder(64, CRLF);

    /**
     * bytes 编码为 Base64 bytes (标准模式: + / =).
     * RFC4648
     * RFC4648
     *
     * @param data bytes
     */
    public static byte[] encode(byte[] data) {
        if (data == null) {
            return null;
        }
        if (data.length <= 0) {
            return new byte[0];
        }
        return CompatBase64.getEncoder().encode(data);
    }

    /**
     * bytes 编码为 Base64 String (标准模式: + / =).
     * RFC4648
     *
     * @param data bytes
     */
    public static String encodeToString(byte[] data) {
        if (data == null) {
            return null;
        }
        if (data.length <= 0) {
            return "";
        }
        return CompatBase64.getEncoder().encodeToString(data);
    }

    /**
     * bytes 编码为 自动换行的 Base64 String (标准模式: + / =).
     * RFC2045 (每行不超过76个字符)
     *
     * @param data bytes
     */
    public static String encodeToMimeString(byte[] data) {
        if (data == null) {
            return null;
        }
        if (data.length <= 0) {
            return "";
        }
        return MINE_ENCODER.encodeToString(data);
    }

    /**
     * bytes 编码为 URL安全 的 Base64 String (URL安全模式: - _ =).
     * RFC4648_URLSAFE
     *
     * @param data bytes
     */
    public static String encodeToUrlSafeString(byte[] data){
        if (data == null) {
            return null;
        }
        if (data.length <= 0) {
            return "";
        }
        return CompatBase64.getUrlEncoder().encodeToString(data);
    }

    /**
     * bytes 编码为 URL Encoding 的 Base64 String (URL编码模式: %2B %2F %3D)
     *
     * @param data bytes
     */
    public static String encodeToUrlEncodedString(byte[] data){
        if (data == null) {
            return null;
        }
        if (data.length <= 0) {
            return "";
        }
        try {
            return URLEncoder.encode(CompatBase64.getEncoder().encodeToString(data), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Base64 bytes 解码为 bytes (标准模式: + / =).
     * RFC4648 | RFC2045
     */
    public static byte[] decode(byte[] data) {
        if (data == null) {
            return null;
        }
        if (data.length <= 0) {
            return new byte[0];
        }
        for (int i = 0 ; i < 77 && i < data.length ; i++) {
            byte b = data[i];
            if (b == CR || b == LF) {
                // RFC2045
                return CompatBase64.getMimeDecoder().decode(data);
            }
        }
        // RFC4648
        return CompatBase64.getDecoder().decode(data);
    }

    /**
     * Base64 String解码为bytes (标准模式: + / =).
     * RFC4648 | RFC2045
     */
    public static byte[] decode(String data) {
        if (data == null) {
            return null;
        }
        if (data.length() <= 0) {
            return new byte[0];
        }
        for (int i = 0 ; i < 77 && i < data.length() ; i++) {
            char c = data.charAt(i);
            if (c == CHAR_CR || c  == CHAR_LF) {
                // RFC2045
                return CompatBase64.getMimeDecoder().decode(data);
            }
        }
        // RFC4648
        return CompatBase64.getDecoder().decode(data);
    }

    /**
     * URL安全的 Base64 String解码为bytes (URL安全模式: - _ =).
     * RFC4648_URLSAFE
     */
    public static byte[] decodeFromUrlSafeString(String data) {
        if (data == null) {
            return null;
        }
        if (data.length() <= 0) {
            return new byte[0];
        }
        return CompatBase64.getUrlDecoder().decode(data);
    }

    /**
     * URL Encoding的 Base64 String解码为bytes (URL编码模式: %2B %2F %3D)
     */
    public static byte[] decodeFromUrlEncodedString(String data) {
        if (data == null) {
            return null;
        }
        if (data.length() <= 0) {
            return new byte[0];
        }
        try {
            return CompatBase64.getDecoder().decode(URLDecoder.decode(data, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
