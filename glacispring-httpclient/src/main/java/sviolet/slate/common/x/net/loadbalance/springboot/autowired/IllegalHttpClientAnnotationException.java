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

package sviolet.slate.common.x.net.loadbalance.springboot.autowired;

/**
 * <code>@HttpClient注解用法错误异常</code>
 */
public class IllegalHttpClientAnnotationException extends RuntimeException {

    private static final long serialVersionUID = -1430701161974732142L;

    public IllegalHttpClientAnnotationException(String message) {
        super(message);
    }

    public IllegalHttpClientAnnotationException(String message, Throwable cause) {
        super(message, cause);
    }

}
