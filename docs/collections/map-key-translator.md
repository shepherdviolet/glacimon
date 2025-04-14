# Map键映射工具 KeyMapTranslator

* 传入源Map和目的Map

```
    Map<String, Object> src = ...源数据...
    Map<String, Object> dest = new HashMap<>();

    MapKeyTranslator.keyMappings(MapKeyTranslator.NullStrategy.KEEP_NULL,
            "AAA",  "aaa",
            "BBB",  "bbb",
            "CCC",  "ccc",
            "DDD",  "ddd",
            "EEE",  "eee",
            "FFF",  "fff"
    ).translate(src, dest);
```

* 传入源Map, 返回目的Map

```
    Map<String, Object> src = ...源数据...

    Map<String, Object> dest = MapKeyTranslator.keyMappings(MapKeyTranslator.NullStrategy.KEEP_NULL,
            "AAA",  "aaa",
            "BBB",  "bbb",
            "CCC",  "ccc",
            "DDD",  "ddd",
            "EEE",  "eee",
            "FFF",  "fff"
    ).translate(src);
```

* 以上`MapKeyTranslator.translate()`操作等效于手动赋值, 如下所示: 

```
    Map<String, Object> src = ...源数据...
    Map<String, Object> dest = new HashMap<>();
    
    dest.put("AAA", src.get("aaa"));
    dest.put("BBB", src.get("bbb"));
    dest.put("CCC", src.get("ccc"));
    dest.put("DDD", src.get("ddd"));
    dest.put("EEE", src.get("eee"));
    dest.put("FFF", src.get("fff"));
```

<br>

# 依赖

* gradle

```text
//version替换为具体版本(2025.1.0以上)
dependencies {
    compile 'com.github.shepherdviolet.glacimon:glacijava-common:?'
}
```

* maven

```maven
    <!--version替换为具体版本(2025.1.0以上)-->
    <dependency>
        <groupId>com.github.shepherdviolet.glacimon</groupId>
        <artifactId>glacijava-common</artifactId>
        <version>?</version>
    </dependency>
```

