package com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.propertysource;

import org.springframework.core.env.PropertySource;

/**
 * <p>[Spring属性解密] 支持解密的PropertySource接口</p>
 *
 * @author shepherdviolet
 */
public interface ICryptoPropertySource<T> {

    PropertySource<T> getDelegate();

}
