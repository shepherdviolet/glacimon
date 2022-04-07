# Glacimon (GlaciJava / GlaciSpring) 2022.0.0

[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/shepherdviolet/glacimon.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/shepherdviolet/glacimon/context:java)

* Comprehensive Java library
* [Github Home](https://github.com/shepherdviolet/glacimon)
* [Search in Maven Central](https://search.maven.org/search?q=g:com.github.shepherdviolet.glacimon)
* [PGP Key](https://keyserver.ubuntu.com/pks/lookup?search=0x90998B78AABD6E96&fingerprint=on&op=index)

<br>
<br>

# GlaciJava (For JavaSE JDK7+)

## Module 'glacijava-spi-api'/'glacijava-spi-core'

> What's SPI?

```text
Service provider interface is a feature for discovering and loading implementations matching the given interface. 
Glaciion is an implementation of service provider interface feature. It can make your library expandable. 
```

> [English Documents](https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md)
> [中文文档](https://github.com/shepherdviolet/glaciion/blob/master/docs/index-cn.md)

<br>

## Module 'glacijava-common'

[![Depends](https://img.shields.io/badge/Depends-glaciion--api-dc143c.svg?style=flat)](https://github.com/shepherdviolet/glaciion)

> Core module of thistle

### Data structure

* [Bitmap / Bloom filter](https://github.com/shepherdviolet/thistle/tree/master/thistle-common/src/main/java/sviolet/thistle/model/bitmap)
* [Sliding window](https://github.com/shepherdviolet/thistle/tree/master/thistle-common/src/main/java/sviolet/thistle/model/statistic)

### Crypto utils

* [Crypto utils : RSA ECDSA AES DES / SHA MD5 / PEM p12 ...](https://github.com/shepherdviolet/thistle/blob/master/docs/crypto/guide.md)

### Reflect utils

* [BeanInfoUtils : Get property information of Java Bean](https://github.com/shepherdviolet/thistle/tree/master/thistle-common/src/main/java/sviolet/thistle/util/reflect/BeanInfoUtils.java)
* [GenericClassUtils : Get actual types of generic class](https://github.com/shepherdviolet/thistle/tree/master/thistle-common/src/main/java/sviolet/thistle/util/reflect/GenericClassUtils.java)
* [MethodCaller : Get caller information of a method](https://github.com/shepherdviolet/thistle/tree/master/thistle-common/src/main/java/sviolet/thistle/util/reflect/MethodCaller.java)
* [ClassPrinter : Print all information for a class / object](https://github.com/shepherdviolet/thistle/tree/master/thistle-common/src/main/java/sviolet/thistle/util/reflect/ClassPrinter.java)

### Misc utils

* [SimpleKeyValueEncoder : Convert between simple Key-Value and String](https://github.com/shepherdviolet/thistle/blob/master/docs/kvencoder/guide.md)
* [ThreadPoolExecutorUtils : Create thread pool](https://github.com/shepherdviolet/thistle/tree/master/thistle-common/src/main/java/sviolet/thistle/util/concurrent/ThreadPoolExecutorUtils.java)
* [...](https://github.com/shepherdviolet/thistle/tree/master/thistle-common/src/main/java/sviolet/thistle/util)

<br>

## Module 'glacijava-crypto'

[![Depends](https://img.shields.io/badge/Depends-thistle--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/thistle)
[![Depends](https://img.shields.io/badge/Depends-bcpkix--jdk15on-dc143c.svg?style=flat)](https://search.maven.org/search?q=g:org.bouncycastle%20a:bcpkix-jdk15on)

> The module has more crypto features (depends on bouncy-castle)

### Crypto utils

* [Advanced crypto utils : SM2 SM4 / SM3 ...](https://github.com/shepherdviolet/thistle/blob/master/docs/crypto/guide.md)

<br>

## Module 'glacijava-trace'

[![Depends](https://img.shields.io/badge/Depends-thistle--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/thistle)
[![Depends](https://img.shields.io/badge/Depends-glaciion--core-dc143c.svg?style=flat)](https://github.com/shepherdviolet/glaciion)

> The module for tracing

### Tracing utils

* [Trace : Help to trace invocation across thread or process](https://github.com/shepherdviolet/thistle/blob/master/docs/trace/guide.md)

<br>
<br>

# GlaciSpring (For Java Spring JDK8+)

## Module 'slate-common'

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

## Module 'slate-txtimer'

[![Depends](https://img.shields.io/badge/Depends-slate--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/slate)
[![Depends](https://img.shields.io/badge/Depends-glaciion--core-dc143c.svg?style=flat)](https://github.com/shepherdviolet/glaciion)

> The module for statistic

### TxTimer

* [TxTimer : RT Statistic API](https://github.com/shepherdviolet/slate/blob/master/docs/txtimer/guide.md)

<br>

## Module 'slate-helper'

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

## Module 'slate-http-client'

[![Depends](https://img.shields.io/badge/Depends-slate--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/slate)
[![Depends](https://img.shields.io/badge/Depends-slate--txtimer-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/slate)
[![Depends](https://img.shields.io/badge/Depends-okhttp-dc143c.svg?style=flat)](https://search.maven.org/search?q=g:com.squareup.okhttp3%20a:okhttp)

> Provides a solution for http client

### Http client

* [MultiHostOkHttpClient : A HTTP client supporting load balancing](https://github.com/shepherdviolet/slate/blob/master/docs/loadbalance/guide.md)

<br>

## Module 'slate-mapxbean'

[![Depends](https://img.shields.io/badge/Depends-slate--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/slate)
[![Depends](https://img.shields.io/badge/Depends-glaciion--core-dc143c.svg?style=flat)](https://github.com/shepherdviolet/glaciion)

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
    compile 'com.github.shepherdviolet:glaciion-core:?'
    compile 'com.github.shepherdviolet:thistle-common:?'
    compile 'com.github.shepherdviolet:thistle-crypto-plus:?'
    compile 'com.github.shepherdviolet:thistle-trace:?'
    compile 'com.github.shepherdviolet:slate-common:?'
    compile 'com.github.shepherdviolet:slate-txtimer:?'
    compile 'com.github.shepherdviolet:slate-helper:?'
    compile 'com.github.shepherdviolet:slate-http-client:?'
    compile 'com.github.shepherdviolet:slate-mapxbean:?'
}

```

```maven
    <dependency>    
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>glaciion-core</artifactId>
        <version>?</version> 
    </dependency>
    <dependency>    
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle-common</artifactId>
        <version>?</version> 
    </dependency>
    <dependency>    
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle-crypto-plus</artifactId>
        <version>?</version> 
    </dependency>
    <dependency>    
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle-trace</artifactId>
        <version>?</version> 
    </dependency>
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>slate-common</artifactId>
        <version>?</version>
    </dependency>
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>slate-txtimer</artifactId>
        <version>?</version>
    </dependency>
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>slate-helper</artifactId>
        <version>?</version>
    </dependency>
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>slate-http-client</artifactId>
        <version>?</version>
    </dependency>
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>slate-mapxbean</artifactId>
        <version>?</version>
    </dependency>
```
