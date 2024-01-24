package com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.enhanced;

import org.springframework.core.env.PropertySource;

/**
 * <p>[Spring属性解密] 支持解密的PropertySource接口, 加强模式(或CUT_IN_ENVIRONMENT模式)专用</p>
 *
 * @author shepherdviolet
 */
public interface ICryptoPropertySource<T> {

    PropertySource<T> getDelegate();

}
