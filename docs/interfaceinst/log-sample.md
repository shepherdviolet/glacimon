# InterfaceInstantiation 日志样例

* SLF4J日志级别`INFO`, 日志包路径`sviolet.slate.common.x.proxy.interfaceinst`
* `InterfaceInstantiator`: 接口实例化器的实现类
* `Scan package`: 扫描的类路径
* `Bean created`: 创建的接口实例

```text
InterfaceInstBeanDefRegistry5 : InterfaceInst | Interface Instantiation Start (spring 5+ and jdk 8+) Doc: https://github.com/shepherdviolet/slate
InterfaceInstBeanDefRegistry5 : InterfaceInst | InterfaceInstantiator:beet.scrunchy.core.ServiceDispatchInstanceInstantiator
InterfaceInstBeanDefRegistry5 : InterfaceInst | Scan package:template.api
InterfaceInstBeanDefRegistry5 : InterfaceInst | Bean created:template.api.base.UserService, name:proxy.template.api.base.UserService
InterfaceInstBeanDefRegistry5 : InterfaceInst | Interface Instantiation Finish
```
