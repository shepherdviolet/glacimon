# GlacimonSpi File Exclusion

```text
When a problem definition file exists under the classpath, we can temporarily exclude it. 
```

[Back to index](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/index.md)

<br>

## Exclude definition files

### 1.Enable Logging

* The basic mode does not output logs externally. Please refer to the [Homepage](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/index.md) to enable logging (you can also refer to the homepage for extracting logs from memory).

### 2.Set log level to trace

* In-Memory Logs: Add VM option `-Dglacimonspi.conf.mem.loglevel=TRACE`
* SLF4J Logs: Set log level of package`com.github.shepherdviolet.glacimon.java.spi` to `TRACE`
* System.out Logs: Add VM option `-Dglacimonspi.conf.system.loglevel=TRACE`

### 3.See logs

* There is an md5 value after the file loading log
* Record the md5 value that needs to be excluded

```text
08:44:52.753 [main] TRACE com.github.shepherdviolet.glacimon.java.spi.GlacimonSpi - 0 | Loading file file:/D:/glacimon/glacimon-spi-test/out/test/resources/META-INF/glacimonspi/services/multiple/com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin, md5:a936a6931ba1eaa50a6a1b18cf42fc77
08:44:52.942 [main] TRACE com.github.shepherdviolet.glacimon.java.spi.GlacimonSpi - 0 | Loading file file:/D:/glacimon/glacimon-spi-test/out/production/resources/META-INF/glacimonspi/services/multiple/com.github.shepherdviolet.glacimon.java.spi.test.SamplePlugin, md5:bb16e015cfea2dc672777ebcd8d99ba2
```

* `NOTICE: The md5 value is calculated based on the contents of the file. Once the content of the file changes, 
the md5 value will also change, so this feature can only solve the problem temporarily.`

### 4.Add VM option

* -Dglacimonspi.exclude.file=`md5`,`md5`,`md5`
* Example: -Dglacimonspi.exclude.file=a936a6931ba1eaa50a6a1b18cf42fc77,bb16e015cfea2dc672777ebcd8d99ba2
