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
 * 请求初始化异常(通常是网络请求发送前的异常, 准备阶段异常)
 *
 * @author S.Violet
 */
public class RequestBuildException extends Exception {

    private static final long serialVersionUID = -8250196145180572152L;

    public RequestBuildException(String message) {
        super(message);
    }

    public RequestBuildException(String message, Throwable cause) {
        super(message, cause);
    }

}
