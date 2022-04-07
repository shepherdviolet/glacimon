/*
 * Copyright (C) 2015-2020 S.Violet
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

package sviolet.thistle.util.crypto.base;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.util.Date;
import java.util.Set;

/**
 * 提供验证所需的证书颁发者
 *
 * @param <ParameterType> IssuerProvider接收的参数类型, 可选
 * @see SimpleIssuerProvider
 * @see RootIssuerProvider
 */
public interface IssuerProvider<ParameterType> {

    /**
     * 根据DN信息查找颁发者的证书
     *
     * @param dn DN信息
     * @param issuerProviderParameter IssuerProvider接收的参数, 可选
     * @return 颁发者的证书
     */
    X509Certificate findIssuer(String dn, ParameterType issuerProviderParameter) throws CertificateException;


    /* ************************************************************************************************************** */


    /**
     * [仅限于IssuerProvider使用] 将一个非根证书当成根证书使用, 即不再继续查找签发者. 见BaseBCCertificateUtils.
     */
    class ActAsRoot extends X509Certificate {

        private X509Certificate actualCertificate;

        public ActAsRoot(X509Certificate actualCertificate) {
            this.actualCertificate = actualCertificate;
        }

        public X509Certificate getActualCertificate(){
            return actualCertificate;
        }

        @Override
        public byte[] getEncoded() throws CertificateEncodingException {
            return actualCertificate.getEncoded();
        }

        @Override
        public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
            actualCertificate.verify(key);
        }

        @Override
        public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
            actualCertificate.verify(key, sigProvider);
        }

        @Override
        public String toString() {
            return "ActAsRoot{" +
                    actualCertificate +
                    '}';
        }

        @Override
        public PublicKey getPublicKey() {
            return actualCertificate.getPublicKey();
        }

        @Override
        public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
            actualCertificate.checkValidity();
        }

        @Override
        public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
            actualCertificate.checkValidity(date);
        }

        @Override
        public int getVersion() {
            return actualCertificate.getVersion();
        }

        @Override
        public BigInteger getSerialNumber() {
            return actualCertificate.getSerialNumber();
        }

        @Override
        public Principal getIssuerDN() {
            return actualCertificate.getIssuerDN();
        }

        @Override
        public Principal getSubjectDN() {
            return actualCertificate.getSubjectDN();
        }

        @Override
        public Date getNotBefore() {
            return actualCertificate.getNotBefore();
        }

        @Override
        public Date getNotAfter() {
            return actualCertificate.getNotAfter();
        }

        @Override
        public byte[] getTBSCertificate() throws CertificateEncodingException {
            return actualCertificate.getTBSCertificate();
        }

        @Override
        public byte[] getSignature() {
            return actualCertificate.getSignature();
        }

        @Override
        public String getSigAlgName() {
            return actualCertificate.getSigAlgName();
        }

        @Override
        public String getSigAlgOID() {
            return actualCertificate.getSigAlgOID();
        }

        @Override
        public byte[] getSigAlgParams() {
            return actualCertificate.getSigAlgParams();
        }

        @Override
        public boolean[] getIssuerUniqueID() {
            return actualCertificate.getIssuerUniqueID();
        }

        @Override
        public boolean[] getSubjectUniqueID() {
            return actualCertificate.getSubjectUniqueID();
        }

        @Override
        public boolean[] getKeyUsage() {
            return actualCertificate.getKeyUsage();
        }

        @Override
        public int getBasicConstraints() {
            return actualCertificate.getBasicConstraints();
        }

        @Override
        public boolean hasUnsupportedCriticalExtension() {
            return actualCertificate.hasUnsupportedCriticalExtension();
        }

        @Override
        public Set<String> getCriticalExtensionOIDs() {
            return actualCertificate.getCriticalExtensionOIDs();
        }

        @Override
        public Set<String> getNonCriticalExtensionOIDs() {
            return actualCertificate.getNonCriticalExtensionOIDs();
        }

        @Override
        public byte[] getExtensionValue(String oid) {
            return actualCertificate.getExtensionValue(oid);
        }

    }

}
