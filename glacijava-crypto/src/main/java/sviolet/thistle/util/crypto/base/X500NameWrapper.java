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

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>X500Name包装类, 便于获取DN信息中的要素</p>
 * <p>例如: C=BE,O=GlobalSign nv-sa,OU=Root CA,CN=GlobalSign Root CA</p>
 * <p>获取: CN 为 GlobalSign Root CA</p>
 *
 * @author S.Violet
 */
public class X500NameWrapper {

    private final X500Name x500Name;

    /**
     * 解析DN信息为X500Name, 并用本类包装, 格式错误会抛出异常
     * @param dn DN信息, 例如: C=BE,O=GlobalSign nv-sa,OU=Root CA,CN=GlobalSign Root CA
     */
    public X500NameWrapper(String dn) {
        this.x500Name = new X500Name(dn);
    }

    /**
     * 把一个X500Name对象包装起来
     * @param x500Name X500Name
     */
    public X500NameWrapper(X500Name x500Name) {
        this.x500Name = x500Name;
    }

    /**
     * 获取DN信息中的要素(只取第一个), 可能返回空.
     * 注意, DN允许同一个类型由多个值, 例如: CN=AAA+CN=BBB, CN=CCC.
     * 这个方法只取第一个值返回.
     *
     * @param objectIdentifier 标识, 例如: BCStyle.CN
     */
    public String getObject(ASN1ObjectIdentifier objectIdentifier){
        if (x500Name == null) {
            return null;
        }
        RDN[] rdns = x500Name.getRDNs(objectIdentifier);
        if (rdns == null || rdns.length <= 0 || rdns[0] == null) {
            return null;
        }
        AttributeTypeAndValue typeAndValue = rdns[0].getFirst();
        if (typeAndValue == null) {
            return null;
        }
        return String.valueOf(typeAndValue.getValue());
    }

    /**
     * 获取DN信息中的要素(返回所有匹配的结果), 可能返回空列表.
     * 注意, DN允许同一个类型由多个值, 例如: CN=AAA+CN=BBB, CN=CCC.
     * 这个方法返回所有匹配的结果, {AAA, BBB, CCC}
     *
     * @param objectIdentifier 标识, 例如: BCStyle.CN
     */
    public List<String> getObjects(ASN1ObjectIdentifier objectIdentifier) {
        if (x500Name == null) {
            return new ArrayList<>(0);
        }
        RDN[] rdns = x500Name.getRDNs(objectIdentifier);
        if (rdns == null || rdns.length <= 0) {
            return new ArrayList<>(0);
        }
        List<String> result = new ArrayList<>(Math.max(rdns.length, 4));
        for (RDN rdn : rdns) {
            if (rdn == null) {
                continue;
            }
            if (rdn.isMultiValued()) {
                AttributeTypeAndValue[] typeAndValues = rdn.getTypesAndValues();
                for (AttributeTypeAndValue typeAndValue : typeAndValues) {
                    if (typeAndValue != null) {
                        result.add(String.valueOf(typeAndValue.getValue()));
                    }
                }
            } else {
                AttributeTypeAndValue typeAndValue = rdn.getFirst();
                if (typeAndValue != null) {
                    result.add(String.valueOf(typeAndValue.getValue()));
                }
            }
        }
        return result;
    }

    /**
     * 得到原始的X500Name对象
     */
    public X500Name getRawX500Name() {
        return x500Name;
    }

}
