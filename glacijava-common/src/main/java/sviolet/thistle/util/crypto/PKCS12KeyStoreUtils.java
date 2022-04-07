/*
 * Copyright (C) 2015-2018 S.Violet
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

package sviolet.thistle.util.crypto;

import sviolet.thistle.util.common.CloseableUtils;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * <p>PKCS12密钥文件工具(p12/pfx)</p>
 *
 * <p>
 *     OPENSSL查看p12文件信息:
 *     openssl pkcs12 -in pkcs12-test.p12 -info -noout
 * </p>
 *
 * <p>
 *     Windows查看证书信息:
 *     certmgr.msc
 * </p>
 *
 * <p>
 *     可以用同样的代码操作 JKS 密钥文件, 只要 ALGORITHM = "JKS" 即可.
 *     没有把PKCS12和JKS整合是因为AdvancedPKCS12KeyStoreUtils中操作PKCS12的代码无法通用, 而JKS文件可以用keytool命令转PKCS12.
 *     keytool -list -v -keystore name.jks
 *     keytool -importkeystore -srckeystore name.jks -destkeystore name.p12 -deststoretype pkcs12
 * </p>
 *
 * @author S.Violet
 */
public class PKCS12KeyStoreUtils {

    private static final String ALGORITHM = "PKCS12";

    /**
     * 将证书和私钥保存到p12/pfx文件中, 本JDK版本较弱, 国密等算法请用thistle-crypto-plus的AdvancedPKCS12KeyStoreUtils
     *
     * <p>{@code
     *      //output user p12
     *      PKCS12KeyStoreUtils.storeCertificateAndKey(
     *          "user-cert.p12",
     *          "000000",
     *          "Thistle test user cert",
     *          userPrivateKey,
     *          userCertificate);
     *
     *      ////output root p12
     *      PKCS12KeyStoreUtils.storeCertificateAndKey(
     *          "root-cert.p12",
     *          "000000",
     *          "Thistle test ca cert",
     *          null,
     *          caCertificate);
     * }</p>
     *
     * @param keyStorePath keyStore的文件路径
     * @param keyStorePassword keyStore的密码
     * @param alias 证书和私钥的别名
     * @param privateKey 证书对应的私钥(如果为空, 则仅保存证书)
     * @param certificateChain 证书链, 通常传入一个证书即可, 用户证书/CA证书/根证书一般会分别导出独立的文件. 如果需要一次性导出整个
     *                         证书链到一个文件, 也可以传入多个证书, 顺序是个人证书->二级CA证书->根证书, {userCertificate, subCaCertificate, rootCertificate}
     */
    public static void storeCertificateAndKey(String keyStorePath, String keyStorePassword, String alias, PrivateKey privateKey, Certificate... certificateChain) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        File keyStoreFile = new File(keyStorePath);
        File dirFile = keyStoreFile.getParentFile();
        if (dirFile != null && !dirFile.exists()){
            if (!dirFile.mkdirs()){
                throw new IOException("Can not make directory for keyStore, path:" + dirFile.getAbsolutePath());
            }
        }
        try (FileOutputStream keyStoreOutputStream = new FileOutputStream(keyStoreFile)) {
            storeCertificateAndKey(keyStoreOutputStream, keyStorePassword, alias, privateKey, certificateChain);
        }
    }

    /**
     * 将证书和私钥保存到p12/pfx文件中, 本JDK版本较弱, 国密等算法请用thistle-crypto-plus的AdvancedPKCS12KeyStoreUtils
     *
     * <p>{@code
     *      //output user p12
     *      PKCS12KeyStoreUtils.storeCertificateAndKey(
     *          outputStream1,
     *          "000000",
     *          "Thistle test user cert",
     *          userPrivateKey,
     *          userCertificate);
     *
     *      ////output root p12
     *      PKCS12KeyStoreUtils.storeCertificateAndKey(
     *          outputStream2,
     *          "000000",
     *          "Thistle test ca cert",
     *          null,
     *          caCertificate);
     * }</p>
     *
     * @param keyStoreOutputStream keyStore的输出流, 完成后会关闭
     * @param keyStorePassword keyStore的密码
     * @param alias 证书和私钥的别名, 无私钥且证书不止一个时, 别名会变成0,1,2,...
     * @param privateKey 证书对应的私钥(如果为空, 则仅保存证书)
     * @param certificateChain 证书链, 通常传入一个证书即可, 用户证书/CA证书/根证书一般会分别导出独立的文件. 如果需要一次性导出整个
     *                         证书链到一个文件, 也可以传入多个证书, 顺序是个人证书->二级CA证书->根证书, {userCertificate, subCaCertificate, rootCertificate}
     */
    public static void storeCertificateAndKey(OutputStream keyStoreOutputStream, String keyStorePassword, String alias, PrivateKey privateKey, Certificate... certificateChain) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        try {
            if (certificateChain == null || certificateChain.length <= 0){
                throw new IllegalAccessError("Null or empty certificateChain");
            }
            KeyStore keyStore = KeyStore.getInstance(ALGORITHM);
            keyStore.load(null, null);
            if (privateKey != null) {
                keyStore.setKeyEntry(alias, privateKey, keyStorePassword != null ? keyStorePassword.toCharArray() : null, certificateChain);
            } else if (certificateChain.length == 1) {
                keyStore.setCertificateEntry(alias, certificateChain[0]);
            } else {
                //无私钥且证书不止一个时, 别名改成0,1,2,...
                int i = 0;
                for (Certificate certificate : certificateChain){
                    keyStore.setCertificateEntry(String.valueOf(i++), certificate);
                }
            }
            keyStore.store(keyStoreOutputStream, keyStorePassword != null ? keyStorePassword.toCharArray() : null);
        } finally {
            CloseableUtils.closeQuiet(keyStoreOutputStream);
        }
    }

    /**
     * 从p12/pfx文件中遍历alias, 本JDK版本较弱, 国密等算法请用thistle-crypto-plus的AdvancedPKCS12KeyStoreUtils
     *
     * <pre>{@code
     *      Enumeration<String> aliases = PKCS12KeyStoreUtils.loadAliases(
     *          "ca-cert.p12",
     *          "000000"
     *          );
     * }</pre>
     *
     * @param keyStorePath keyStore路径
     * @param keystorePassword keyStore密码
     */
    public static Enumeration<String> loadAliases(String keyStorePath, String keystorePassword) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        File keyStoreFile = new File(keyStorePath);
        if (!keyStoreFile.exists()){
            throw new IOException("Can not find keyStore file, path:" + keyStoreFile.getAbsolutePath());
        }
        return loadAliases(new FileInputStream(keyStoreFile), keystorePassword);
    }

    /**
     * 从p12/pfx文件中遍历alias, 本JDK版本较弱, 国密等算法请用thistle-crypto-plus的AdvancedPKCS12KeyStoreUtils
     *
     * <pre>{@code
     *      Enumeration<String> aliases = PKCS12KeyStoreUtils.loadAliases(
     *          inputStream,
     *          "000000"
     *          );
     * }</pre>
     *
     * @param inputStream keyStore输入流, 完成后会关闭
     * @param keystorePassword keyStore密码
     */
    public static Enumeration<String> loadAliases(InputStream inputStream, String keystorePassword) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        try {
            KeyStore keyStore = KeyStore.getInstance(ALGORITHM);
            keyStore.load(inputStream, keystorePassword != null ? keystorePassword.toCharArray() : null);
            return keyStore.aliases();
        } finally {
            CloseableUtils.closeQuiet(inputStream);
        }
    }

    /**
     * 从p12/pfx文件中读取证书和私钥, 本JDK版本较弱, 国密等算法请用thistle-crypto-plus的AdvancedPKCS12KeyStoreUtils
     *
     * <pre>{@code
     *      PKCS12KeyStoreUtils.CertificateChainAndKey certificateChainAndKey = PKCS12KeyStoreUtils.loadCertificateAndKey(
     *          "ca-cert.p12",
     *          "000000",
     *          "Thistle test ca alias"
     *          );
     * }</pre>
     *
     * @param keyStorePath keyStore路径
     * @param keystorePassword keyStore密码
     * @param alias 证书和私钥的别名
     */
    public static CertificateChainAndKey loadCertificateAndKey(String keyStorePath, String keystorePassword, String alias) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        File keyStoreFile = new File(keyStorePath);
        if (!keyStoreFile.exists()){
            throw new IOException("Can not find keyStore file, path:" + keyStoreFile.getAbsolutePath());
        }
        return loadCertificateAndKey(new FileInputStream(keyStoreFile), keystorePassword, alias);
    }

    /**
     * 从p12/pfx文件中读取证书和私钥, 本JDK版本较弱, 国密等算法请用thistle-crypto-plus的AdvancedPKCS12KeyStoreUtils
     *
     * <pre>{@code
     *      PKCS12KeyStoreUtils.CertificateChainAndKey certificateChainAndKey = PKCS12KeyStoreUtils.loadCertificateAndKey(
     *          inputStream,
     *          "000000",
     *          "Thistle test ca alias"
     *          );
     * }</pre>
     *
     * @param inputStream keyStore输入流, 完成后会关闭
     * @param keystorePassword keyStore密码
     * @param alias 证书和私钥的别名
     */
    public static CertificateChainAndKey loadCertificateAndKey(InputStream inputStream, String keystorePassword, String alias) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        try {
            KeyStore keyStore = KeyStore.getInstance(ALGORITHM);
            keyStore.load(inputStream, keystorePassword != null ? keystorePassword.toCharArray() : null);
            Certificate[] certificateChain = keyStore.getCertificateChain(alias);
            if (certificateChain == null){
                Certificate certificate = keyStore.getCertificate(alias);
                if (certificate != null) {
                    certificateChain = new Certificate[]{certificate};
                }
            }
            return new CertificateChainAndKey(alias, certificateChain, (PrivateKey) keyStore.getKey(alias, keystorePassword != null ? keystorePassword.toCharArray() : null));
        } finally {
            CloseableUtils.closeQuiet(inputStream);
        }
    }

    /**
     * 从p12/pfx文件中读取证书和私钥, 本JDK版本较弱, 国密等算法请用thistle-crypto-plus的AdvancedPKCS12KeyStoreUtils
     *
     * <pre>{@code
     *      List<PKCS12KeyStoreUtils.CertificateChainAndKey> certificateChainAndKeyList = PKCS12KeyStoreUtils.loadAllCertificateAndKey(
     *          "ca-cert.p12",
     *          "000000"
     *          );
     * }</pre>
     *
     * @param keyStorePath keyStore路径
     * @param keystorePassword keyStore密码
     */
    public static List<CertificateChainAndKey> loadAllCertificateAndKey(String keyStorePath, String keystorePassword) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        File keyStoreFile = new File(keyStorePath);
        if (!keyStoreFile.exists()){
            throw new IOException("Can not find keyStore file, path:" + keyStoreFile.getAbsolutePath());
        }
        return loadAllCertificateAndKey(new FileInputStream(keyStoreFile), keystorePassword);
    }

    /**
     * 从p12/pfx文件中读取证书和私钥, 本JDK版本较弱, 国密等算法请用thistle-crypto-plus的AdvancedPKCS12KeyStoreUtils
     *
     * <pre>{@code
     *      List<PKCS12KeyStoreUtils.CertificateChainAndKey> certificateChainAndKeyList = PKCS12KeyStoreUtils.loadAllCertificateAndKey(
     *          inputStream,
     *          "000000"
     *          );
     * }</pre>
     *
     * @param inputStream keyStore输入流, 完成后会关闭
     * @param keystorePassword keyStore密码
     */
    public static List<CertificateChainAndKey> loadAllCertificateAndKey(InputStream inputStream, String keystorePassword) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        try {
            KeyStore keyStore = KeyStore.getInstance(ALGORITHM);
            keyStore.load(inputStream, keystorePassword != null ? keystorePassword.toCharArray() : null);
            List<CertificateChainAndKey> list = new ArrayList<>(1);
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Certificate[] certificateChain = keyStore.getCertificateChain(alias);
                if (certificateChain == null) {
                    Certificate certificate = keyStore.getCertificate(alias);
                    if (certificate != null) {
                        certificateChain = new Certificate[]{certificate};
                    }
                }
                list.add(new CertificateChainAndKey(alias, certificateChain, (PrivateKey) keyStore.getKey(alias, keystorePassword != null ? keystorePassword.toCharArray() : null)));
            }
            return list;
        } finally {
            CloseableUtils.closeQuiet(inputStream);
        }
    }

    public static class CertificateChainAndKey {

        private String alias;
        private Certificate[] certificateChain;
        private PrivateKey privateKey;

        protected CertificateChainAndKey(String alias, Certificate[] certificateChain, PrivateKey privateKey) {
            this.alias = alias;
            this.certificateChain = certificateChain;
            this.privateKey = privateKey;
        }

        public String getAlias() {
            return alias;
        }

        /**
         * 获得证书链
         */
        public Certificate[] getCertificateChain() {
            return certificateChain;
        }

        /**
         * 获得私钥
         */
        public PrivateKey getPrivateKey() {
            return privateKey;
        }

        @Override
        public String toString() {
            return "CertificateChainAndKey{" +
                    "alias='" + alias + '\'' +
                    ", certificateChain=" + Arrays.toString(certificateChain) +
                    ", privateKey=" + privateKey +
                    '}';
        }
    }

}
