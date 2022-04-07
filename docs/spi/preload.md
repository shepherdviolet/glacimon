# Glaciion Preload

```text
Pre-check and load the SPI definitions. 
If there is a problem in the definition file / interface class / implementation class, 
the exception can be thrown in advance.
We can print (or get) the CheckSum value after preload, which is used to judge whether 
the implementation class or properties is changed accidentally. 
(Polluted by dependencies or Inadvertently modified, etc.)
```

[Back to index](https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md)

<br>

## What's preload

```text
Back-end systems are usually developed by multiple people. If a developer adjusts the definition file or introduces a 
library (dependency) containing glaciion definition files, it may cause the implementation to be accidentally replaced, 
or the properties to be accidentally adjusted. It may cause a production accident.
In addition, if you do not load all the services during the startup phase, you will not be able to find the problem in advance.
Preloading is designed to solve such problems. Discovery and load all definition files and properties files under the 
classpath ahead of time, throw an exception if there is a problem. Finally, a checkSum value can be output to determine 
if the definition and properties have been modified. Preloading does not create a service instance.
```

<br>

## Auto preload

* Auto preload means: Trigger preload when the service is first loaded
* Automatic preloading is enabled by default in the Spring environment (determine whether the ApplicationContext class exists in the classpath)
* Automatic preloading is turned off by default in non-Spring environments
* You can add a VM option to turn automatic preloading on or off.

```text
-Dglaciion.conf.preload.auto=true
-Dglaciion.conf.preload.auto=false
```

<br>

## Preload manually

* You can manually call the API to start preloading during the system startup phase

```text
//Preload by the default classloader
Glaciion.preload();
//Preload by the specified classloader
//Glaciion.preload(classloader);
```

<br>

## CheckSum

* After the preload, you can get a checkSum value.
* Add the VM option '-Dglaciion.conf.preload.checksum=false` to disable checkSum calculation

### What will cause the CheckSum change?

* Increase and decrease of extension points (Glaciion interface)
* Implementation changes / additions / reductions (including order and name)
* Injection properties changes(Including adjustment with VM options)

### How to use CheckSum

* You can see it in the log

```text
11:40:57.470 [main] INFO com.github.shepherdviolet.glaciion.Glaciion - ? | Preload | CheckSum 1851083996, classloader:sun.misc.Launcher$AppClassLoader@18b4aac2
```

* You can get it by API
* `WARNING: If the preload is not completed or CheckSum calculation is disabled, it will return null`

```text
//Get CheckSum preloaded with default classloader
Integer checkSum = Glaciion.getPreloadCheckSum();

//Get CheckSum preloaded with specified classloader
//Integer checkSum = Glaciion.getPreloadCheckSum(classloader);

//Get all CheckSum values, Map's Key is the classloader ID
//Map<String, Integer> checkSums = Glaciion.getPreloadCheckSums()
```

* In extremely strict cases, you can configure the CheckSum value of this package in the update package or configuration 
center, and automatically check during the system startup phase.
