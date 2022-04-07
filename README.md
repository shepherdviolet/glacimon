# Glacimon (glacijava/glacispring) 2022.0.0

[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/shepherdviolet/glacimon.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/shepherdviolet/glacimon/context:java)

* Comprehensive Java library
* [Github Home](https://github.com/shepherdviolet/glacimon)
* [Search in Maven Central](https://search.maven.org/search?q=g:com.github.shepherdviolet.glacimon)
* [PGP Key](https://keyserver.ubuntu.com/pks/lookup?search=0x90998B78AABD6E96&fingerprint=on&op=index)

<br>
<br>

# glacijava (For JavaSE JDK7+)

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
```
