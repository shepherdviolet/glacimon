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

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * [CTF用] 基于CtfAsn1Utils的一键式 公私钥证书 高容忍度读取器
 *
 * 直接调用guess
 *
 * @author shepherdviolet
 */
public class CtfAsn1Guesser {

    public static void guess(File file) {
        try {
            doGuess(CtfAsn1Utils.parse(file));
        } catch (Exception e) {
            System.out.println("[ERROR] Inspection crashed during file read: " + e.getMessage());
        }
    }

    public static void guess(String asn1Text) {
        doGuess(CtfAsn1Utils.parse(asn1Text));
    }

    public static void guess(byte[] asn1Bytes) {
        doGuess(CtfAsn1Utils.parse(asn1Bytes));
    }

    /**
     * 智能推测主入口
     */
    private static void doGuess(Map<String, Object> rawResult) {
        // Step 1: 打印最原始裸解析出的 Map 拓扑树
        System.out.println("--- [1] Raw ASN.1 Parse Tree Topology ---");
        System.out.println(rawResult);
        System.out.println();

        // Step 2: 启动深层 Schema 模式探测与内容反洗
        System.out.println("--- [2] Standard Crypto Format Matcher & Decoder ---");

        // A. 尝试按照 X.509 证书 Schema 提取元数据
        inspectCertificateMetadata(rawResult);

        // B. 递归扫描整个解析树进行多方案密钥解耦
        boolean[] matchedAny = new boolean[]{false};
        inspectNodeRecursive(rawResult, "Root", matchedAny);

        System.out.println("========================================================================");
    }

    /**
     * 核心递归：支持多层嵌套外壳（如 PKCS#8, X509 证书外层）的逐层剥离与并发解析
     */
    private static void inspectNodeRecursive(Object node, String path, boolean[] matchedAny) {
        if (node instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) node;

            // 格式 A 尝试：标准 PKCS#1 RSA 私钥结构校验 (包含Version且有5个以上核心大整数)
            if (map.containsKey("INTEGER_1") && map.containsKey("INTEGER_2") &&
                    (map.containsKey("INTEGER_3") || map.containsKey("INTEGER_4") || map.containsKey("INTEGER_5"))) {

                matchedAny[0] = true;
                System.out.println("------------------------------------------------------------------------");
                System.out.println("🎯 Match Found: [RSA Private Key Structure (PKCS#1 Spec)]");
                System.out.println("    Location Path: " + path);
                System.out.println("------------------------------------------------------------------------");
                printParamField("Version (INTEGER_0)", map.get("INTEGER_0"));
                printParamField("n (Modulus / 模数 / INTEGER_1)", map.get("INTEGER_1"));
                printParamField("e (Public Exponent / 公钥指数 / INTEGER_2)", map.get("INTEGER_2"));
                printParamField("d (Private Exponent / 私钥指数 / INTEGER_3)", map.get("INTEGER_3"));
                printParamField("p (Prime Factor 1 / 素因子 p / INTEGER_4)", map.get("INTEGER_4"));
                printParamField("q (Prime Factor 2 / 素因子 q / INTEGER_5)", map.get("INTEGER_5"));
                printParamField("dmp1 (d mod (p-1) / INTEGER_6)", map.get("INTEGER_6"));
                printParamField("dmq1 (d mod (q-1) / INTEGER_7)", map.get("INTEGER_7"));
                printParamField("iqmp (q^-1 mod p / INTEGER_8)", map.get("INTEGER_8"));
                System.out.println();
            }

            // 格式 B 尝试：标准 RSA 公钥结构校验 (仅包含 n, e 两组大整数，没有私钥参数)
            if (map.containsKey("INTEGER_0") && map.containsKey("INTEGER_1") && !map.containsKey("INTEGER_2")) {
                Object i0 = map.get("INTEGER_0");
                if (i0 instanceof BigInteger && ((BigInteger) i0).bitLength() > 100) {
                    matchedAny[0] = true;
                    System.out.println("------------------------------------------------------------------------");
                    System.out.println("🎯 Match Found: [RSA Public Key Structure (PKCS#1 Spec)]");
                    System.out.println("    Location Path: " + path);
                    System.out.println("------------------------------------------------------------------------");
                    printParamField("n (Modulus / 模数 / INTEGER_0)", map.get("INTEGER_0"));
                    printParamField("e (Public Exponent / 指数 / INTEGER_1)", map.get("INTEGER_1"));
                    System.out.println();
                }
            }

            // 深入遍历 Map 内部子节点并探测隐藏 OID
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                checkAndPrintOidMetadata(entry.getValue());
                inspectNodeRecursive(entry.getValue(), path + " -> " + entry.getKey(), matchedAny);
            }

        } else if (node instanceof List) {
            List<?> list = (List<?>) node;
            for (int i = 0; i < list.size(); i++) {
                Object child = list.get(i);
                checkAndPrintOidMetadata(child);
                inspectNodeRecursive(child, path + "[" + i + "]", matchedAny);
            }
        }
    }

    /**
     * 智能抽取并打印证书（X.509）元数据信息
     */
    private static void inspectCertificateMetadata(Map<String, Object> rawResult) {
        List<Object> timeline = new ArrayList<>();
        collectCertElements(rawResult, timeline);

        List<BigInteger> prefixIntegers = new ArrayList<>();
        List<DnAttr> issuerAttrs = new ArrayList<>();
        List<DnAttr> subjectAttrs = new ArrayList<>();
        CertTime notBefore = null;
        CertTime notAfter = null;

        int timeIndicator = 0;
        for (Object el : timeline) {
            if (el instanceof BigInteger) {
                if (timeIndicator == 0) {
                    prefixIntegers.add((BigInteger) el);
                }
            } else if (el instanceof CertTime) {
                timeIndicator++;
                if (timeIndicator == 1) notBefore = (CertTime) el;
                if (timeIndicator == 2) notAfter = (CertTime) el;
            } else if (el instanceof DnAttr) {
                if (timeIndicator == 0) {
                    issuerAttrs.add((DnAttr) el);
                } else {
                    subjectAttrs.add((DnAttr) el);
                }
            }
        }

        // 如果拥有显著的时间块或者DN特征，判定匹配到了证书实体
        if (notBefore != null || !issuerAttrs.isEmpty() || !subjectAttrs.isEmpty()) {
            System.out.println("------------------------------------------------------------------------");
            System.out.println("🎯 Match Found: [X.509 Certificate Standard Metadata Profile]");
            System.out.println("------------------------------------------------------------------------");

            // 1. 版本号与证书序列号
            if (!prefixIntegers.isEmpty()) {
                if (prefixIntegers.size() >= 2) {
                    BigInteger v = prefixIntegers.get(0);
                    System.out.println("  * Certificate Version: v" + (v.intValue() + 1) + " (Raw: " + v + ")");
                    System.out.println("  * Serial Number      : " + prefixIntegers.get(1) + " (0x" + prefixIntegers.get(1).toString(16) + ")");
                } else {
                    System.out.println("  * Serial Number      : " + prefixIntegers.get(0) + " (0x" + prefixIntegers.get(0).toString(16) + ")");
                }
            }

            // 2. 有效期
            if (notBefore != null) System.out.println("  * Validity Not Before: " + formatCertTime(notBefore.time));
            if (notAfter != null)  System.out.println("  * Validity Not After : " + formatCertTime(notAfter.time));

            // 3. 颁发者 Distinguished Name
            if (!issuerAttrs.isEmpty()) {
                System.out.println("  * Issuer DN (颁发者实体信息):");
                for (DnAttr attr : issuerAttrs) {
                    System.out.println("    - " + translateDnOid(attr.oid) + " = " + attr.value);
                }
            }

            // 4. 使用者 Distinguished Name
            if (!subjectAttrs.isEmpty()) {
                System.out.println("  * Subject DN (使用者主体信息):");
                for (DnAttr attr : subjectAttrs) {
                    System.out.println("    - " + translateDnOid(attr.oid) + " = " + attr.value);
                }
            }
            System.out.println();
        }
    }

    /**
     * 深度优先线性拉平扫描证书核心元素
     */
    private static void collectCertElements(Object node, List<Object> timeline) {
        if (node instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) node;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object val = entry.getValue();
                if (val instanceof BigInteger) {
                    timeline.add(val);
                } else {
                    collectCertElements(val, timeline);
                }
            }
        } else if (node instanceof List) {
            List<?> list = (List<?>) node;
            // 如果发现了 [OID, VALUE_STRING] 的成对DN属性结构
            if (list.size() == 2 && list.get(0) instanceof String && list.get(1) instanceof String) {
                String s0 = ((String) list.get(0)).toLowerCase().trim();
                String s1 = (String) list.get(1);
                if (isDnOid(s0)) {
                    timeline.add(new DnAttr(s0, s1));
                    return;
                }
            }
            for (Object child : list) {
                if (child instanceof String) {
                    String s = (String) child;
                    // UTCTime 或者是 GeneralizedTime 特征匹配 (以Z结尾)
                    if (s.endsWith("Z") && (s.length() == 13 || s.length() == 15)) {
                        timeline.add(new CertTime(s));
                    }
                } else {
                    collectCertElements(child, timeline);
                }
            }
        }
    }

    private static boolean isDnOid(String oid) {
        return oid.equals("550403") || oid.equals("550406") || oid.equals("550407") ||
                oid.equals("550408") || oid.equals("55040a") || oid.equals("55040b") ||
                oid.equals("2a864886f70d010901");
    }

    private static String translateDnOid(String oid) {
        switch (oid) {
            case "550403": return "Common Name (CN)";
            case "550406": return "Country (C)";
            case "550407": return "Locality (L)";
            case "550408": return "State/Province (ST)";
            case "55040a": return "Organization (O)";
            case "55040b": return "Organizational Unit (OU)";
            case "2a864886f70d010901": return "Email Address";
            default: return "OID_" + oid;
        }
    }

    private static String formatCertTime(String rawTime) {
        try {
            if (rawTime.endsWith("Z")) {
                String digits = rawTime.substring(0, rawTime.length() - 1);
                if (digits.length() == 12) { // YYMMDDHHMMSS (UTCTime)
                    String yr = digits.substring(0, 2);
                    int y = Integer.parseInt(yr);
                    yr = (y >= 50 ? "19" : "20") + yr;
                    return yr + "-" + digits.substring(2, 4) + "-" + digits.substring(4, 6) + " " +
                            digits.substring(6, 8) + ":" + digits.substring(8, 10) + ":" + digits.substring(10, 12) + " UTC";
                } else if (digits.length() == 14) { // YYYYMMDDHHMMSS (GeneralizedTime)
                    return digits.substring(0, 4) + "-" + digits.substring(4, 6) + "-" + digits.substring(6, 8) + " " +
                            digits.substring(8, 10) + ":" + digits.substring(10, 12) + ":" + digits.substring(12, 14) + " UTC";
                }
            }
        } catch (Exception ignored) {}
        return rawTime;
    }

    /**
     * OID 元数据自动转译清洗
     */
    private static void checkAndPrintOidMetadata(Object val) {
        if (val instanceof String) {
            String hex = ((String) val).toLowerCase().trim();
            String oidDesc = translateOid(hex);
            if (oidDesc != null) {
                System.out.println("------------------------------------------------------------------------");
                System.out.println("📦 Envelope Metadata Found: [Standard Algorithm Identifier / OID]");
                System.out.println("------------------------------------------------------------------------");
                System.out.println("  * Algorithm Name: " + oidDesc);
                System.out.println("  * Raw ASN.1 Hex : " + hex);
                System.out.println();
            }
        }
    }

    /**
     * 常见密码学与证书标准 OID 映射库
     */
    private static String translateOid(String hex) {
        switch (hex) {
            case "2a864886f70d010101": return "1.2.840.113549.1.1.1 (rsaEncryption - RSA加密算法主体)";
            case "2a864886f70d010105": return "1.2.840.113549.1.1.5 (sha1WithRSAEncryption - RSA老式证书签名)";
            case "2a864886f70d01010b": return "1.2.840.113549.1.1.11 (sha256WithRSAEncryption - 现代主流X.509证书签名)";
            case "2a864886f70d01010c": return "1.2.840.113549.1.1.12 (sha384WithRSAEncryption)";
            case "2a864886f70d01010d": return "1.2.840.113549.1.1.13 (sha512WithRSAEncryption)";
            case "2a864886f70d010903": return "1.2.840.113549.1.9.3 (contentType - 内容类型属性)";
            case "2a864886f70d010904": return "1.2.840.113549.1.9.4 (messageDigest - 签名哈希值)";
            case "2a864886f70d010905": return "1.2.840.113549.1.9.5 (signingTime - 签名时间戳元数据)";
            case "2b0e03021a":          return "1.3.14.3.2.26 (sha1 - 基础散列函数 OID)";
            case "608648016503040201": return "2.16.840.1.101.3.4.2.1 (sha-256 - 基础散列函数 OID)";
            default: return null;
        }
    }

    /**
     * 信息格式化输出
     */
    private static void printParamField(String label, Object value) {
        if (value == null) return;
        System.out.println("  * " + label + ":");
        if (value instanceof BigInteger) {
            BigInteger bi = (BigInteger) value;
            System.out.println("    [Bits] -> " + bi.bitLength());
            System.out.println("    [Dec ] -> " + bi.toString());
            System.out.println("    [Hex ] -> 0x" + bi.toString(16));
        } else {
            System.out.println("    [RawValue] -> " + value);
        }
    }

    /* 内部时序分析辅助载体 */
    private static class DnAttr {
        String oid;
        String value;
        DnAttr(String o, String v) { this.oid = o; this.value = v; }
    }
    private static class CertTime {
        String time;
        CertTime(String t) { this.time = t; }
    }

}
