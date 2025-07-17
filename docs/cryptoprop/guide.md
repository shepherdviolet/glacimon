# CryptoProp | Spring属性加解密

* [Source Code](https://github.com/shepherdviolet/glacimon/tree/master/glacispring-common/src/main/java/com/github/shepherdviolet/glacimon/spring/x/crypto/cryptoprop)

```
CryptoProp是一个Spring属性(property)加解密方案. 用于保护敏感属性(例如数据库密码), 避免属性明文配置在工程源码中被开发人员获取.
启用CryptoProp并配置密钥后, 你可以将敏感属性加密后配置到属性文件(properties/yaml)中, 应用启动后会自动将属性解密并注入Bean中.
CryptoProp支持在'@Value'和'XML'中使用占位符(${...})获取解密的属性, 支持使用'@ConfigurationProperties'绑定属性的Bean获得
解密的属性, 支持Environment#getProperty手动获取解密的属性.

```

* 要求: JDK 8+
* 要求: apollo-client 1.4.0+

```
CryptoProp支持Apollo配置中心, 支持实时修改属性(Apollo上发布后无需重启应用), 请使用apollo-client 1.4.0及以上版本! 
老版本实时修改属性会出现异常! (注意, 对Apollo实时修改的支持仅限于业务属性, CryptoProp本身的配置参数不支持实时修改)
```

<br>
<br>

# 配置及使用说明

## 添加依赖

```gradle

repositories {
    mavenCentral()
}
dependencies {
    implementation 'com.github.shepherdviolet.glacimon:glacispring-common:2024.1.5'
}

```

```maven
    <dependency>    
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>glacispring-common</artifactId>
        <version>2024.1.5</version> 
    </dependency>
```

<br>

## 产生密钥

* 使用任意工具产生AES或RSA密钥, AES推荐128位, RSA推荐1024位 (属性加密的密码强度要求通常不高, 你也可以换AES256或者RSA2048)
* 下面是使用JAVA代码生成密钥的示例:

```
// 产生AES对称密钥, 128位; 可以记录密钥的sha256值, 用于比对启动日志判断密钥设置是否正确
String aesKey = SimpleCryptoPropUtils.generateAesKey();
System.out.println("[aesKey]\n" + aesKey);
System.out.println("[aesKey sha256]\n" + SimpleCryptoPropUtils.sha256(aesKey));

// 产生RSA非对称密钥对, 1024位; 注意, 如果用密钥文件, 必须用PEM格式, 如果直接配置密钥明文, 则使用DER格式; 可以记录密钥的sha256值, 用于比对启动日志判断密钥设置是否正确
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

> 注意, CryptoProp本身的配置参数(glacispring.crypto-prop.*)不支持Apollo实时修改, 修改后需要重启应用, 对Apollo实时修改的支持仅限于业务属性

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
# 其他可选参数 (一般不需要配置)

glacispring:
  crypto-prop:
    ## 指定一些PropertySource不被侵入
#    skip-property-sources: com.packagename.SomePropertySource1,com.packagename.SomePropertySource2
    ## intercept-by-proxy侵入模式从包装方式改为代理方式
#    intercept-by-proxy: true
```

<br>

### Spring XML手动配置

* 在XML中配置CryptoProp

```
    <!-- 方式一: 简易配置 (2024.1.4+) -->
    <!-- 注意, 这里不能用占位符${propertyname}注入属性, 因为BeanFactoryPostProcessor在Spring启动初期执行 -->
    <bean id="glacispring.cryptoProp.beanFactoryPostProcessor" class="com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.CryptoPropBeanFactoryPostProcessor"/>
```

```
    <!-- 方式二: 高级配置 (完整配置, 可以自定义实现) -->
    <!-- 注意, 这里不能用占位符${propertyname}注入属性, 因为BeanFactoryPostProcessor在Spring启动初期执行 -->
    <bean id="glacispring.cryptoProp.decryptor" class="com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.decryptor.SimpleCryptoPropDecryptor"/>
    <bean id="glacispring.cryptoProp.cryptoPropertySourceConverter" class="com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.propertysource.DefaultCryptoPropertySourceConverter">
        <constructor-arg index="0" ref="glacispring.cryptoProp.decryptor"/>
    </bean>
    <bean id="glacispring.cryptoProp.beanFactoryPostProcessor" class="com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.CryptoPropBeanFactoryPostProcessor">
        <constructor-arg index="0" ref="glacispring.cryptoProp.cryptoPropertySourceConverter"/>
    </bean>
```

* 在application.properties(或其他配置文件)中配置参数

> 注意, CryptoProp本身的配置参数(glacispring.crypto-prop.*)不支持Apollo实时修改, 修改后需要重启应用, 对Apollo实时修改的支持仅限于业务属性

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
# 其他可选参数 (一般不需要配置)

## skip-property-sources指定一些PropertySource不被侵入; intercept-by-proxy侵入模式从包装方式改为代理方式
#glacispring.crypto-prop.skip-property-sources=com.packagename.SomePropertySource1,com.packagename.SomePropertySource2
#glacispring.crypto-prop.intercept-by-proxy=true
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

> CryptoProp支持实时修改属性(Apollo上发布后无需重启应用), 请使用apollo-client 1.4.0及以上版本!
> 老版本实时修改属性会出现异常! (注意, 对Apollo实时修改的支持仅限于业务属性, CryptoProp本身的配置参数不支持实时修改)

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

# 最佳实践

## 生产环境: 启用加密

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

## UAT测试环境: 启用加密

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

## 开发环境(SIT测试环境): 不加密

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

# 其他

## 兼容性问题

* apollo-client 1.3.0及以下版本不支持实时属性修改 (请使用1.4.0及以上版本)

```
apollo-client 1.3.0及以下版本中, AutoUpdateConfigChangeListener类中, 存在shouldTriggerAutoUpdate方法, 
它会判断ConfigChangeEvent中的新属性值和environment#getProperty返回值是否相等, 相等才会更新属性.
因为ConfigChangeEvent中的新属性值是密文, 而environment#getProperty返回值是明文, 两个结果不相等, 
所以属性实时更新被跳过. 更新apollo-client 1.4.0及以上版本解决.
P.S.低版本用也能用, 只是不支持配置实时更新.
```

## 局限性

* 未监听Environment中PropertySources的增删事件, 对于后续增加的PropertySource不会进行处理

```
某些组件会在CryptoPropBeanFactoryPostProcessor处理完成后, 往Environment添加PropertySource.
CryptoProp未监听Environment中PropertySources的增删事件, 对于后续增加的PropertySource不会进行代理. 

如果需要优化, 可以参考jasypt-spring-boot的RefreshScopeRefreshedEventListener, 在捕获到事件时重新调用
CryptoPropBeanFactoryPostProcessor#convertPropertySources处理一遍即可.
```

## 支持哪些属性配置方式?

* 启动参数

```
-Dspring.datasource.password=CIPHER(bhkSKmy/80y8kW91XRFoVQesS/UpT6Mq1zWxcuMUGNQ=)
```

* SpringBoot默认的yaml和properties文件

```
application.yaml
application-<env>.yaml
application.properties
application-<env>.properties
......
```

* 用注解加载的yaml和properties文件

```
@PropertySource({
        "classpath:properties/general.properties",
})
@PropertySource(factory = OptionalYamlPropertySourceFactory.class, value = {
        "classpath:properties/yaml.yml",
})
```

* Apollo配置中心

```
注意, 请使用apollo-client 1.4.0及以上版本! 老版本实时修改属性会出现异常!
```

* XML加载的properties文件

```
<context:property-placeholder location="classpath*:properties/byxml.properties" />
```

## 支持哪些属性获取方式?

* `@Value`中的`${...}`占位符 支持自动解密

```
@Value("${bar.foo}")
private String foo;

@Value("${bar.foo}")
public void setFoo(String foo) 
    ......
}
```

* `@Value`中的`${...}`占位符 支持自动解密

```
<bean id="foo" class="bar.FooService">
    <property name="foo" value="${bar.foo}"/>
</bean>
```

* 用@ConfigurationProperties绑定属性的Bean 支持自动解密

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

* 调用Environment#getProperty获取属性

```
@Autowired
private Environment env;

public void test() {
    // 从environment手动获取属性
    String value1 = env.getProperty("bar.foo1");
    String value2 = env.resolvePlaceholders("${bar.foo2}")
}
```
