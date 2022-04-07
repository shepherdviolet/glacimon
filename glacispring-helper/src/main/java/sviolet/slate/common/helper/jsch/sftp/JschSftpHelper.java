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
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * <p>JSch SFTP 助手</p>
 *
 * <pre>{@code
 *
 *  //实例化JschHelper(可单例)
 *  JschHelper jsch = new JschHelper();
 *
 *  //实例化JschSftpHelper(可单例)
 *  JschSftpHelper helper = new JschSftpHelper();
 *  helper.setJsch(jsch);
 *  helper.setHost("192.168.163.136");
 *  helper.setPort(17799);
 *  helper.setUsername("root");
 *  helper.setPassword("root");
 *  helper.setTimeout(5000);
 *
 *  //创建连接, 执行一系列操作, 关闭连接
 *  String result = helper.command(new JschSftpCommand<String>() {
 *      public String onCommand(ChannelSftp channel) throws SftpException {
 *          //cd
 *          channel.cd("/home/sviolet");
 *          //ls
 *          Vector<ChannelSftp.LsEntry> lsVector = channel.ls("/home/sviolet");
 *          for (ChannelSftp.LsEntry obj : lsVector){
 *              System.out.println(obj.getLongname());
 *          }
 *          //put
 *          channel.put(".\\LICENSE.txt", "/home/sviolet/", ChannelSftp.OVERWRITE);
 *          //get
 *          channel.get("/home/sviolet/LICENSE.txt", ".\\out\\", null, ChannelSftp.OVERWRITE);
 *          //return value
 *          return "succeed";
 *      }
 *      public String onConnectFailed(Throwable t) {
 *          t.printStackTrace();
 *          return "connect failed";
 *      }
 *      public String onException(Throwable t) {
 *          t.printStackTrace();
 *          return "exception";
 *      }
 *  });
 *
 *  System.out.println("result:" + result);
 *
 *
 * }</pre>
 *
 * @author S.Violet
 */
public class JschSftpHelper {

    private static final String CHANNEL_TYPE = "sftp";

    private JSch jsch;

    private String host;
    private int port = 22;
    private String username;
    private String password;
    private int timeout;

    /**
     * 创建连接, 执行一系列操作, 关闭连接
     * @param command SFTP操作
     * @param <T> 返回类型
     * @return 自定义返回值
     */
    public <T> T command(JschSftpCommand<T> command) {
        JSch jsch = getAndCheckJsch();
        Session session = null;
        try {
            session = buildSession(jsch, username, password, host, port, timeout);
            ChannelSftp channel = (ChannelSftp)session.openChannel(CHANNEL_TYPE);
            channel.connect();
            try {
                return command.onCommand(channel);
            } catch (Throwable t) {
                return command.onException(t);
            }
        } catch (Throwable t) {
            return command.onConnectFailed(t);
        } finally {
            if (session != null) {
                try {
                    session.disconnect();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    /**
     * 可复写该方法修改Session配置
     */
    protected Session buildSession(JSch jsch, String username, String password, String host, int port, int timeout) throws JSchException {
        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);
        session.setTimeout(timeout);
        session.connect();
        return session;
    }

    private JSch getAndCheckJsch(){
        if (jsch == null) {
            throw new NullPointerException("JSch is null, you must setJsch() before use");
        }
        return jsch;
    }

    public JSch getJsch() {
        return jsch;
    }

    public void setJsch(JSch jsch) {
        this.jsch = jsch;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
