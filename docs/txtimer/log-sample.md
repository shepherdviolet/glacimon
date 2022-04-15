# TxTimer 日志样例

* SLF4J日志级别`INFO`, 日志包路径`com.github.shepherdviolet.glacimon.spring.x.monitor.txtimer.def`

## 日志格式

* 格式为`|标识|版本|随机数|开始时间|统计时长|组名|交易名|执行中交易数||总平均耗时|总交易数||最小耗时|最大耗时|平均耗时|交易数|`
* `总平均耗时`和`总交易数`为应用启动以来的数据, 其他的是最近一个周期的数据, 时间单位为ms
* `随机数`在进程启动时产生, 用于标记报告属于哪个进程(不严格), 通常用于去重或分析问题出在哪个进程
* 默认情况下, 当一个组别的输出记录超过20条时, 会分页, 每页的页码会+1

```text
2018-10-03 09:12:38,978 INFO Glacispring-TxTimer-Report-0 s.s.common.x.monitor.txtimer.def.Reporter : Page 1
   Ver Rand StartTime Duration Group Name RunCnt     TotAvg TotCnt     CurrMin CurrMax CurrAvg CurrCnt (TimeUnit:ms)
TxT|1|DriYUYUu|20191003 09:09:00|180000|rpc-invoke|template.api.base.UserService#get|0||153|195353||102|573|162|44|
TxT|1|DriYUYUu|20191003 09:09:00|180000|rpc-invoke|template.api.base.UserService#set|0||352|75353||287|851|377|26|
```
