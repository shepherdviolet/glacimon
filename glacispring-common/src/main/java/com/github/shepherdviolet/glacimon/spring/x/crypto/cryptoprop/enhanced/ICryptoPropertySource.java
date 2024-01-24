package com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.enhanced;

import org.springframework.core.env.PropertySource;

/**
 *
 *
 * @author shepherdviolet
 */
public interface ICryptoPropertySource<T> {

    PropertySource<T> getDelegate();

}
