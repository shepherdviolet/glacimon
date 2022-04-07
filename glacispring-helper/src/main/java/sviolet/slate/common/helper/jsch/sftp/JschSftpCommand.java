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

package sviolet.slate.common.helper.jsch.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

/**
 * SFTP操作接口
 * @param <T>
 *
 * @author S.Violet
 */
public interface JschSftpCommand <T> {

    /**
     * 操作流程
     * @param channel channel
     * @return 自定义返回值
     * @throws SftpException 操作异常
     */
    T onCommand(ChannelSftp channel) throws SftpException;

    /**
     * SFTP连接失败
     * @param t 异常
     * @return 自定义返回值
     */
    T onConnectFailed(Throwable t);

    /**
     * SFTP操作异常
     * @param t 异常
     * @return 自定义返回值
     */
    T onException(Throwable t);

}
