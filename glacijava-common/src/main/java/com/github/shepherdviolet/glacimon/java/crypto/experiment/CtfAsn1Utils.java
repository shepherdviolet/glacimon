/*
 * Copyright (C) 2022-2026 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/glacimon
 * Email: shepherdviolet@163.com
 */

package com.github.shepherdviolet.glacimon.java.crypto.experiment;

import com.github.shepherdviolet.glacimon.java.conversion.Base64Utils;
import com.github.shepherdviolet.glacimon.java.conversion.ByteUtils;
import com.github.shepherdviolet.glacimon.java.crypto.PEMEncodeUtils;
import com.github.shepherdviolet.glacimon.java.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * [CTF用] ASN.1格式的密钥/证书高容忍解析工具 (DER / PEM / X509 / PKCS#1/#8)
 *
 * @author shepherdviolet
 */
public class CtfAsn1Utils {

    public static Map<String, Object> parse(File file) throws IOException, FileUtils.LengthOutOfLimitException {
        if (file == null || !file.exists()) {
            return new LinkedHashMap<>();
        }
        byte[] bytes = FileUtils.readBytes(file, 10 * 1024 * 1024);
        return parse(bytes);
    }

    public static Map<String, Object> parse(String asn1Text) {
        if (asn1Text == null || asn1Text.trim().isEmpty()) {
            return new LinkedHashMap<>();
        }
        return parse(asn1Text.getBytes(StandardCharsets.UTF_8));
    }

    public static Map<String, Object> parse(byte[] asn1Bytes) {
        if (asn1Bytes == null || asn1Bytes.length == 0) {
            return new LinkedHashMap<>();
        }

        byte[] derBytes = null;

        // 检查前 1024 字节是否全部为可打印 ASCII 或常见空白符
        // 如果包含任何二进制高位字节(如 0x82 等)，判定为纯二进制 DER，跳过 String 转换
        boolean isTextFormat = true;
        int checkLen = Math.min(asn1Bytes.length, 1024);
        for (int i = 0; i < checkLen; i++) {
            int b = asn1Bytes[i] & 0xFF;
            // 允许的文本字符范围：0x09(TAB), 0x0A(换行), 0x0D(回车), 0x20-0x7E(标准可打印 ASCII)
            if (!((b >= 0x20 && b <= 0x7E) || b == 0x09 || b == 0x0A || b == 0x0D)) {
                isTextFormat = false;
                break;
            }
        }

        // 如果是PEM文本, 尝试删除BEGIN/END
        if (isTextFormat) {
            try {
                String asn1Text = new String(asn1Bytes, StandardCharsets.UTF_8);
                try {
                    derBytes = PEMEncodeUtils.pemEncodedToX509EncodedBytes(asn1Text);
                } catch (Exception e) {
                    // 盲解兜底清洗
                    String cleaned = asn1Text.replaceAll("-----BEGIN[^-]*-----", "")
                            .replaceAll("-----END[^-]*-----", "")
                            .replaceAll("\\s+", "");
                    if (cleaned.matches("^[A-Za-z0-9+/=]+$")) {
                        try {
                            derBytes = Base64Utils.decode(cleaned);
                        } catch (Exception ignored) {}
                    }
                }
            } catch (Exception ignored) {}
        }

        // 如果判定为纯二进制，或者文本清洗失败，则作为原始二进制 DER 数据解析
        if (derBytes == null || derBytes.length == 0) {
            derBytes = asn1Bytes;
        }

        int[] offset = new int[]{0};
        return parseAsn1Node(derBytes, offset, derBytes.length);
    }

    /**
     * ASN.1解析逻辑
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseAsn1Node(byte[] data, int[] offset, int maxLimit) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Object> children = new ArrayList<>();
        int intCounter = 0;

        while (offset[0] < maxLimit && offset[0] < data.length) {
            int startPos = offset[0];
            int tagByte = data[offset[0]++] & 0xFF;

            // 解析长度字段 (支持长格式与短格式)
            if (offset[0] >= data.length) break;
            int lenByte = data[offset[0]++] & 0xFF;
            int length = 0;

            if ((lenByte & 0x80) == 0) {
                length = lenByte;
            } else {
                int numLenBytes = lenByte & 0x7F;
                // 如果长度字段本身溢出或损坏，容错处理：将其视为长度为0，跳过此标签
                if (offset[0] + numLenBytes > data.length || numLenBytes > 4) {
                    offset[0] = startPos + 1; // 强行步进 1 字节纠错
                    continue;
                }
                for (int i = 0; i < numLenBytes; i++) {
                    length = (length << 8) | (data[offset[0]++] & 0xFF);
                }
            }

            // 判断长度是否合理，防止畸形数据导致 OOM 或死循环
            if (length < 0 || offset[0] + length > data.length) {
                // 如果长度损坏，允许强行将后续所有可用字节当成当前节点内容，实现最大化容忍
                length = data.length - offset[0];
            }

            int endPos = offset[0] + length;

            // 根据 Tag 类型进行裸分流与嵌套识别
            boolean isConstructed = (tagByte & 0x20) != 0;
            int tagType = tagByte & 0x1F;

            // Sequence(0x30), Set(0x31) 或者是包含嵌套的特殊 Object/Octet String
            if (isConstructed || tagType == 0x10 || tagType == 0x11) {
                int[] subOffset = new int[]{offset[0]};
                Map<String, Object> subTree = parseAsn1Node(data, subOffset, endPos);

                // 如果子结构成功捞到了东西，装载进入树节点
                if (!subTree.isEmpty()) {
                    children.add(subTree);
                } else if (length > 0) {
                    // 如果被构造类型内部解不出有效结构，降级保存为十六进制字面量
                    byte[] rawValue = new byte[length];
                    System.arraycopy(data, offset[0], rawValue, 0, length);
                    // 复用 ByteUtils
                    children.add(ByteUtils.bytesToHex(rawValue));
                }
                offset[0] = endPos; // 锚定当前节点结束位置
            }
            // 底层数据类型：大整数 (INTEGER = 0x02)
            else if (tagType == 0x02 && length > 0) {
                byte[] valBytes = new byte[length];
                System.arraycopy(data, offset[0], valBytes, 0, length);

                // 将捞出的大整数直接以正整数形式保留并编号存入结果
                BigInteger bi = new BigInteger(1, valBytes);
                result.put("INTEGER_" + (intCounter++), bi);
                offset[0] = endPos;
            }
            // 其他普通标量数据 (如 OID, BitString, PrintString 等)
            else {
                if (length > 0) {
                    byte[] rawValue = new byte[length];
                    System.arraycopy(data, offset[0], rawValue, 0, length);

                    // 容错：尝试探测 BitString(0x03) 或 OctetString(0x04) 内部是否深层嵌套了 ASN.1 结构
                    if ((tagType == 0x03 || tagType == 0x04) && length > 2) {
                        int checkTag = rawValue[tagType == 0x03 ? 1 : 0] & 0xFF;
                        if (checkTag == 0x30 || checkTag == 0x02) { // 发现内部隐藏了 Sequence 或 Integer
                            try {
                                int[] innerOffset = new int[]{tagType == 0x03 ? 1 : 0};
                                Map<String, Object> innerTree = parseAsn1Node(rawValue, innerOffset, length);
                                if (!innerTree.isEmpty()) {
                                    children.add(innerTree);
                                    offset[0] = endPos;
                                    continue;
                                }
                            } catch (Exception ignored) {}
                        }
                    }

                    // 解析证书，如果是文本或时间Tag，直接转为人类可读字符串存入树中
                    if (tagType == 0x0C || (tagType >= 0x12 && tagType <= 0x16) || tagType == 0x17 || tagType == 0x18) {
                        children.add(new String(rawValue, StandardCharsets.UTF_8));
                    } else {
                        children.add(ByteUtils.bytesToHex(rawValue));
                    }
                    offset[0] = endPos;
                }
            }
        }

        if (!children.isEmpty()) {
            if (children.size() == 1 && children.get(0) instanceof Map) {
                result.putAll((Map<String, Object>) children.get(0));
            } else {
                result.put("SEQUENCE_SET", children);
            }
        }
        return result;
    }

}