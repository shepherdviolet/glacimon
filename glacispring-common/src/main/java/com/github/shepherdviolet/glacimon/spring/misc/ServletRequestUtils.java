/*
 * Copyright (C) 2022-2025 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/glacimon
 * Email: shepherdviolet@163.com
 */

package com.github.shepherdviolet.glacimon.spring.misc;

import com.github.shepherdviolet.glacimon.java.datastruc.IgnoreCaseHashMap;
import com.github.shepherdviolet.glacimon.java.net.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * ServletRequest Utils
 *
 * @author shepherdviolet
 */
public class ServletRequestUtils {

    /**
     * get http uri
     * @param request HttpServletRequest
     */
    public static String getUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    /**
     * get http method
     * @param request HttpServletRequest
     */
    public static String getMethod(HttpServletRequest request) {
        return request.getMethod();
    }

    /**
     * get http headers
     * @param request HttpServletRequest
     */
    public static HttpHeaders getHttpHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        HttpHeaders httpHeaders = new HttpHeaders();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> values = request.getHeaders(headerName);
            while (values != null && values.hasMoreElements()) {
                httpHeaders.add(headerName, values.nextElement());
            }
        }
        return httpHeaders;
    }

    /**
     * get http parameters (only from url query)
     * @param request HttpServletRequest
     */
    public static Map<String, String> getHttpParams(HttpServletRequest request) {
        Map<String, String> httpParams = new HashMap<>();
        try {
            /*
             * 标记request为usingInputStream, 使得request.getParameterMap()时不会去解析报文体, 只从URL中获取parameters.
             * 如果这里不标记usingInputStream, 先调用getHttpParams获取Params再调用readBody读取报文体的时候, 会发现报文体空了,
             * Params里出现了一个key为报文体的项. 不必担心request.getInputStream()返回的输入流没有关闭, Tomcat容易会在请求
             * 结束后关闭输入流.
             */
            request.getInputStream();
        } catch (IOException ignore) {
        }
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String paramKey = entry.getKey();
            String[] paramValues = entry.getValue();
            if (paramValues != null && paramValues.length > 0) {
                httpParams.put(paramKey, paramValues[0]);
            }
        }
        return httpParams;
    }

    /**
     * get http request body
     * @param request HttpServletRequest
     */
    public static byte[] readBody(HttpServletRequest request) throws IOException {
        try (InputStream inputStream = request.getInputStream()) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            return byteArrayOutputStream.toByteArray();
        }
    }

}
