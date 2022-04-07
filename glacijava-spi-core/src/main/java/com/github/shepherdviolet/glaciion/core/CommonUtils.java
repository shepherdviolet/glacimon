/*
 * Copyright (C) 2019-2019 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/glaciion
 * Email: shepherdviolet@163.com
 */

package com.github.shepherdviolet.glaciion.core;

import com.github.shepherdviolet.glaciion.Glaciion;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

class CommonUtils {

    private static final AtomicInteger LOADER_ID = new AtomicInteger(0);

    /**
     * loaderId of service loader
     */
    static String generateLoaderId(){
        return String.valueOf(LOADER_ID.getAndIncrement());
    }

    /**
     * Close closeable quietly
     */
    static void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignore) {
        }
    }

    /**
     * Check if string is empty or null
     */
    static boolean isEmpty(String input){
        return input == null || input.length() <= 0;
    }

    /**
     * Check if string is blank, empty or null
     */
    static boolean isEmptyOrBlank(String input){
        return isEmpty(input) || input.trim().length() <= 0;
    }

    /**
     * Digest file, get hash
     */
    @SuppressWarnings({"lgtm[java/weak-cryptographic-algorithm]"})
    static String digest(InputStream inputStream, String type) throws IOException {
        if (inputStream == null){
            throw new NullPointerException("inputStream is null");
        }
        try {
            // About suppressed warnings: The MD5 algorithm is only used for troubleshooting, no security risk
            MessageDigest cipher = MessageDigest.getInstance(type);
            byte[] buff = new byte[1024];
            int size;
            while((size = inputStream.read(buff)) != -1){
                cipher.update(buff, 0, size);
            }
            return bytesToHex(cipher.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No Such Algorithm:" + type, e);
        } finally {
            closeQuietly(inputStream);
        }
    }

    /**
     * bytes to hex
     */
    static String bytesToHex(byte[] bytes){
        if (bytes == null) {
            return null;
        }
        if (bytes.length <= 0){
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (byte unit : bytes) {
            int unitInt = unit & 0xFF;
            String unitHex = Integer.toHexString(unitInt);
            if (unitHex.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(unitHex);
        }
        return stringBuilder.toString();
    }

    /**
     * get method caller info
     */
    static String getCaller() {
        StackTraceElement stackTraceElement = getMethodCaller(EQUAL_SKIPS, STARTS_WITH_SKIPS);
        if (stackTraceElement != null) {
            return stackTraceElement.getClassName() + "#" + stackTraceElement.getMethodName();
        }
        return "unknown#unknown";
    }

    private static final Set<String> EQUAL_SKIPS = new HashSet<>(Arrays.asList(
            CommonUtils.class.getName(),
            Glaciion.CLASS_NAME,
            PreLoader.CLASS_NAME,
            SingleServiceLoader.CLASS_NAME,
            MultipleServiceLoader.CLASS_NAME

    ));

    private static final Collection<String> STARTS_WITH_SKIPS = Arrays.asList(
            "org.springframework.cglib.proxy.Proxy$ProxyImpl$$"
    );

    private static StackTraceElement getMethodCaller(Set<String> equalSkips, Collection<String> startsWithSkips) {
        //获取当前堆栈
        StackTraceElement[] elements = new Throwable().getStackTrace();
        //跳过头两个元素(getCaller方法和调用getCaller的方法)
        if (elements != null && elements.length > 2) {
            //遍历堆栈
            int i = 2;
            for (; i < elements.length; i++) {
                //当前元素
                StackTraceElement element = elements[i];
                //如果类名与skipEquals中的一个相同, 则跳过该元素查找下一个
                if (equalSkips != null && equalSkips.contains(element.getClassName())) {
                    continue;
                }
                //如果类名是skipStarts中的任意一个作为开头的, 则跳过该元素查找下一个
                if (startsWithSkips != null && isStackTraceStartsWithSkips(startsWithSkips, element)) {
                    continue;
                }
                //如果未跳过则当前元素就是调用者
                return element;
            }
        }
        //找不到调用者
        return null;
    }

    private static boolean isStackTraceStartsWithSkips(Collection<String> startsWithSkips, StackTraceElement element) {
        String className = element.getClassName();
        for (String skipStart : startsWithSkips) {
            if (skipStart != null && className.startsWith(skipStart)) {
                return true;
            }
        }
        return false;
    }

}
