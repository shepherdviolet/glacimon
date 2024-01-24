package com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.enhanced;

import com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.CryptoPropDecryptor;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Map;

/**
 * <p>[Spring属性解密] PropertySource包装类(实现解密逻辑), 加强模式(或CUT_IN_ENVIRONMENT模式)专用</p>
 *
 * @author shepherdviolet
 */
public class CryptoMapPropertySource extends MapPropertySource implements ICryptoPropertySource<Map<String, Object>> {

    private final MapPropertySource delegate;
    private final CryptoPropDecryptor decryptor;

    public CryptoMapPropertySource(MapPropertySource delegate, CryptoPropDecryptor decryptor) {
        super(delegate.getName(), delegate.getSource());
        this.delegate = delegate;
        this.decryptor = decryptor;
    }

    @Override
    public Object getProperty(String name) {
        Object value = delegate.getProperty(name);
        // 如果属性值是String则尝试解密
        if (value instanceof String) {
            return decryptor.decrypt(name, (String) value);
        }
        return value;
    }

    @Override
    public PropertySource<Map<String, Object>> getDelegate() {
        return delegate;
    }

}
