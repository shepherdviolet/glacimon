/*
 * Copyright (C) 2022-2022 S.Violet
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

package com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * <p>[Spring属性解密] 启用属性解密 (For springboot)</p>
 *
 * <p>
 * 说明:<br>
 * 1.普通模式(NORMAL)支持'@Value'和'XML property'中占位符(placeholder)的解密, 不支持Environment#getProperty解密, 侵入点少兼容性好. <br>
 * 2.加强模式(ENHANCED)支持Environment#getProperty解密, 侵入点较多, 可能会出现一些兼容性问题. <br>
 * </p>
 *
 * <p></p>
 * <p>启用:</p>
 *
 * <pre>
 *  <code>@Configuration</code>
 *  <code>@EnableCryptoProp</code>
 *  public class MyConfiguration {
 *      // ...
 *  }
 * </pre>
 *
 * <p>配置密钥:</p>
 *
 * <pre>
 * glacispring.crypto-prop.key=[密钥内容]
 * P.S.非必须, 不配置密钥就不解密
 * </pre>
 *
 * <pre>
 * [密钥内容]示例
 * 对称加密:
 * 密钥字符串: aes:IPRGkutx3FfsCYty (BASE64)
 * 密钥文件路径: aes:file:/home/yourname/crypto_prop_key.txt (BASE64, 文件里不要有多余的换行)
 * 密钥类路径: aes:classpath:config/crypto_prop_key.txt (BASE64, 文件里不要有多余的换行)
 * 非对称加密:
 * 私钥字符串: rsa:MIICdwIB......KM2wnjk1ZY= (DER格式)
 * 私钥文件路径: rsa:file:/home/yourname/crypto_prop_private.pem (PEM格式, 文件里不要有多余的换行)
 * 私钥类路径: rsa:classpath:config/crypto_prop_private.pem (PEM格式, 文件里不要有多余的换行)
 * </pre>
 *
 * <p>属性用工具类加密:</p>
 *
 * <pre>
 * String plain = "decrypted successfully 111";
 *
 * //非对称加密
 * System.out.println(SimpleCryptoPropUtils.encryptAndWrap(plain,
 *         "rsa:classpath:config/demo/common/cryptoprop/cryptoprop-public-key.pem"));
 *
 * // 对称加密
 * System.out.println(SimpleCryptoPropUtils.encryptAndWrap(plain,
 *         "aes:classpath:config/demo/common/cryptoprop/cryptoprop-key.txt"));
 * </pre>
 *
 * <p>在@Value中引用密文属性:</p>
 *
 * <pre>
 * <code/>@Value("${foo.param.name}")
 * </pre>
 *
 * <p>在XML中引用密文</p>
 *
 * <pre>
 * &lt property name="foo" value="${foo.param.name}" /&gt
 * </pre>
 *
 * <p>在properties或yaml中配置密文属性</p>
 *
 * <pre>
 * foo:
 *   param:
 *     name: CIPHER(bhkSKmy/80y8kW91XRFoVQesS/UpT6Mq1zWxcuMUGNQ=)
 * </pre>
 *
 * <pre>
 * foo.param.name=CIPHER(bhkSKmy/80y8kW91XRFoVQesS/UpT6Mq1zWxcuMUGNQ=)
 * </pre>
 *
 * @author shepherdviolet
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({CryptoPropConfiguration.class})
public @interface EnableCryptoProp {

}
