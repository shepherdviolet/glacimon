/*
 * Copyright (C) 2015-2021 S.Violet
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

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * UUID生成工具
 *
 * @author shepherdviolet
 */
public class UuidUtils {

    /**
     * 生成一个标准的 UUID 字符串
     */
    public static String newStringUuid() {
        return toStringUuid(null);
    }

    /**
     * 转换为 标准的 UUID 字符串
     */
    public static String toStringUuid(UUID uuid) {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        return uuid.toString();
    }

    /**
     * 生成一个不带"-"符号的 UUID 字符串,
     * 效率比 UUID.randomUUID().toString().replaceAll("-", "") 高
     */
    public static String newStringUuidWithoutDash(){
        return toStringUuidWithoutDash(null);
    }

    /**
     * 转换为 不带"-"符号的 UUID 字符串,
     * 效率比 UUID.randomUUID().toString().replaceAll("-", "") 高
     */
    public static String toStringUuidWithoutDash(UUID uuid){
        return ByteUtils.bytesToHex(bytesUuid(uuid));
    }

    /**
     * 生成一个压缩的 UUID 字符串, 比较短,
     * 用URL-Safe BASE64编码的 UUID, 包含数字 字母 "-" "_", 并删掉了末尾的 "=="
     */
    public static String newStringUuidCompressed() {
        return toStringUuidCompressed(null);
    }

    /**
     * 转换为 压缩的 UUID 字符串, 比较短,
     * 用URL-Safe BASE64编码的 UUID, 包含数字 字母 "-" "_", 并删掉了末尾的 "=="
     */
    public static String toStringUuidCompressed(UUID uuid) {
        return Base64Utils.encodeToUrlSafeString(bytesUuid(uuid)).substring(0, 22);
    }

    private static byte[] bytesUuid(UUID uuid) {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

}
