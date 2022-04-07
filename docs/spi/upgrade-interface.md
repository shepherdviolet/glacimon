# GlacimonSpi Upgrade Interface

```text
The interface should be kept as constant as possible. If the method is modified, implementations based on the old version 
interface will be incompatible. In order to meet the interface upgrade requirements, GlacimonSpi supports adding methods to 
the interface, and provide a backward compatible approach.
```

[Back to index](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/index.md)

<br>

## Add methods

* Add the @NewMethod annotation to the new method, and configure a `CompatibleApproach'
* If the implementation does not implement the method, GlacimonSpi will generate a compatible proxy for it. When invoking the 
unimplemented method, it will be handled by the `CompatibleApproach`.

```text
@SingleServiceInterface
//@MultipleServiceInterface
public interface SampleService {

    String oldMethod(String param);

    /**
     * If the implementation does not implement the method, it returns null directly when invoked, and does nothing
     */
    @NewMethod(compatibleApproach = DoNothing.class)
    String newMethod1(String param);

    /**
     * If the implementation does not implement the method, invocation will be handled by the NewMethodCompat class when invoked
     */
    @NewMethod(compatibleApproach = NewMethodCompat.class)
    String newMethod2(String param);

    class NewMethodCompat implements CompatibleApproach {
        @Override
        public Object onInvoke(Class<?> serviceInterface, Object serviceInstance, Method method, Object[] params) throws Throwable {
            //When newMethod2 is invoked, oldMethod is actually invoked
            return ((SampleService)serviceInstance).oldMethod(params[0]);
        }
    }

}
```
