package com.github.shepherdviolet.glacimon.spring.basic.servlet;

import com.github.shepherdviolet.glacimon.java.datastruc.IgnoreCaseHashMap;

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
    public static Map<String, String> getHttpHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, String> headerMap = new IgnoreCaseHashMap<>();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headerMap.put(headerName, headerValue);
        }
        return headerMap;
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
