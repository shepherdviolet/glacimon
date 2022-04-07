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
 * This is a wrapper to make a url stream handler factory a parent aware url stream
 *
 * @author  http://svn.apache.org/repos/asf/commons/sandbox/jnet/trunk/src/main/java/org/apache/commons/jnet
 */
public class URLStreamHandlerFactoryWrapper extends ParentAwareURLStreamHandlerFactory {

    protected final URLStreamHandlerFactory wrapper;

    public URLStreamHandlerFactoryWrapper(URLStreamHandlerFactory f) {
        this.wrapper = f;
    }

    /**
     * @see sviolet.thistle.util.urlext.installer.ParentAwareURLStreamHandlerFactory#create(java.lang.String)
     */
    protected URLStreamHandler create(String protocol) {
        return this.wrapper.createURLStreamHandler(protocol);
    }

}