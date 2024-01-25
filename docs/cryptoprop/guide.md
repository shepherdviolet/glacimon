# CryptoProp | Spring属性加解密

* [Source Code](https://github.com/shepherdviolet/glacimon/tree/master/glacispring-common/src/main/java/com/github/shepherdviolet/glacimon/spring/x/crypto/cryptoprop)

```
CryptoProp是一个Spring属性(property)加解密方案. 用于保护敏感属性(例如数据库密码), 避免在工程源码中明文配置被开发人员获取, 
避免直接在启动脚本中明文配置被生产查询(query)用户获取.
```

<br>
<br>

# 使用说明

## 添加依赖

```gradle

repositories {
    mavenCentral()
}
dependencies {
    compile 'com.github.shepherdviolet.glacimon:glacispring-common:?'
}

```

```maven
    <dependency>    
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>glacispring-common</artifactId>
        <version>?</version> 
    </dependency>
```

<br>

## 产生密钥

* 使用任意工具产生AES或RSA密钥, AES推荐128位, RSA推荐1024位 (属性加密的密码强度要求通常不高, 你也可以换AES256或者RSA2048)
* 下面是使用JAVA代码生成密钥的示例:

```
// 产生AES对称密钥, 128位
System.out.println("aesKey:\n" + SimpleCryptoPropUtils.generateAesKey());
// 产生RSA非对称密钥对, 1024位; 注意, 如果用密钥文件, 必须用PEM格式, 如果直接配置密钥明文, 则使用DER格式
System.out.println(SimpleCryptoPropUtils.generateRsaKeyPair());
```

<br>

## 配置CryptoProp

### SpringBoot自动配置

* 启用CryptoProp

```
@Configuration
@EnableCryptoProp
public class MyConfiguration {

}
```

* 在application.yaml(或其他配置文件)中配置参数

```
glacispring:
  crypto-prop:
    ## 解密密钥: RSA算法, 引用外部密钥文件, 注意文件内容必须为PEM格式, 文件权限建议设置为600
    key: rsa:file:/home/username/cryptoprop-private-key.pem
```

```
# 密钥(glacispring.crypto-prop.key)格式说明
# 注意: 生产环境的解密密钥请勿放在工程源码中!

## [推荐] RSA算法, 引用外部密钥文件, 注意文件内容必须为PEM格式, 文件权限建议设置为600
    rsa:file:/home/username/cryptoprop-private-key.pem

## RSA算法, 引用类路径下的密钥文件, 注意文件内容必须为PEM格式
    rsa:classpath:config/demo/common/cryptoprop/cryptoprop-private-key.pem

## RSA算法, 私钥明文, 注意密钥格式必须为DER格式
    rsa:MIICdwIBADANBgkqh......2Ad/xfQ6d0WMLKE=

## [推荐] AES算法, 引用外部密钥文件, 注意文件内容尽量为一行, 文件权限建议设置为600
    aes:file:/home/username/cryptoprop-key.txt

## AES算法, 引用类路径下的密钥文件, 注意文件内容尽量为一行
    aes:classpath:/home/username/cryptoprop-key.txt

## AES算法, 密钥明文 (生产环境的解密密钥请勿放在工程源码中!)
    aes:KrIjtliPM3MIlHPh+l3ylA==
```

```
# 其他参数说明

glacispring:
  crypto-prop:
    ## NORMAL:普通模式(默认); ENHANCED:增强模式(适用范围增大, 侵入点更多, 可能有兼容新问题)
#    mode: ENHANCED
    ## (仅限应急) true:当CryptoProp初始化失败时不抛出异常, 但属性解密功能也会失效; false:默认, 抛出异常启动失败
#    ignore-exception: true
    ## 增强模式相关配置: skip-property-sources指定一些PropertySource不被侵入; intercept-by-proxy侵入模式从包装方式改为代理方式
#    enhanced:
#      skip-property-sources: com.packagename.SomePropertySource1,com.packagename.SomePropertySource2
#      intercept-by-proxy: true
```

<br>

### Spring XML手动配置

* 在XML中配置CryptoProp

```
    <!-- CryptoProp Spring属性解密 -->
    <!-- 注意, 这里必须用#{environment.getProperty('propertyname')}获取参数, 不能直接用${propertyname}, 因为BeanDefinitionRegistryPostProcessor在Spring启动初期执行 -->
    <bean id="glacispring.cryptoProp.decryptor" class="com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.decryptor.SimpleCryptoPropDecryptor">
        <constructor-arg index="0" value="#{environment.getProperty('glacispring.crypto-prop.key')}"/>
    </bean>
    <bean id="glacispring.cryptoProp.enhancedModePropertySourceConverter" class="com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.enhanced.DefaultCryptoPropertySourceConverter">
        <constructor-arg index="0" ref="glacispring.cryptoProp.decryptor"/>
        <constructor-arg index="1" value="#{'true'.equals(environment.getProperty('glacispring.crypto-prop.enhanced.intercept-by-proxy'))}"/>
        <constructor-arg index="2" value="#{environment.getProperty('glacispring.crypto-prop.enhanced.skip-property-sources')}"/>
    </bean>
    <bean id="glacispring.cryptoProp.beanDefinitionRegistryPostProcessor" class="com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.CryptoPropBeanDefinitionRegistryPostProcessor">
        <constructor-arg index="0" ref="glacispring.cryptoProp.decryptor"/>
        <constructor-arg index="1" ref="glacispring.cryptoProp.enhancedModePropertySourceConverter"/>
        <constructor-arg index="2" value="#{environment.getProperty('glacispring.crypto-prop.mode', 'NORMAL')}"/>
        <constructor-arg index="3" value="#{'true'.equals(environment.getProperty('glacispring.crypto-prop.ignore-exception'))}"/>
    </bean>
```

* 在application.properties(或其他配置文件)中配置参数

```
## 解密密钥: RSA算法, 引用外部密钥文件, 注意文件内容必须为PEM格式, 文件权限建议设置为600
glacispring.crypto-prop.key=rsa:file:/home/username/cryptoprop-private-key.pem 
```

```
# 密钥(glacispring.crypto-prop.key)格式说明
# 注意: 生产环境的解密密钥请勿放在工程源码中!

## [推荐] RSA算法, 引用外部密钥文件, 注意文件内容必须为PEM格式, 文件权限建议设置为600
    rsa:file:/home/username/cryptoprop-private-key.pem

## RSA算法, 引用类路径下的密钥文件, 注意文件内容必须为PEM格式
    rsa:classpath:config/demo/common/cryptoprop/cryptoprop-private-key.pem

## RSA算法, 私钥明文, 注意密钥格式必须为DER格式
    rsa:MIICdwIBADANBgkqh......2Ad/xfQ6d0WMLKE=

## [推荐] AES算法, 引用外部密钥文件, 注意文件内容尽量为一行, 文件权限建议设置为600
    aes:file:/home/username/cryptoprop-key.txt

## AES算法, 引用类路径下的密钥文件, 注意文件内容尽量为一行
    aes:classpath:/home/username/cryptoprop-key.txt

## AES算法, 密钥明文 (生产环境的解密密钥请勿放在工程源码中!)
    aes:KrIjtliPM3MIlHPh+l3ylA==
```

```
# 其他参数说明
## NORMAL:普通模式(默认); ENHANCED:增强模式(适用范围增大, 侵入点更多, 可能有兼容新问题)
#glacispring.crypto-prop.mode=ENHANCED
## (仅限应急) true:当CryptoProp初始化失败时不抛出异常, 但属性解密功能也会失效; false:默认, 抛出异常启动失败
#glacispring.crypto-prop.ignore-exception=true
## 增强模式相关配置: skip-property-sources指定一些PropertySource不被侵入; intercept-by-proxy侵入模式从包装方式改为代理方式
#glacispring.crypto-prop.enhanced.skip-property-sources=com.packagename.SomePropertySource1,com.packagename.SomePropertySource2
#glacispring.crypto-prop.enhanced.intercept-by-proxy=true
```

<br>

## 用工具类加密属性

```
import com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.decryptor.SimpleCryptoPropUtils;

/**
 * CryptoProp属性加密工具类
 * 如果用RSA算法, 公钥可以放在工程源码中
 * 如果用AES算法, 密钥必须专人保管, 防止泄露
 */
public class CryptoPropertyUtils {
    public static void main(String[] args) {

        String plain = "message";

        //非对称加密
        System.out.println(SimpleCryptoPropUtils.encryptAndWrap(plain,
                "rsa:classpath:config/demo/common/cryptoprop/cryptoprop-public-key.pem"));

        // 对称加密
        System.out.println(SimpleCryptoPropUtils.encryptAndWrap(plain,
                "aes:classpath:config/demo/common/cryptoprop/cryptoprop-key.txt"));

    }
}
```

> 密文示例: CIPHER(bhkSKmy/80y8kW91XRFoVQesS/UpT6Mq1zWxcuMUGNQ=)

<br>

## 在properties/yaml/启动参数中使用密文

```
# properties
bar.foo=CIPHER(bhkSKmy/80y8kW91XRFoVQesS/UpT6Mq1zWxcuMUGNQ=)
```

```
# yaml
bar:
  foo: CIPHER(bhkSKmy/80y8kW91XRFoVQesS/UpT6Mq1zWxcuMUGNQ=)
```

```
# 启动参数
-Dbar.foo=CIPHER(bhkSKmy/80y8kW91XRFoVQesS/UpT6Mq1zWxcuMUGNQ=)
```

<br>
<br>

## 最佳实践

### 生产环境: 启用加密

* 生成一个`生产专用`密钥, 由运维人员专人管理, 防止泄露
* 在`生产`服务器中放置密钥文件`/home/username/key.pem`, 并设置权限600, 禁止其他用户访问
* 在工程源码的`生产环境`配置`application-pro.yaml`或`application-pro.properties`中配置密钥路径`glacispring.crypto-prop.key=rsa:file:/home/username/key.pem`
* 对工程源码中的`生产环境`属性加密

```
# 以数据库密码为例, 配置application-pro.yaml
spring:
  datasource:
    password: CIPHER(bhkSKmy/80y8kW91XRFoVQesS/UpT6Mq1zWxcuMUGNQ=)
```

```
# 以数据库密码为例, 配置application-pro.properties
spring.datasource.password=CIPHER(bhkSKmy/80y8kW91XRFoVQesS/UpT6Mq1zWxcuMUGNQ=)
```

### UAT测试环境: 启用加密

* 生成一个`测试专用`密钥, 按照生产环境的方式配置, 提高生产环境与测试环境的一致性
* 在`UAT环境`服务器中放置密钥文件`/home/username/key.pem`, 并设置权限600, 禁止其他用户访问
* 在工程源码的`UAT环境`配置`application-uat.yaml`或`application-uat.properties`中配置密钥路径`glacispring.crypto-prop.key=rsa:file:/home/username/key.pem`
* 对工程源码中的`UAT环境`属性加密

```
# 以数据库密码为例, 配置application-uat.yaml
spring:
  datasource:
    password: CIPHER(bhkSKmy/80y8kW91XRFoVQesS/UpT6Mq1zWxcuMUGNQ=)
```

```
# 以数据库密码为例, 配置application-uat.properties
spring.datasource.password=CIPHER(bhkSKmy/80y8kW91XRFoVQesS/UpT6Mq1zWxcuMUGNQ=)
```

### 开发环境(SIT测试环境): 不加密

> 为了便于开发, 建议开发环境和SIT环境不加密

* 在工程源码的`开发环境`配置`application-dev.yaml`或`application-dev.properties`中配置密钥为空`glacispring.crypto-prop.key=null` (或者不配置这个参数)
* 工程源码中的`开法环境`属性不加密, 用明文

```
# 以数据库密码为例, 配置application-dev.yaml
spring:
  datasource:
    password: 123456
```

```
# 以数据库密码为例, 配置application-dev.properties
spring.datasource.password=123456
```

<br>
<br>

## 关于模式 (为什么Environment#getProperty返回的是密文?)

> CryptoProp有两种模式, 普通模式(NORMAL)下, 手动从Environment#getProperty获取参数是不支持解密的, 它会返回密文.
> 建议使用普通模式, Spring应用尽量避免用Environment#getProperty直接获取属性, 建议通过占位符(placeholder, ${...})的方式注入属性.
> 如果必须要支持Environment#getProperty解密, 可以将模式修改为加强模式, 加强模式侵入点比较多, 有可能会有兼容性问题. 

| 模式               | @Value Placeholder | XML Placeholder | @ConfigurationProperties Binding | Environment#getProperty | 说明 |
|------------------| ------------------ | --------------- | -------------------------------- | ----------------------- | ---- |
| 普通模式(NORMAL) `默认` | 支持解密 | 支持解密 | 支持解密 | `不支持解密` | 只侵入`PropertySourcesPlaceholderConfigurer`, 侵入点少, 兼容性好 |
| 增强模式(ENHANCED)   | 支持解密 | 支持解密 | 支持解密 | 支持解密 | 同时侵入`Environment的PropertySources`, 侵入点较多, 可能会有兼容性问题 |

* @Value Placeholder 支持解密

```
@Value("${bar.foo}")
private String foo;

@Value("${bar.foo}")
public void setFoo(String foo) 
    ......
}
```

* XML Placeholder 支持解密

```
<bean id="foo" class="bar.FooService">
    <property name="foo" value="${bar.foo}"/>
</bean>
```

* @ConfigurationProperties Binding 属性绑定支持解密

```
@Component
@ConfigurationProperties(prefix="test.bar")
public class FooProperties {

    private String foo;

    public void setFoo(String foo) {
        this.foo = foo;
    }
    
    public String getFoo() {
        return foo;
    }
    
}
```

* Environment#getProperty 需要开启`增强模式`才支持解密

```
@Autowired
private Environment env;

public void test() {
    // 从environment手动获取属性, 必须开启增强模式
    String value1 = env.getProperty("bar.foo1");
    String value2 = env.resolvePlaceholders("${bar.foo2}")
}
```

