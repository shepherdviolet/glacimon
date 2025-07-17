# HttpClient 自定义DNS

* `GlaciHttpClient`默认使用系统DNS解析域名
* 若使用`dns-description`参数配置自定义DNS, 则需要手动依赖`dnsjava:dnsjava:3.6.3`
* 支持配置多个DNS, 支持同步解析, 支持后台自动更新

## 配置方法

### 添加依赖

```
implementation 'dnsjava:dnsjava:3.6.3'
```

### 配置

* 通过yaml配置

```
glacispring:
  httpclients:
    client1:
      ......
      # 配置自定义Dns
      dns-description: ip=8.8.8.8|114.114.114.114,resolveTimeoutSeconds=5,preferIPv6=false
      # 关注DNS的同学可能还需要关注'最大闲置连接数'参数. 若设置为0(默认), 每次重新解析域名+重新建立连接, 性能差, 但支持动态域名解析. 若设置为正整数(例如16), 会复用连接池中的连接, 性能强, 但若DNS域名解析记录更新, 可能会向原IP发送请求.
      #max-idle-connections: 0
```

* 通过代码配置

```
@Configuration
public class MyConfiguration {

    @Value("${http.client.dns-description}")
    private String dnsDescription;

    @Bean
    public GlaciHttpClient client1() {
        return new GlaciHttpClient()
                ......
                .setDnsDescription(dnsDescription) // 配置自定义Dns
                .setMaxIdleConnections(0); // 关注DNS的同学可能还需要关注'最大闲置连接数'参数. 若设置为0(默认), 每次重新解析域名+重新建立连接, 性能差, 但支持动态域名解析. 若设置为正整数(例如16), 会复用连接池中的连接, 性能强, 但若DNS域名解析记录更新, 可能会向原IP发送请求.
    }
}
```

### `dns-description`参数详解

> 采用SimpleKeyValueEncoder格式, 详见: https://github.com/shepherdviolet/glacimon/blob/master/docs/kvencoder/guide.md

* 参数格式

```
key1=value1,key2=value2,key3=value3
```

* 参数说明

| 参数名                   | 说明                                        | 备注                                            |
|-----------------------|-------------------------------------------|-----------------------------------------------|
| ip                    | DNS服务地址, `必输`                             | 多个地址使用'\|'分割, 例如: ip=8.8.8.8\|114.114.114.114 |
| resolveTimeoutSeconds | 域名解析超时时间(秒), 可选, 默认5s                     |                                               |
| preferIpv6            | true:Ipv6优先, false:Ipv4优先, 可选, 默认false    |                                               |
| minTtlSeconds         | 最小TTL(秒), 可选, 默认20                        | 实际TTL为max(服务器返回TTL, 该参数值)                     |
| maxTtlSeconds         | 最大TTL(秒), 可选, 默认300                       | 实际TTL为min(服务器返回TTL, 该参数值)                     |
| errorTtlSeconds       | 域名解析错误时的TTL(秒), 可选, 默认0                   |                                               |
| updMinIntervalSec     | 后台自动更新最小间隔(秒), 可选, 默认5                    | 程序会在TTL到期前自动更新域名解析记录, 这是更新线程最小间隔              |
| updMaxIntervalSec     | 后台自动更新最大间隔(秒), 可选, 默认3600                 | 程序会在TTL到期前自动更新域名解析记录, 这是更新线程最大间隔              |
| isBackgroundUpdate    | true:启用后台自动更新, false:关闭后台自动更新, 可选, 默认true |                                               |
| reportIntervalSec     | DNS解析报告打印间隔(秒), 可选, 默认3600                | 程序会在日志中打印DNS解析相关统计信息                          |
| stopUpdAftFails       | 域名解析失败指定次数后, 停止自动更新, 可选, 默认5              | 仅影响自动更新, 不影响同步解析                              |
| stopUpdAftIdleSec     | 域名未使用指定时间(秒)后, 停止自动更新, 可选, 默认1200         | 仅影响自动更新, 不影响同步解析                              |

* 参数示例

* 设置一个DNS: ip=8.8.8.8
* 设置多个DNS: ip=8.8.8.8|114.114.114.114
* 设置解析超时时间: ip=8.8.8.8,resolveTimeoutSeconds=5
* 设置ipv6是否优先: ip=8.8.8.8,resolveTimeoutSeconds=5,preferIpv6=false
* 设置最大最小TTL: ip=8.8.8.8,resolveTimeoutSeconds=5,preferIpv6=false,minTtlSeconds=30,maxTtlSeconds=300
* 设置是否后台自动更新: ip=8.8.8.8,resolveTimeoutSeconds=5,preferIpv6=false,isBackgroundUpdate=true

<br>
<br>

## 运行机制

### 域名解析(lookup)

* 从缓存获取记录
* 检查缓存是否过期(TTL)
* * 缓存未过期: 返回结果 -> `结束执行`
* * 缓存已过期: 继续执行
* 用第一个DNS服务器解析域名(同步等待)
* * 成功: 缓存记录, 返回结果 -> `结束执行`
* * 失败: 继续执行
* 用第二个DNS服务器解析域名(同步等待)
* * 成功: 缓存记录, 返回结果 -> `结束执行`
* * 失败: 继续执行
* ......
* 缓存失败记录, 返回失败 -> `结束执行`

### 后台自动更新

> 同步域名解析虽然有缓存机制, 但过期后依然需要同步等待解析结果, 导致HTTP响应时长增加.
> 为了减少HTTP响应时间, 我们增加了后台自动更新机制, 在TTL过半的时候, 主动更新域名解析记录(异步).

* 计算`下次唤醒时间`
* * 遍历域名解析缓存, 忽略不需要更新的记录(超过指定时间未使用/失败超过指定次数)
* * 计算每条记录的`后台更新时间` = `过期时间` - `TTL` / 2
* * 取最早的`后台更新时间`作为`下次唤醒时间`
* * 若没有记录需要更新(无记录/全部长时间未使用/全部连续失败), 后台自动更新进入IDLE状态 (默认1小时唤醒一次)
* 挂起线程`等待时间到达`或`被提前唤醒` (提前唤醒另行说明)
* 执行异步更新
* * 遍历域名解析缓存, 对达到`后台更新时间`的记录进行异步更新(每个记录启一个线程)
* * CountDownLatch等待所有更新完成
* * -> `(回到开头重复执行)`

> 域名解析记录的TTL存在最大最小值限制(minTtlSeconds/maxTtlSeconds).
> 后台自动更新间隔存在最大最小限制(updMinIntervalSec/updMaxIntervalSec).

### 关于后台更新线程`被提前唤醒`

* 以下情况会提前唤醒`后台更新线程`
* * 新域名解析完成(成功/失败): 重新计算新增域名的`后台更新时间`
* * `dns-description`参数调整: 应用新参数
* * 后台更新线程进入IDLE状态后, 有域名解析成功/从缓存获取地址成功: 使后台更新线程退出IDLE状态
