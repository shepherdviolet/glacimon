# Glaciion File Exclusion

```text
When a problem definition file exists under the classpath, we can temporarily exclude it. 
```

[Back to index](https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md)

<br>

## Exclude definition files

### 1.Set log level to trace

* SLF4J: Set log level of package`com.github.shepherdviolet.glaciion` to `TRACE`
* System.out: Add VM option `-Dglaciion.conf.system.loglevel=TRACE`

### 2.See logs

* There is an md5 value after the file loading log
* Record the md5 value that needs to be excluded

```text
08:44:52.753 [main] TRACE com.github.shepherdviolet.glaciion.Glaciion - 0 | Loading file file:/D:/glaciion/tests/out/test/resources/META-INF/glaciion/services/multiple/com.github.shepherdviolet.glaciion.test.SamplePlugin, md5:a936a6931ba1eaa50a6a1b18cf42fc77
08:44:52.942 [main] TRACE com.github.shepherdviolet.glaciion.Glaciion - 0 | Loading file file:/D:/glaciion/tests/out/production/resources/META-INF/glaciion/services/multiple/com.github.shepherdviolet.glaciion.test.SamplePlugin, md5:bb16e015cfea2dc672777ebcd8d99ba2
```

* `NOTICE: The md5 value is calculated based on the contents of the file. Once the content of the file changes, 
the md5 value will also change, so this feature can only solve the problem temporarily.`

### 3.Add VM option

* -Dglaciion.exclude.file=`md5`,`md5`,`md5`
* Example: -Dglaciion.exclude.file=a936a6931ba1eaa50a6a1b18cf42fc77,bb16e015cfea2dc672777ebcd8d99ba2
