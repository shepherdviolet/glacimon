# Glacimon (GlaciJava / GlaciSpring) 2022.0.0

[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/shepherdviolet/glacimon.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/shepherdviolet/glacimon/context:java)

* Comprehensive Java library, JDK8+
* [Github Home](https://github.com/shepherdviolet/glacimon)
* [Search in Maven Central](https://search.maven.org/search?q=g:com.github.shepherdviolet.glacimon)
* [PGP Key](https://keyserver.ubuntu.com/pks/lookup?search=0x90998B78AABD6E96&fingerprint=on&op=index)

<br>
<br>

# GlaciJava (Glacimon for JavaSE)

<br>

## Module: glacijava-spi-api glacijava-spi-core

> GlacimonSpi is an implementation of service provider interface feature. It can make your library expandable.

### SPI

* [GlacimonSpi English Documents](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/index.md)
* [GlacimonSpi 中文文档](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/index-cn.md)

<br>

## Module: glacijava-common

[![Depends](https://img.shields.io/badge/Depends-glacijava--spi--api-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/glacimon)

> Common utils for JavaSE

### Data structure

* [Bitmap / Bloom filter](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/datastruc/bitmap)
* [Cache](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/datastruc/cache)
* [...](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/datastruc)

### Crypto utils

* [Crypto utils : RSA ECDSA AES DES / SHA MD5 / PEM p12 ...](https://github.com/shepherdviolet/glacimon/blob/master/docs/crypto/guide.md)

### Reflect utils

* [BeanInfoUtils : Get property information of Java Bean](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/reflect/BeanInfoUtils.java)
* [GenericClassUtils : Get actual types of generic class](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/reflect/GenericClassUtils.java)
* [MethodCaller : Get caller information of a method](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/reflect/MethodCaller.java)
* [ClassPrinter : Print all information for a class / object](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/reflect/ClassPrinter.java)
* [...](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/reflect)

### Concurrent utils

* [ThreadPoolExecutorUtils : Create thread pool](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/concurrent/ThreadPoolExecutorUtils.java)
* [...](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/concurrent)

### Conversion utils

* [SimpleKeyValueEncoder : Convert between simple Key-Value and String](https://github.com/shepherdviolet/glacimon/blob/master/docs/kvencoder/guide.md)
* [...](https://github.com/shepherdviolet/glacimon/tree/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/conversion)

<br>

## Module: glacijava-crypto

[![Depends](https://img.shields.io/badge/Depends-glacijava--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/glacimon)
[![Depends](https://img.shields.io/badge/Depends-bcpkix--jdk15on-dc143c.svg?style=flat)](https://search.maven.org/search?q=g:org.bouncycastle%20a:bcpkix-jdk15on)

> More crypto features (depends on bouncy-castle)

### Crypto utils

* [Crypto utils : SM2 SM3 SM4 ...](https://github.com/shepherdviolet/glacimon/blob/master/docs/crypto/guide.md)

<br>

## Module: glacijava-trace

[![Depends](https://img.shields.io/badge/Depends-thistle--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/thistle)
[![Depends](https://img.shields.io/badge/Depends-glacijava--spi--core-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/glacimon)

> The module for tracing

### Tracing utils

* [Trace : Help to trace invocation across thread or process](https://github.com/shepherdviolet/thistle/blob/master/docs/trace/guide.md)

<br>
<br>

# GlaciSpring (Glacimon for Java Spring)

<br>

## Module: slate-common

[![Depends](https://img.shields.io/badge/Depends-thistle--common-dc143c.svg?style=flat)](https://github.com/shepherdviolet/thistle)
[![Depends](https://img.shields.io/badge/Depends-spring--context-dc143c.svg?style=flat)](https://search.maven.org/search?q=g:org.springframework%20a:spring-context)
[![Depends](https://img.shields.io/badge/Depends-slf4j--api-dc143c.svg?style=flat)](https://search.maven.org/search?q=g:org.slf4j%20a:slf4j-api)

> Core module of slate

| Auto Configurations |
| ------------------- |
| [SlateCommonAutoConfiguration](https://github.com/shepherdviolet/slate/tree/master/slate-common/src/main/java/sviolet/slate/common/springboot/autoconfig/SlateCommonAutoConfiguration.java) |

### Spring utils

* [InterfaceInstantiation : Instantiate interfaces into Spring context](https://github.com/shepherdviolet/slate/blob/master/docs/interfaceinst/guide.md)
* [MemberProcessor : Process all fields/methods (of bean) in Spring context (To implement custom injection / method binding...)](https://github.com/shepherdviolet/slate/blob/master/docs/mbrproc/guide.md)
* [YamlPropertySourceFactory : Load YAML by @PropertySource](https://github.com/shepherdviolet/slate/tree/master/slate-common/src/main/java/sviolet/slate/common/spring/property)
* [LambdaBuilder(Buildable) : New object in lambda way](https://github.com/shepherdviolet/slate/tree/master/slate-common/src/main/java/sviolet/slate/common/util/common)
* [...](https://github.com/shepherdviolet/slate/tree/master/slate-common/src/main/java/sviolet/slate/common/util)

### Helpers

* [DynamicDataSource : Dynamic datasource for Spring Boot](https://github.com/shepherdviolet/slate/tree/master/slate-common/src/main/java/sviolet/slate/common/helper/data/datasource/DynamicDataSource.java)
* [...](https://github.com/shepherdviolet/slate/tree/master/slate-common/src/main/java/sviolet/slate/common/helper)

<br>

## Module: slate-txtimer

[![Depends](https://img.shields.io/badge/Depends-slate--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/slate)
[![Depends](https://img.shields.io/badge/Depends-glacijava--spi--core-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/glacimon)

> The module for statistic

### TxTimer

* [TxTimer : RT Statistic API](https://github.com/shepherdviolet/slate/blob/master/docs/txtimer/guide.md)

<br>

## Module: slate-helper

[![Depends](https://img.shields.io/badge/Depends-slate--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/slate)
![Depends](https://img.shields.io/badge/Depends-...-dc143c.svg?style=flat)

> Helpers for third-party libraries

### Helpers

* [RocketMQ : Subscribe message by annotation](https://github.com/shepherdviolet/slate/blob/master/docs/rocketmq/guide.md)
* [Sentinel : Another way to config rules](https://github.com/shepherdviolet/slate/blob/master/docs/ezsentinel/guide.md)
* [Apollo : ApolloRefreshableProperties : The 'Properties' dynamically updated by Apollo](https://github.com/shepherdviolet/slate/tree/master/slate-helper/src/main/java/sviolet/slate/common/helper/apollo/ApolloRefreshableProperties.java)
* [Hessianlite : HessianLiteSerializeUtils : Serialize util with hessianlite](https://github.com/shepherdviolet/slate/tree/master/slate-helper/src/main/java/sviolet/slate/common/helper/hessianlite/HessianLiteSerializeUtils.java)
* [JetCache : SyncRedisLettuceCacheBuilder : Connect to redis in a synchronous manner for JetCache](https://github.com/shepherdviolet/slate/tree/master/slate-helper/src/main/java/sviolet/slate/common/helper/jetcache/lettuce/SyncRedisLettuceCacheBuilder.java)
* [...](https://github.com/shepherdviolet/slate/tree/master/slate-helper/src/main/java/sviolet/slate/common/helper)

<br>

## Module: slate-http-client

[![Depends](https://img.shields.io/badge/Depends-slate--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/slate)
[![Depends](https://img.shields.io/badge/Depends-slate--txtimer-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/slate)
[![Depends](https://img.shields.io/badge/Depends-okhttp-dc143c.svg?style=flat)](https://search.maven.org/search?q=g:com.squareup.okhttp3%20a:okhttp)

> Provides a solution for http client

### Http client

* [MultiHostOkHttpClient : A HTTP client supporting load balancing](https://github.com/shepherdviolet/slate/blob/master/docs/loadbalance/guide.md)

<br>

## Module: slate-mapxbean

[![Depends](https://img.shields.io/badge/Depends-slate--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/slate)
[![Depends](https://img.shields.io/badge/Depends-glacijava--spi--core-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/glacimon)

> Map - Bean converter

### MapXBean

* [MapXBean : Map - Bean Converter](https://github.com/shepherdviolet/slate/blob/master/docs/mapxbean/guide.md)

<br>
<br>

# Import dependencies from maven repository

* [Search in Maven Central](https://search.maven.org/search?q=g:com.github.shepherdviolet.glacimon)

```gradle

repositories {
    //In mavenCentral
    mavenCentral()
}
dependencies {
    compile 'com.github.shepherdviolet.glacimon:glacijava-spi-core:?'
    compile 'com.github.shepherdviolet.glacimon:thistle-common:?'
    compile 'com.github.shepherdviolet.glacimon:thistle-crypto-plus:?'
    compile 'com.github.shepherdviolet.glacimon:thistle-trace:?'
    compile 'com.github.shepherdviolet.glacimon:slate-common:?'
    compile 'com.github.shepherdviolet.glacimon:slate-txtimer:?'
    compile 'com.github.shepherdviolet.glacimon:slate-helper:?'
    compile 'com.github.shepherdviolet.glacimon:slate-http-client:?'
    compile 'com.github.shepherdviolet.glacimon:slate-mapxbean:?'
}

```

```maven
    <dependency>    
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>glacijava-spi-core</artifactId>
        <version>?</version> 
    </dependency>
    <dependency>    
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>thistle-common</artifactId>
        <version>?</version> 
    </dependency>
    <dependency>    
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>thistle-crypto-plus</artifactId>
        <version>?</version> 
    </dependency>
    <dependency>    
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>thistle-trace</artifactId>
        <version>?</version> 
    </dependency>
    <dependency>
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>slate-common</artifactId>
        <version>?</version>
    </dependency>
    <dependency>
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>slate-txtimer</artifactId>
        <version>?</version>
    </dependency>
    <dependency>
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>slate-helper</artifactId>
        <version>?</version>
    </dependency>
    <dependency>
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>slate-http-client</artifactId>
        <version>?</version>
    </dependency>
    <dependency>
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>slate-mapxbean</artifactId>
        <version>?</version>
    </dependency>
```
