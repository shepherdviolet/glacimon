/*
 * Copyright (C) 2015-2022 S.Violet
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

package sviolet.thistle.util.urlext.installer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Hashtable;

/**
 * The installer is a general purpose class to install an own
 * {@link URLStreamHandlerFactory} in any environment.
 *
 * @author  http://svn.apache.org/repos/asf/commons/sandbox/jnet/trunk/src/main/java/org/apache/commons/jnet
 *
 * Enhanced:
 * 1.Set synchronized
 * 2.Clear handlers when install
 *
 * @author shepherdviolet
 */
public class URLStreamHandlerFactoryInstaller {

    /**
     * Set the url stream handler factory.
     * @param factory factory
     */
    @SuppressWarnings({"unchecked", "SynchronizationOnLocalVariableOrMethodParameter"})
    public static void setURLStreamHandlerFactory(URLStreamHandlerFactory factory) {

        try {

            // if we can set the factory, its the first!

            URL.setURLStreamHandlerFactory(factory);

        } catch (Error err) {

            // let's use reflection to get the field holding the factory

            final Field[] fields = URL.class.getDeclaredFields();
            Field factoryField = null;
            Field handlersField = null;
            Field streamHandlerLockField = null;

            // get required fields
            for (final Field current : fields) {
                if (Modifier.isStatic(current.getModifiers()) && current.getType().equals(URLStreamHandlerFactory.class)) {
                    factoryField = current;
                    factoryField.setAccessible(true);
                } else if (Modifier.isStatic(current.getModifiers()) && current.getType().equals(Hashtable.class)) {
                    handlersField = current;
                    handlersField.setAccessible(true);
                } else if (Modifier.isStatic(current.getModifiers()) && current.getType().equals(Object.class)) {
                    streamHandlerLockField = current;
                    streamHandlerLockField.setAccessible(true);
                }
            }

            // check
            if ( factoryField == null ) {
                throw new RuntimeException("Unable to detect static field in the URL class for the URLStreamHandlerFactory (factory). Please report this error together with your exact environment to the Apache Excalibur project.");
            }
            if ( handlersField == null ) {
                throw new RuntimeException("Unable to detect static field in the URL class for the Hashtable<String,URLStreamHandler> (handlers). Please report this error together with your exact environment to the Apache Excalibur project.");
            }
            if ( streamHandlerLockField == null ) {
                throw new RuntimeException("Unable to detect static field in the URL class for the Object (streamHandlerLock). Please report this error together with your exact environment to the Apache Excalibur project.");
            }

            // get synchronized lock
            Object streamHandlerLock;
            try {
                streamHandlerLock = streamHandlerLockField.get(null);
            } catch (Throwable e) {
                throw new RuntimeException("Unable to set url stream handler factory " + factory + ", get streamHandlerLockField failed", e);
            }
            if (streamHandlerLock == null) {
                throw new RuntimeException("Unable to set url stream handler factory " + factory + ", get streamHandlerLockField returns null");
            }

            synchronized (streamHandlerLock) {

                try {
                    URLStreamHandlerFactory oldFactory = (URLStreamHandlerFactory) factoryField.get(null);
                    if (factory instanceof ParentAwareURLStreamHandlerFactory) {
                        ((ParentAwareURLStreamHandlerFactory) factory).setParentFactory(oldFactory);
                    }
                    factoryField.set(null, factory);
                } catch (Throwable e) {
                    throw new RuntimeException("Unable to set url stream handler factory " + factory + ", replace factory failed", e);
                }

                try {
                    Hashtable<String,URLStreamHandler> handlers = (Hashtable<String, URLStreamHandler>) handlersField.get(null);
                    handlers.clear();
                } catch (Throwable e) {
                    throw new RuntimeException("Unable to set url stream handler factory " + factory + ", clear handlers (cache) failed", e);
                }

            }
        }

    }

    /**
     * Determine if the protocol is installed
     * @param protocol protocol name
     * @return true: installed false: not installed
     */
    public static boolean isProtocolInstalled(String protocol) {
        try {
            new URL(protocol + ":test");
        } catch (MalformedURLException e) {
            if (e.getMessage().startsWith("unknown protocol")) {
                return false;
            }
        }
        return true;
    }

}
