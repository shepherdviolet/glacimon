# SimpleKeyValueEncoder

* [Source Code](https://github.com/shepherdviolet/glacimon/blob/master/glacijava-common/src/main/java/com/github/shepherdviolet/glacimon/java/conversion/SimpleKeyValueEncoder.java)
* 这是一个简易的Key-Value编码工具, 用于将键值对编码成String, 或从String解码得到键值对

# 编码

## 逗号分隔

* 执行如下代码

```text
        Map<String, String> map = new LinkedHashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        String encoded = SimpleKeyValueEncoder.encode(map);

        List<KeyValue<String, String>> list = new ArrayList<>();
        list.put("key1", "value1");
        list.put("key2", "value2");
        list.put("key3", "value3");
        String encoded = SimpleKeyValueEncoder.encode(list);
```

* 得到如下字符串

```text
key1=value1,key2=value2,key3=value3
```

## 换行分隔

* 执行如下代码

```text
        Map<String, String> map = new LinkedHashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        String encoded = SimpleKeyValueEncoder.encode(map, true);
        
        List<KeyValue<String, String>> list = new ArrayList<>();
        list.put("key1", "value1");
        list.put("key2", "value2");
        list.put("key3", "value3");
        String encoded = SimpleKeyValueEncoder.encode(list, true);
```

* 得到如下字符串

```text
key1=value1
key2=value2
key3=value3
```

# 解码

* 有字符串如下

```text
key1=value1,key2=value2,key3=value3
```

* 或

```text
key1=value1
key2=value2
key3=value3
```

* 执行如下代码, 得到键值对

```text
    Map<String, String> result1 = SimpleKeyValueEncoder.decode(encoded);
    List<KeyValue<String, String>> result2 = SimpleKeyValueEncoder.decodeToList(encoded);
```

* 转义符

```text
空值   ->  \0
逗号,  ->  \,
等号=  ->  \=
反斜\  ->  \\
空格   ->  \s
TAB   ->  \t
换行   ->  \n
回车   ->  \r
```
