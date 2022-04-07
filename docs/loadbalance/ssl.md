# HttpClient SSL相关配置

# 推荐: 快速配置

## 访问自签名的服务端 (根证书非官方)

* 如果我们要访问一个服务器, 它的证书不是正规CA签发的, 我们可以向对方获取根证书, 然后用如下方法添加信任的根证书

### Spring Boot YAML

```text
slate:
  httpclients:
    client1:
      # 添加自定义的根证书, 用于验证自签名的服务器(设置一个, 优先级高). 如果设置为"UNSAFE-TRUST-ALL-ISSUERS"则不校验服务端证书链, 信任一切服务端证书, 不安全!!!
      custom-server-issuer-encoded: '自签名的服务端根证书X509-Base64字符串'
      # 添加自定义的根证书, 用于验证自签名的服务器(设置多个, 优先级低). 在properties中: slate.httpclients.custom-server-issuers-encoded[0]=...
      custom-server-issuers-encoded: 
        - '自签名的服务端根证书X509-Base64字符串(1)'
        - '自签名的服务端根证书X509-Base64字符串(2)'
```

### 其他

* 调用如下方法设置根证书, 或在Spring XML中设置如下参数

```text
//四选一
SslUtils.setCustomServerIssuers(simpleOkHttpClient, ...)
SslUtils.setCustomServerIssuer(simpleOkHttpClient, ...)
SslUtils.setCustomServerIssuersEncoded(simpleOkHttpClient, ...)
SslUtils.setCustomServerIssuerEncoded(simpleOkHttpClient, ...)
```

## 强制指定证书域名(CN)/DN

* 如果我们要求服务端证书必须是指定的域名(CN)或DN, 即不信任其他域名(CN)/DN的证书, 可以通过如下方法指定域名(CN)/DN
* 如果我们通过代理访问一个服务器(例如Nginx的Stream代理), 我们访问的地址和证书的CN不符, 会报错, 可以通过如下方法指定域名(CN)/DN

### Spring Boot YAML

```text
slate:
  httpclients:
    client1:
      # 使用指定的域名验证服务端证书的CN(方式二, 优先级低). 如果设置为"UNSAFE-TRUST-ALL-CN"则不校验CN, 所有合法证书都通过, 不安全!!!
      verify-server-cn-by-custom-hostname: 'www.baidu.com'
      # 使用指定的域名验证服务端证书的DN(方式一, 优先级高). 如果设置为"UNSAFE-TRUST-ALL-DN"则不校验DN, 所有合法证书都通过, 不安全!!!
      verify-server-dn-by-customdn: 'CN=baidu.com,O=Beijing Baidu Netcom Science Technology Co.\, Ltd,OU=service operation department,L=beijing,ST=beijing,C=CN'
```

### 其他

* 调用如下方法设置根证书, 或在Spring XML中设置如下参数

```text
SslUtils.setVerifyServerCnByCustomHostname(simpleOkHttpClient, ...)
SslUtils.setVerifyServerDnByCustomDn(simpleOkHttpClient, ...)
```

<br>
<br>

# 高级: 自定义SSL验证逻辑

## 自定义证书验证逻辑(X509TrustManager)

* 给客户端设置自定义的X509TrustManager

```text
SslUtils.setX509TrustManager(simpleOkHttpClient, new X509TrustManager() {
    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        // TO DO ...
    }
    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        // TO DO ...
    }
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        // TO DO ...
    }
});
```

## 自定义SSLSocketFactory和X509TrustManager

* 给客户端设置自定义的SSLSocketFactory和X509TrustManager

```text
// 两个要同时设置
simpleOkHttpClient.setSSLSocketFactory(sslSocketFactory);
simpleOkHttpClient.setX509TrustManager(x509TrustManager);
```

## 自定义域名验证逻辑

* 示例1

```text
simpleOkHttpClient.setHostnameVerifier(new SimpleHostnameVerifier() {
    @Override
    protected boolean isHostnameMatch(String hostname, String cn) {
        //证书的域名必须是指定值
        return super.isHostnameMatch("www.baidu.com", cn);
    }
});
```

* 示例2

```text
simpleOkHttpClient.setHostnameVerifier(new HostnameVerifier() {
    public boolean verify(String hostname, SSLSession session) {
        try {
            //服务端证书链
            Certificate[] certificates = session.getPeerCertificates();
            if (certificates == null || certificates.length <= 0) {
                return false;
            }
            //第一个证书是站点证书
            X509Certificate x509Certificate = (X509Certificate) certificates[0];
            String dn = x509Certificate.getSubjectX500Principal().getName();
            //证书的DN必须是指定值
            return "你指定的DN".equals(dn);
        } catch (Throwable ignored) {
        }
        return false;
    }
});
```

# 高级: 如何在Spring中设置自定义逻辑

## SpringBoot YML自动配置的客户端

```text
@Configuration
public class HttpClientConfiguration {

    @Autowired
    public void configureHttpClients(HttpClients httpClients){
        SimpleOkHttpClient client1 = httpClients.get("client1");
        //给客户端设置自定义的X509TrustManager
        SslUtils.setX509TrustManager(client1, x509TrustManager);
        //给客户端设置自定义的SSLSocketFactory和X509TrustManager, 两个要同时设置
        //client1.setSSLSocketFactory(sslSocketFactory);
        //client1.setX509TrustManager(x509TrustManager);
        //自定义域名验证逻辑
        client1.setHostnameVerifier(hostnameVerifier);
    }

}
```

## Spring 注解手动配置的客户端

```text
    @Autowired
    @Qualifier("simpleOkHttpClient")
    public void configureHttpClients(SimpleOkHttpClient simpleOkHttpClient){
        //给客户端设置自定义的X509TrustManager
        SslUtils.setX509TrustManager(simpleOkHttpClient, x509TrustManager);
        //给客户端设置自定义的SSLSocketFactory和X509TrustManager, 两个要同时设置
        //simpleOkHttpClient.setSSLSocketFactory(sslSocketFactory);
        //simpleOkHttpClient.setX509TrustManager(x509TrustManager);
        //自定义域名验证逻辑
        simpleOkHttpClient.setHostnameVerifier(hostnameVerifier);
    }
```

## Spring XML手动配置的客户端

* 编写一个Bean, 注入客户端(SimpleOkHttpClient), 实现InitializingBean接口, 在afterPropertiesSet方法中操作客户端

```text
public class HttpClientSslConfigurer implements InitializingBean {
    
    private SimpleOkHttpClient simpleOkHttpClient;

    public void setSimpleOkHttpClient(SimpleOkHttpClient simpleOkHttpClient) {
        this.simpleOkHttpClient = simpleOkHttpClient;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //给客户端设置自定义的X509TrustManager
        SslUtils.setX509TrustManager(simpleOkHttpClient, x509TrustManager);
        //给客户端设置自定义的SSLSocketFactory和X509TrustManager, 两个要同时设置
        //simpleOkHttpClient.setSSLSocketFactory(sslSocketFactory);
        //simpleOkHttpClient.setX509TrustManager(x509TrustManager);
        //自定义域名验证逻辑
        simpleOkHttpClient.setHostnameVerifier(hostnameVerifier);
    }

}
```
