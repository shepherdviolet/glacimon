# TxTimer 日志样例

* SLF4J日志级别`INFO`, 日志包路径`sviolet.slate.common.x.monitor.txtimer.def`

## 20.0.0+ 14.8+

* 格式为`|标识|版本|随机数|开始时间|统计时长|组名|交易名|执行中交易数||总平均耗时|总交易数||最小耗时|最大耗时|平均耗时|交易数|`
* `总平均耗时`和`总交易数`为应用启动以来的数据, 其他的是最近一个周期的数据, 时间单位为ms
* `随机数`在进程启动时产生, 用于标记报告属于哪个进程(不严格), 通常用于去重或分析问题出在哪个进程
* 默认情况下, 当一个组别的输出记录超过20条时, 会分页, 每页的页码会+1

```text
2018-10-03 09:12:38,978 INFO Slate-TxTimer-Report-0 s.s.common.x.monitor.txtimer.def.Reporter : Page 1
   Ver Rand StartTime Duration Group Name RunCnt     TotAvg TotCnt     CurrMin CurrMax CurrAvg CurrCnt (TimeUnit:ms)
TxT|1|DriYUYUu|20191003 09:09:00|180000|rpc-invoke|template.api.base.UserService#get|0||153|195353||102|573|162|44|
TxT|1|DriYUYUu|20191003 09:09:00|180000|rpc-invoke|template.api.base.UserService#set|0||352|75353||287|851|377|26|
```

## 14.7-

* `Group (...) Time (...)  Page ...`: 当前打印的交易组别, 统计时间区间(非精确), 页码
* 默认情况下, 当一个组别的输出记录超过20条时, 会分页, 每页的页码会+1
* ` ... > last ? min ( cnt: ... avg: ... max:... min:... ) `: 该交易最近N分钟的交易数(cnt), 平均耗时(avg), 最大耗时(max), 最小耗时(min)
* `total ( cnt:... ing:... est-avg:... )`: 该交易累计交易数(cnt), 当前执行中的交易数(ing), 平均耗时估计(est-avg)

```text
2018-10-03 09:12:38,978 INFO Slate-TxTimer-Report-0 s.s.common.x.monitor.txtimer.def.Reporter : 
TxTimer | ------------------------------------------------------------------------------------------------------------
TxTimer | Group (Scrunchy-Actions) Time (2019-10-03 09:09:00 - 2019-10-03 09:12:00)  Page 1
TxTimer | template.api.base.UserService#get > last 3 min ( cnt:44, avg:162ms, max:573ms, min:102ms ) total ( cnt:195353, ing:0, est-avg:153ms )
TxTimer | template.api.base.UserService#set > last 3 min ( cnt:26, avg:377ms, max:851ms, min:287ms ) total ( cnt:75353, ing:0, est-avg:352ms )
```
