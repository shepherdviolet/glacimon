# Glaciion

* A library of Java Service Provider Interface
* Git: https://github.com/shepherdviolet/glaciion
* [中文文档](https://github.com/shepherdviolet/glaciion/blob/master/docs/index-cn.md)

<br>

## What's SPI and Glaciion?

```text
Service provider interface is a feature for discovering and loading implementations matching the given interface. 
Glaciion is an implementation of service provider interface feature. It can make your library expandable. 
```

<br>

## Feature

* Ability to load a unique implementation through the selection mechanism: single-service mode
* Ability to load multiple implementations through the plug-unplug mechanism: multiple-service mode
* Ability to load properties through the selection mechanism, and injecting into the implementation instance
* Ability to pre-check and load all services from the classpath during system startup: preload
* Ability to adjust implementation and properties through definition files or VM options

<br>

## Import dependencies from maven repository

```gradle
repositories {
    //glaciion in mavenCentral
    mavenCentral()
}
dependencies {
    compile 'com.github.shepherdviolet:glaciion-core:?'
}
```

```maven
    <dependency>    
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>glaciion-core</artifactId>
        <version>?</version> 
    </dependency>
```

<br>

## Documents

* [Single-Service Mode](https://github.com/shepherdviolet/glaciion/blob/master/docs/single-service-mode.md)

```text
If only one implementation is required, select the single-service mode.
You will notice that the annotation on the interface class is @SingleServiceInterface.
It will load which implementation based on the priority in the definition file.
```

* [Multiple-Service Mode](https://github.com/shepherdviolet/glaciion/blob/master/docs/multiple-service-mode.md)

```text
If multiple implementations are required, select the multiple-service mode.
You will notice that the annotation on the interface class is @MultipleServiceInterface.
It will load which implementations based on the enable(+) and disable(-) sign in the definition file.
The service loader can get an instance by name or a list of all instances (sorted by priority).
```

* [Property Inject](https://github.com/shepherdviolet/glaciion/blob/master/docs/property-injection.md)

```text
Inject properties into the implementation instance.
It chooses which properties to apply based on the priority in the properties file. 
The highest priority will be applied, and the other will not take effect.
```

* [Preload](https://github.com/shepherdviolet/glaciion/blob/master/docs/preload.md)

```text
Pre-check and load the SPI definitions. 
If there is a problem in the definition file / interface class / implementation class, 
the exception can be thrown in advance.
We can print (or get) the CheckSum value after preload, which is used to judge whether 
the implementation class or properties is changed accidentally. 
(Polluted by dependencies or Inadvertently modified, etc.)
```

* [Implementation Lifecycle](https://github.com/shepherdviolet/glaciion/blob/master/docs/implementation-lifecycle.md)

```text
It can perceive while the implementation instance created or closed (service loader uninstalled). 
```

* [File Exclusion](https://github.com/shepherdviolet/glaciion/blob/master/docs/file-exclusion.md)

```text
When a problem definition file exists under the classpath, we can temporarily exclude it. 
```

* [About Cache](https://github.com/shepherdviolet/glaciion/blob/master/docs/about-cache.md)

```text
After the service is loaded, the service loader (SingleServiceLoader / MultipleServiceLoader) will be cached, 
and get from the cache when it is loaded repeatedly.
After the service is instantiated, the service instance will be cached in the service loader, 
and get from the cache when it is got repeatedly.
Therefore, in general, the service instance will be held by static field (similar to the singleton in the Spring Context), 
if the service implementation class is not properly designed, it may cause memory leaks.
To accommodate special usages, the service loader is allowed to be uninstalled (removed from the cache).
```

* [Upgrade Interface](https://github.com/shepherdviolet/glaciion/blob/master/docs/upgrade-interface.md)

```text
The interface should be kept as constant as possible. If the method is modified, implementations based on the old version 
interface will be incompatible. In order to meet the interface upgrade requirements, Glaciion supports adding methods to 
the interface, and provide a backward compatible approach.
```

<br>

## About log

* When SLF4J exists in the classpath, the log is output with SLF4J by default.
* When SLF4J does not exist in the classpath, the log is output with System.out by default .

### SLF4J Logger

* Logger Package `com.github.shepherdviolet.glaciion`
* Recommended level `INFO`
* If you need to troubleshoot, adjust the log level to `DEBUG` or `TRACE`

### System.out Logger

* The default log level is `OFF`
* If you need to troubleshoot, adjust the log level to `DEBUG` or `TRACE`
* Adjust the log level by VM option

```text
-Dglaciion.conf.system.loglevel=ERROR
-Dglaciion.conf.system.loglevel=WARN
-Dglaciion.conf.system.loglevel=INFO
-Dglaciion.conf.system.loglevel=DEBUG
-Dglaciion.conf.system.loglevel=TRACE
```

* Adjust the date format by VM option

```text
-Dglaciion.conf.system.logtime=yyyyMMdd-HHmmss
```

### Custom logger

* Implement the interface:com.github.shepherdviolet.glaciion.api.interfaces.SpiLogger
* Add VM option:-Dglaciion.conf.custom.logger=`classname-of-custom-logger`