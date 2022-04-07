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
 * Project GitHub: https://github.com/shepherdviolet/slate
 * Email: shepherdviolet@163.com
 */

package sviolet.slate.common.x.net.loadbalance.classic;

/**
 * Http请求拒绝异常(网络请求发送后的异常, HTTP响应码不为2XX)
 *
 * @author S.Violet
 */
public class HttpRejectException extends Exception {

    private static final long serialVersionUID = 8474850236277260027L;

    private int code;
    private String message;

    public HttpRejectException(int code, String message) {
        super("Http rejected, code:" + code + ", message:" + message);
        this.code = code;
        this.message = message;
    }

    /**
     * HTTP响应码
     * @return HTTP响应码
     */
    public int getResponseCode() {
        return code;
    }

    /**
     * HTTP响应信息
     * @return HTTP响应信息
     */
    public String getResponseMessage(){
        return message;
    }

}
