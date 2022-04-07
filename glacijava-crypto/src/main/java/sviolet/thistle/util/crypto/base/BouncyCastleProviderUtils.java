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

package sviolet.thistle.util.crypto.base;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.lang.reflect.Field;
import java.security.Provider;
import java.security.Security;

public class BouncyCastleProviderUtils {

    /**
     * Allow modification of BC's name under special circumstances, e.g. sm-crypto-allinone
     */
    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
    private static String PROVIDER_NAME = BouncyCastleProvider.PROVIDER_NAME;

    public synchronized static void installProvider(){
        if (Security.getProvider(PROVIDER_NAME) == null) {
            if (PROVIDER_NAME.equals(BouncyCastleProvider.PROVIDER_NAME)) {
                // add provider normally
                Security.addProvider(new BouncyCastleProvider());
            } else {
                // add provider with custom name
                try {
                    Provider provider = new BouncyCastleProvider();
                    Field nameField = Provider.class.getDeclaredField("name");
                    nameField.setAccessible(true);
                    nameField.set(provider, PROVIDER_NAME);
                    Security.addProvider(provider);
                } catch (Throwable t) {
                    throw new RuntimeException("BouncyCastleProviderUtils | Install BouncyCastleProvider failed with custom provider name " + PROVIDER_NAME, t);
                }
            }
        }
    }

    public static String getProviderName() {
        return PROVIDER_NAME;
    }

}
