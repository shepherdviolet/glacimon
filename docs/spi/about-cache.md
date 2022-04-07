# Glaciion About Cache

```text
After the service is loaded, the service loader (SingleServiceLoader / MultipleServiceLoader) will be cached, 
and get from the cache when it is loaded repeatedly.
After the service is instantiated, the service instance will be cached in the service loader, 
and get from the cache when it is got repeatedly.
Therefore, in general, the service instance will be held by static field (similar to the singleton in the Spring Context), 
if the service implementation class is not properly designed, it may cause memory leaks.
To accommodate special usages, the service loader is allowed to be uninstalled (removed from the cache).
```

[Back to index](https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md)

<br>

## References of instance

* Service loader instances is referenced by static variable of SingleServiceLoader and MultipleServiceLoader
* Service instances is referenced by service loader
* So the service instances is indirectly referenced by static variable

<br>

## Uninstall service loaders (Special Usage)

* If your implementation instance needs to be properly closed (i.e. receives a close signal), 
you can call the uninstall API to notify all implementation classes when needed, 
See [Implementation Lifecycle](https://github.com/shepherdviolet/glaciion/blob/master/docs/implementation-lifecycle.md)
* If you need to load / unload classes dynamically (i.e. need to create and destroy classloaders at runtime). 
You need to uninstall all the service instances loaded by the deserted classloader, otherwise it will cause memory leaks. 
* In fact, the uninstall operation removes the service loader from the cache and then sets the service loader's closed 
state to true. The implementation instances that implements the CloseableImplementation interface can determine 
whether it is closed according to the closed flag it holds, 
See [Implementation Lifecycle](https://github.com/shepherdviolet/glaciion/blob/master/docs/implementation-lifecycle.md)

```text
//Uninstall all services loaded by the default classloader, used to notify the implementation class to close when the system is stopped
Glaciion.uninstallDefaultClassloader();
//Uninstalled all services loaded by the specified classloader, invoke when the classloader needs to be destroyed at runtime
//Glaciion.uninstall(classloader);
```
