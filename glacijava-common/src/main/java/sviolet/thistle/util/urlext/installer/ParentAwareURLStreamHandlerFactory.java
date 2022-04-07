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

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * A parent aware url stream handler factory delegates to a parent
 * url stream handler factory,
 *
 * @author http://svn.apache.org/repos/asf/commons/sandbox/jnet/trunk/src/main/java/org/apache/commons/jnet
 */
public abstract class ParentAwareURLStreamHandlerFactory implements URLStreamHandlerFactory {

    protected URLStreamHandlerFactory parentFactory;

    /**
     * Set the parent factory.
     * @param factory factory
     */
    public void setParentFactory(URLStreamHandlerFactory factory) {
        this.parentFactory = factory;
    }

    /**
     * Return the parent factory.
     * @return The parent factory.
     */
    public URLStreamHandlerFactory getParent() {
        return this.parentFactory;
    }

    /**
     * @see java.net.URLStreamHandlerFactory#createURLStreamHandler(java.lang.String)
     */
    public URLStreamHandler createURLStreamHandler(String protocol) {
        URLStreamHandler handler = this.create(protocol);
        if ( handler == null && this.parentFactory != null ) {
            handler = this.parentFactory.createURLStreamHandler(protocol);
        }
        return handler;
    }

    /**
     * This method can be overwritten by subclasses to instantiate url stream
     * handlers for the given protocol.
     * @param protocol The protocol.
     * @return A url stream handler for the protocol or null.
     */
    protected abstract URLStreamHandler create(String protocol);
}
