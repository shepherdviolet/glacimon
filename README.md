# Glacimon 2024.1.3

[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/shepherdviolet/glacimon.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/shepherdviolet/glacimon/context:java)

* Glacimon [ɡleɪsɪmən]: Comprehensive Java library, JDK8+
* [Github Home](https://github.com/shepherdviolet/glacimon)
* [Search in Maven Central](https://search.maven.org/search?q=g:com.github.shepherdviolet.glacimon)
* [PGP Key](https://keyserver.ubuntu.com/pks/lookup?search=0x90998B78AABD6E96&fingerprint=on&op=index)
* [Special thanks to JetBrains for the free open source license, it is very helpful for our project!](https://www.jetbrains.com/?from=glacimon)

<br>
<br>

## Glacimon

<br>

### Module: glacimon-spi-core

> GlacimonSpi is an implementation of service provider interface feature. It can make your library expandable.

#### SPI utils

* GlacimonSpi [(English Documents)](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/index.md) [(中文文档)](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/index-cn.md)

<br>

### Module: glacimon-bom

> BOM for maven

<br>
<br>

## Glacijava (Glacimon for JavaSE)

<br>

### Module: glacijava-common

> Common utils for JavaSE

#### [Misc utils](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/misc)

* Check / Time / Closeable / Properties ... utils
* [LambdaBuilder : Create Map/Set/Object/List with Lambda Expression](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/misc/LambdaBuilder.java)
* [StreamingBuilder : Create Map/Set/Object/List in stream style](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/misc/LambdaBuilder.java)

#### [Crypto basic utils](https://github.com/shepherdviolet/glacimon/blob/master/docs/crypto/guide.md)

* RSA / ECDSA / AES / DES / SHA / MD5 / PEM / P12 ... utils

#### [Conversion utils](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/conversion)

* String / Base64 / Bytes / Hash / Primitive ... utils
* [SimpleKeyValueEncoder : Convert between simple Key-Value and String](https://github.com/shepherdviolet/glacimon/blob/master/docs/kvencoder/guide.md)

#### [Data structure](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/datastruc)

* Array / Collection ... utils
* Cache / Pool ...
* [Bitmap / Bloom filter](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/datastruc/bitmap)

#### [Concurrent utils](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/concurrent)

* Lock / Waiter / Snapshot ...
* [ThreadPoolExecutorUtils / GuavaThreadFactoryBuilder : Create thread pool](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/concurrent/ThreadPoolExecutorUtils.java)

#### [Reflect utils](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/reflect)

* [BeanInfoUtils : Get property information of Java Bean](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/reflect/BeanInfoUtils.java)
* [GenericClassUtils : Get actual types of generic class](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/reflect/GenericClassUtils.java)
* [MethodCaller : Get caller information of a method](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/reflect/MethodCaller.java)
* [ClassPrinter : Print all information for a class / object](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/reflect/ClassPrinter.java)

#### [IO utils](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/io)

#### [Network utils](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/net)

#### [Math utils](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/math)

#### [Class (asm/classloader) utils](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/clazz)

#### [Graph utils](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/graph)

* [Captcha utils](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/graph/captcha/ImageCaptchaUtils.java)

#### [Test utils](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/test)

#### [Protocols](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/protocol)

* [URL protocol installer](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/protocol/url)

#### [Helpers](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/helper)

* [LogbackHelper](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/helper/logback/LogbackHelper.java)

<br>

### Module: glacijava-misc

[![Depends](https://img.shields.io/badge/Depends-glacimon--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/glacimon)

> Uncommonly or incomplete utils for JavaSE

#### [Misc utils (uncommonly or incomplete)](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-misc/src/main/java/com/github/shepherdviolet/glacimon/java)

<br>

### Module: glacijava-crypto

[![Depends](https://img.shields.io/badge/Depends-glacijava--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/glacimon)
[![Depends](https://img.shields.io/badge/Depends-bcpkix--jdk15on-dc143c.svg?style=flat)](https://search.maven.org/search?q=g:org.bouncycastle%20a:bcpkix-jdk15on)

> More crypto features (depends on bouncy-castle)

#### [Crypto advanced utils](https://github.com/shepherdviolet/glacimon/blob/master/docs/crypto/guide.md)

* SM2 / SM3 / SM4 ... utils

<br>

### Module: glacijava-trace

[![Depends](https://img.shields.io/badge/Depends-glacijava--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/glacimon)
[![Depends](https://img.shields.io/badge/Depends-glacimon--spi--core-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/glacimon)

> Tracing utils

#### [Tracing utils]((https://github.com/shepherdviolet/glacimon/blob/master/docs/trace/guide.md))

* [Trace : Help to trace invocation across thread or process](https://github.com/shepherdviolet/glacimon/blob/master/docs/trace/guide.md)

<br>
<br>

## Glacispring (Glacimon for Java Spring)

<br>

### Module: glacispring-common

[![Depends](https://img.shields.io/badge/Depends-glacijava--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/glacimon)
[![Depends](https://img.shields.io/badge/Depends-spring--context-dc143c.svg?style=flat)](https://search.maven.org/search?q=g:org.springframework%20a:spring-context)
[![Depends](https://img.shields.io/badge/Depends-slf4j--api-dc143c.svg?style=flat)](https://search.maven.org/search?q=g:org.slf4j%20a:slf4j-api)

> Common utils for Java Spring and third-party libraries

| Springboot Auto Configurations                                                                                                                                                                                                   |
|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [GlacispringCommonAutoConfiguration](https://github.com/shepherdviolet/glacimon/tree/master/glacispring-common/src/main/java/com/github/shepherdviolet/glacimon/spring/basic/autoconfig/GlacispringCommonAutoConfiguration.java) |

#### [Spring config components](https://github.com/shepherdviolet/glacimon/tree/master/glacispring-common/src/main/java/com/github/shepherdviolet/glacimon/spring/x/config)

* [InterfaceInstantiation : Instantiate interfaces into Spring context](https://github.com/shepherdviolet/glacimon/blob/master/docs/interfaceinst/guide.md)
* [MemberProcessor : Process all fields/methods (of bean) in Spring context (To implement custom injection / method binding...)](https://github.com/shepherdviolet/glacimon/blob/master/docs/mbrproc/guide.md)

#### [Spring crypto components](https://github.com/shepherdviolet/glacimon/tree/master/glacispring-common/src/main/java/com/github/shepherdviolet/glacimon/spring/x/crypto)

* [CryptoProp : Spring property encryption (2024.1+)](https://github.com/shepherdviolet/glacimon/blob/master/docs/cryptoprop/guide.md)

#### [Spring config utils](https://github.com/shepherdviolet/glacimon/tree/master/glacispring-common/src/main/java/com/github/shepherdviolet/glacimon/spring/config)

* FactoryBean / BeanPostProcessor ...
* [YamlPropertySourceFactory : Load YAML by @PropertySource](https://github.com/shepherdviolet/glacimon/tree/master/glacispring-common/src/main/java/com/github/shepherdviolet/glacimon/spring/config/property)

#### [Conversion utils](https://github.com/shepherdviolet/glacimon/tree/master/glacispring-common/src/main/java/com/github/shepherdviolet/glacimon/spring/conversion)

* SequenceFormatter / StringPlaceHolderUtils ...

#### [Proxy utils](https://github.com/shepherdviolet/glacimon/tree/master/glacispring-common/src/main/java/com/github/shepherdviolet/glacimon/spring/proxy)

* AOP / CGLib ...

#### [Misc utils](https://github.com/shepherdviolet/glacimon/tree/master/glacispring-common/src/main/java/com/github/shepherdviolet/glacimon/spring/misc)

#### [Helpers](https://github.com/shepherdviolet/glacimon/tree/master/glacispring-common/src/main/java/com/github/shepherdviolet/glacimon/spring/helper)

* [DynamicDataSource : Dynamic datasource for Spring Boot](https://github.com/shepherdviolet/glacimon/tree/master/glacispring-common/src/main/java/com/github/shepherdviolet/glacimon/spring/helper/data/datasource/DynamicDataSource.java)

<br>

### Module: glacispring-misc

[![Depends](https://img.shields.io/badge/Depends-glacispring--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/glacimon)

> Uncommonly or incomplete utils for Java Spring and third-party libraries

#### [Misc utils (uncommonly or incomplete)](https://github.com/shepherdviolet/glacimon/tree/master/glacispring-misc/src/main/java/com/github/shepherdviolet/glacimon/spring)

<br>

### Module: glacispring-txtimer

[![Depends](https://img.shields.io/badge/Depends-glacispring--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/glacimon)
[![Depends](https://img.shields.io/badge/Depends-glacimon--spi--core-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/glacimon)

> Simple time-consuming statistics

#### Statistic components

* [TxTimer : Simple time-consuming statistic API](https://github.com/shepherdviolet/glacimon/blob/master/docs/txtimer/guide.md)

<br>

### Module: glacispring-httpclient

[![Depends](https://img.shields.io/badge/Depends-glacispring--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/glacimon)
[![Depends](https://img.shields.io/badge/Depends-glacispring--txtimer-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/glacimon)
[![Depends](https://img.shields.io/badge/Depends-okhttp-dc143c.svg?style=flat)](https://search.maven.org/search?q=g:com.squareup.okhttp3%20a:okhttp)

> Provides a solution for http client

#### Http client components

* [SimpleOkHttpClient (MultiHostOkHttpClient) : HTTP client supporting load-balancing / backend health-checking / circuit-breaking](https://github.com/shepherdviolet/glacimon/blob/master/docs/loadbalance/guide.md)

<br>

### Module: glacispring-mapxbean

[![Depends](https://img.shields.io/badge/Depends-glacispring--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/glacimon)
[![Depends](https://img.shields.io/badge/Depends-glacimon--spi--core-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/glacimon)

> Map-to-Bean converter

#### Conversion components

* [MapXBean : Map - Bean Converter](https://github.com/shepherdviolet/glacimon/blob/master/docs/mapxbean/guide.md)

<br>

### Module: glacispring-helper

[![Depends](https://img.shields.io/badge/Depends-glacispring--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/glacimon)
![Depends](https://img.shields.io/badge/Depends-...-dc143c.svg?style=flat)

> Helpers for third-party libraries

#### [Helpers](https://github.com/shepherdviolet/glacimon/tree/master/glacispring-helper/src/main/java/com/github/shepherdviolet/glacimon/spring/helper)

* [RocketMQ : Subscribe message by annotation](https://github.com/shepherdviolet/glacimon/blob/master/docs/rocketmq/guide.md)
* [Sentinel : Another way to config rules](https://github.com/shepherdviolet/glacimon/blob/master/docs/ezsentinel/guide.md)
* [Apollo : ApolloRefreshableProperties : The 'Properties' dynamically updated by Apollo](https://github.com/shepherdviolet/glacimon/tree/master/glacispring-helper/src/main/java/com/github/shepherdviolet/glacimon/spring/helper/apollo/ApolloRefreshableProperties.java)
* [Hessianlite : HessianLiteSerializeUtils : Serialize util with hessianlite](https://github.com/shepherdviolet/glacimon/tree/master/glacispring-helper/src/main/java/com/github/shepherdviolet/glacimon/spring/helper/hessianlite/HessianLiteSerializeUtils.java)
* [JetCache : SyncRedisLettuceCacheBuilder : Connect to redis in a synchronous manner for JetCache](https://github.com/shepherdviolet/glacimon/tree/master/glacispring-helper/src/main/java/com/github/shepherdviolet/glacimon/spring/helper/jetcache/lettuce/SyncRedisLettuceCacheBuilder.java)
* Jedis / jsch ...

<br>
<br>

## Import dependencies from maven repository

* [Search in Maven Central](https://search.maven.org/search?q=g:com.github.shepherdviolet.glacimon)

```gradle

repositories {
    //In mavenCentral
    mavenCentral()
}
dependencies {
    compile 'com.github.shepherdviolet.glacimon:glacimon-spi-core:?'
    
    compile 'com.github.shepherdviolet.glacimon:glacijava-common:?'
    compile 'com.github.shepherdviolet.glacimon:glacijava-crypto:?'
    compile 'com.github.shepherdviolet.glacimon:glacijava-trace:?'
    
    compile 'com.github.shepherdviolet.glacimon:glacispring-common:?'
    compile 'com.github.shepherdviolet.glacimon:glacispring-txtimer:?'
    compile 'com.github.shepherdviolet.glacimon:glacispring-httpclient:?'
    compile 'com.github.shepherdviolet.glacimon:glacispring-mapxbean:?'
    compile 'com.github.shepherdviolet.glacimon:glacispring-helper:?'
}

```

```maven
    <dependency>    
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>glacimon-spi-core</artifactId>
        <version>?</version> 
    </dependency>
    
    <dependency>    
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>glacijava-common</artifactId>
        <version>?</version> 
    </dependency>
    <dependency>    
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>glacijava-crypto</artifactId>
        <version>?</version> 
    </dependency>
    <dependency>    
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>glacijava-trace</artifactId>
        <version>?</version> 
    </dependency>
    
    <dependency>
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>glacispring-common</artifactId>
        <version>?</version>
    </dependency>
    <dependency>
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>glacispring-txtimer</artifactId>
        <version>?</version>
    </dependency>
    <dependency>
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>glacispring-httpclient</artifactId>
        <version>?</version>
    </dependency>
    <dependency>
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>glacispring-mapxbean</artifactId>
        <version>?</version>
    </dependency>
    <dependency>
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>glacispring-helper</artifactId>
        <version>?</version>
    </dependency>
```
