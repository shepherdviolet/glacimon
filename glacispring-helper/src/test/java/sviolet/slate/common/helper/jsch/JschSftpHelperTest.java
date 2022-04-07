/*
 * Copyright (C) 2015-2020 S.Violet
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

package sviolet.slate.common.helper.jsch;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import sviolet.slate.common.helper.jsch.sftp.JschSftpCommand;
import sviolet.slate.common.helper.jsch.sftp.JschSftpHelper;

import java.util.Vector;

/**
 * <p>JSch SFTP 测试案例</p>
 *
 * <p>需要准备一个开启了SFTP关闭了防火墙的linux服务器, 在下面程序中配置IP/端口/用户名/密码, 在下面程序中配置需要ls的路径,
 * 配置需要上传的文件和目标路径, 配置需要下载的文件和目标路径.</p>
 *
 * @author S.Violet
 */
public class JschSftpHelperTest {

    public static void main(String[] args){

        //实例化JschHelper(可单例)
        JschHelper jsch = new JschHelper();

        //实例化JschSftpHelper(可单例)
        JschSftpHelper helper = new JschSftpHelper();
        helper.setJsch(jsch);
        helper.setHost("192.168.163.136");
        helper.setPort(17799);
        helper.setUsername("root");
        helper.setPassword("root");
        helper.setTimeout(5000);

        //创建连接, 执行一系列操作, 关闭连接
        String result = helper.command(new JschSftpCommand<String>() {
            @SuppressWarnings("unchecked")
            @Override
            public String onCommand(ChannelSftp channel) throws SftpException {
                //cd
                channel.cd("/home/sviolet");

                //ls
                Vector<ChannelSftp.LsEntry> lsVector = channel.ls("/home/sviolet");
                for (ChannelSftp.LsEntry obj : lsVector){
                    System.out.println(obj.getLongname());
                }

                //put
//                channel.put(".\\LICENSE.txt", "/home/sviolet/", ChannelSftp.OVERWRITE);

                //get
//                channel.get("/home/sviolet/LICENSE.txt", ".\\out\\", null, ChannelSftp.OVERWRITE);

                //return value
                return "succeed";
            }

            @Override
            public String onConnectFailed(Throwable t) {
                t.printStackTrace();
                return "connect failed";
            }

            @Override
            public String onException(Throwable t) {
                t.printStackTrace();
                return "exception";
            }
        });

        System.out.println("result:" + result);

    }

}
