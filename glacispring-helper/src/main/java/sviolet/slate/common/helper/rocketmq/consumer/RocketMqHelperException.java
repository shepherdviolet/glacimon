/*
 * Copyright (C) 2015-2019 S.Violet
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

package sviolet.slate.common.helper.rocketmq.consumer;

/**
 * @author S.Violet
 */
public class RocketMqHelperException extends Exception {

    private static final long serialVersionUID = 3092154136566006704L;

    public RocketMqHelperException(String message) {
        super(message);
    }

    public RocketMqHelperException(String message, Throwable cause) {
        super(message, cause);
    }

}
