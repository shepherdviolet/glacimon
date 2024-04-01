package com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.propertysource;

import com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.CryptoPropDecryptor;
import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.OriginLookup;
import org.springframework.core.env.MapPropertySource;

/**
 * <p>[Spring属性解密] PropertySource包装类(实现解密逻辑), 适配SpringBoot2.0</p>
 *
 * @author shepherdviolet
 */
public class CryptoMapPropertySourceForBoot2 extends CryptoMapPropertySource implements OriginLookup<String> {

    public CryptoMapPropertySourceForBoot2(MapPropertySource delegate, CryptoPropDecryptor decryptor) {
        super(delegate, decryptor);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Origin getOrigin(String key) {
        if(getDelegate() instanceof OriginLookup) {
            return ((OriginLookup<String>) getDelegate()).getOrigin(key);
        }
        return null;
    }

    @Override
    public boolean isImmutable() {
        if (getDelegate() instanceof OriginLookup) {
            return ((OriginLookup<?>) getDelegate()).isImmutable();
        }
        return OriginLookup.super.isImmutable();
    }

    @Override
    public String getPrefix() {
        if (getDelegate() instanceof OriginLookup) {
            return ((OriginLookup<?>) getDelegate()).getPrefix();
        }
        return OriginLookup.super.getPrefix();
    }

}
