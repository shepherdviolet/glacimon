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

package sviolet.slate.common.x.net.loadbalance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sviolet.slate.common.x.net.loadbalance.classic.*;
import sviolet.slate.common.x.net.loadbalance.inspector.TelnetLoadBalanceInspector;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 支持均衡负载的OkHttpClient测试案例
 */
public class MultiHostOkHttpClientTest {

    private static Logger logger = LoggerFactory.getLogger(MultiHostOkHttpClientTest.class);

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws NoHostException, RequestBuildException, HttpRejectException, IOException {

        LoadBalancedHostManager hostManager = new LoadBalancedHostManager()
                .setHostArray(new String[]{
                        "http://127.0.0.1:8080",
                        "http://127.0.0.1:8081"
                });

        LoadBalancedInspectManager inspectManager = new LoadBalancedInspectManager()
                .setHostManager(hostManager)
                .setInspectInterval(5000L)
                .setInspector(new TelnetLoadBalanceInspector())
                .setVerboseLog(true);

        final MultiHostOkHttpClient client = new MultiHostOkHttpClient()
                .setHostManager(hostManager)
                .setPassiveBlockDuration(3000L)
                .setConnectTimeout(3000L)
                .setWriteTimeout(10000L)
                .setReadTimeout(10000L)
                .setDataConverter(new GsonDataConverter())
                .setVerboseLogConfig(MultiHostOkHttpClient.VERBOSE_LOG_CONFIG_ALL)
                .setVerboseLog(true);

        // sync

        byte[] response = client.get("/basic/get/json")
                .urlParam("name", "wang wang")
                .urlParam("key", "321")
                .sendForBytes();

        System.out.println(new String(response));

        try (InputStream inputStream = client.get("/basic/get/wildcard")
                .sendForInputStream()) {
            int len;
            byte[] buff = new byte[1024];
            StringBuilder stringBuilder = new StringBuilder();
            while ((len = inputStream.read(buff)) >= 0) {
                stringBuilder.append(new String(buff, 0, len));
            }
            System.out.println(stringBuilder.toString());
        }

        try (MultiHostOkHttpClient.ResponsePackage responsePackage = client.get("/")
                .send()) {
            System.out.println(responsePackage.body().string());
        }

        response = client.post("/basic/post/json")
                .urlParam("traceId", "000000001")
                .body("hello json 1".getBytes())
                .sendForBytes();

        System.out.println(new String(response));

        try (InputStream inputStream = client.post("/basic/post/json")
                .urlParam("traceId", "000000001")
                .body("hello json 2".getBytes())
                .sendForInputStream()) {
            int len;
            byte[] buff = new byte[1024];
            StringBuilder stringBuilder = new StringBuilder();
            while ((len = inputStream.read(buff)) >= 0) {
                stringBuilder.append(new String(buff, 0, len));
            }
            System.out.println(stringBuilder.toString());
        }

        try (MultiHostOkHttpClient.ResponsePackage responsePackage = client.post("/basic/post/json")
                .urlParam("traceId", "000000001")
                .body("hello json 3".getBytes())
                .send()) {
            System.out.println(responsePackage.body().string());
        }

        // form

        Map<String, Object> form = new HashMap<>(8);
        form.put("name", "旺旺");
        form.put("key", "741");

        Map<String, Object> responseMap = client.post("/basic/post/json")
                .formBody(form)
                .sendForBean(Map.class);

        System.out.println(responseMap);

        form = new HashMap<>(8);
        form.put("name", "sheng ma");
        form.put("key", "852");

        client.post("/basic/post/json")
                .formBody(form)
                .enqueue(new MultiHostOkHttpClient.BeanCallback<Map>() {
                    @Override
                    public void onSucceed(Map bean) throws Exception {
                        System.out.println(bean);
                    }
                    @Override
                    protected void onErrorBeforeSend(Exception e) {
                        e.printStackTrace();
                    }
                    @Override
                    protected void onErrorAfterSend(Exception e) {
                        e.printStackTrace();
                    }
                });

        // bean

        form = new HashMap<>(8);
        form.put("name", "miaomiao");
        form.put("key", "963");

        responseMap = client.post("/basic/post/json")
                .beanBody(form)
                .sendForBean(Map.class);

        System.out.println(responseMap);

        form = new HashMap<>(8);
        form.put("name", "+++---");
        form.put("key", "951");

        client.post("/basic/post/json")
                .beanBody(form)
                .enqueue(new MultiHostOkHttpClient.BeanCallback<Map<String, Object>>() {
                    @Override
                    public void onSucceed(Map<String, Object> bean) throws Exception {
                        System.out.println(bean);
                    }
                    @Override
                    protected void onErrorBeforeSend(Exception e) {
                        e.printStackTrace();
                    }
                    @Override
                    protected void onErrorAfterSend(Exception e) {
                        e.printStackTrace();
                    }
                });

        // async

        client.get("/basic/get/json")
                .urlParam("name", "wang wang")
                .urlParam("key", "321")
                .enqueue(new MultiHostOkHttpClient.BytesCallback() {
                    @Override
                    public void onSucceed(byte[] body) {
                        System.out.println(new String(body));
                    }
                    @Override
                    protected void onErrorBeforeSend(Exception e) {
                        e.printStackTrace();
                    }
                    @Override
                    protected void onErrorAfterSend(Exception e) {
                        e.printStackTrace();
                    }
                });

        client.get("/basic/get/json")
                .urlParam("name", "wang wang")
                .urlParam("key", "654")
                .enqueue(new MultiHostOkHttpClient.InputStreamCallback() {
                    @Override
                    public void onSucceed(InputStream inputStream) throws Exception {
                        int len;
                        byte[] buff = new byte[1024];
                        StringBuilder stringBuilder = new StringBuilder();
                        while ((len = inputStream.read(buff)) >= 0) {
                            stringBuilder.append(new String(buff, 0, len));
                        }
                        System.out.println(stringBuilder.toString());
                    }
                    @Override
                    protected void onErrorBeforeSend(Exception e) {
                        e.printStackTrace();
                    }
                    @Override
                    protected void onErrorAfterSend(Exception e) {
                        e.printStackTrace();
                    }
                });

        client.get("/basic/get/json")
                .urlParam("name", "wang wang")
                .urlParam("key", "987")
                .enqueue(new MultiHostOkHttpClient.ResponsePackageCallback() {
                    @Override
                    public void onSucceed(MultiHostOkHttpClient.ResponsePackage responsePackage) throws Exception {
                        System.out.println(responsePackage.body().string());
                    }
                    @Override
                    protected void onErrorBeforeSend(Exception e) {
                        e.printStackTrace();
                    }
                    @Override
                    protected void onErrorAfterSend(Exception e) {
                        e.printStackTrace();
                    }
                });

        client.post("/basic/post/json")
                .urlParam("traceId", "000000001")
                .body("hello json 4".getBytes())
                .enqueue(new MultiHostOkHttpClient.BytesCallback() {
                    @Override
                    public void onSucceed(byte[] body) {
                        System.out.println(new String(body));
                    }
                    @Override
                    protected void onErrorBeforeSend(Exception e) {
                        e.printStackTrace();
                    }
                    @Override
                    protected void onErrorAfterSend(Exception e) {
                        e.printStackTrace();
                    }
                });

        client.post("/basic/post/json")
                .urlParam("traceId", "000000001")
                .body("hello json 5".getBytes())
                .enqueue(new MultiHostOkHttpClient.InputStreamCallback() {
                    @Override
                    public void onSucceed(InputStream inputStream) throws Exception {
                        int len;
                        byte[] buff = new byte[1024];
                        StringBuilder stringBuilder = new StringBuilder();
                        while ((len = inputStream.read(buff)) >= 0) {
                            stringBuilder.append(new String(buff, 0, len));
                        }
                        System.out.println(stringBuilder.toString());
                    }
                    @Override
                    protected void onErrorBeforeSend(Exception e) {
                        e.printStackTrace();
                    }
                    @Override
                    protected void onErrorAfterSend(Exception e) {
                        e.printStackTrace();
                    }
                });

        client.post("/basic/post/json")
                .urlParam("traceId", "000000001")
                .body("hello json 6".getBytes())
                .enqueue(new MultiHostOkHttpClient.ResponsePackageCallback() {
                    @Override
                    public void onSucceed(MultiHostOkHttpClient.ResponsePackage responsePackage) throws Exception {
                        System.out.println(responsePackage.body().string());
                    }
                    @Override
                    protected void onErrorBeforeSend(Exception e) {
                        e.printStackTrace();
                    }
                    @Override
                    protected void onErrorAfterSend(Exception e) {
                        e.printStackTrace();
                    }
                });

//        System.exit(0);

    }

}
