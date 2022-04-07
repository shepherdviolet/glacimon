# GlacimonSpi Implementation Lifecycle

```text
It can perceive while the implementation instance created or closed (service loader uninstalled). 
```

[Back to index](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/index.md)

<br>

## Listening for instance creation event

* Implements interface InitializableImplementation

```text
public class SampleServiceImpl implements SampleService, InitializableImplementation {

    @Override
    public void onServiceCreated() {
        //When the service instance is created, the method will be invoked (all properties have been injected), usually used for initialization.
    }
    
    @Override
    public String method() {
        //TO DO logic
    }
}
```

<br>

## Listening for instance closing event

* Implements interface CloseableImplementation

```text
public class SampleServiceImpl implements SampleService, CloseableImplementation {

    //Holding closed state (An AtomicBoolean), the value will become true when the service is closed
    //If there is an asynchronous thread in the service, you can use the closed state as a loop condition, and exit loop if true.
    private AtomicBoolean closed;
    
    @Override
    public void setCloseFlag(AtomicBoolean closed) {
        //Holding closed state
        this.closed = closed;
    }
    
    @Override
    public String method() {
        //Perceive whether the service is closed by determining if the closed state is true
        if (closed != null && closed.get()) {
            //closed
        } else {
            //not closed
        }
    }
}
```

* The closed state will become true when the service loader is uninstalled, 
See [About Cache](https://github.com/shepherdviolet/glacimon/blob/master/docs/spi/about-cache.md)
* The closed state will become true after the service loader's close method is invoked
