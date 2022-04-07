# Glaciion Property Inject

```text
Inject properties into the implementation instance.
It chooses which properties to apply based on the priority in the properties file. 
The highest priority will be applied, and the other will not take effect.
```

[Back to index](https://github.com/shepherdviolet/glaciion/blob/master/docs/index.md)

<br>

## Inject properties into implementation

* Both `single-service` mode and `multiple-service` mode implementation support property injection.
* Data type: String/boolean/Boolean/int/Integer/long/Long/float/Float/double/Double

### 1.Implementation class

```text
package sample;
public class SampleServiceImpl implements SampleService {

    /**
     * Inject to field, the property name is the field name (In this example it's "dataFormat"). 
     */
    @PropertyInject
    private String dateFormat;

    /**
     * Inject by method, the method name must be in standard Getter format (setXxxXxx), and there can only be one parameter. 
     * The property name is determined by the method name, camel case, initial letter lowercase (In this example it's "logEnabled"). 
     */
    @PropertyInject
    public void setLogEnabled(boolean value){
        //TO DO logic
    }
    
    /**
     * Advanced usage: getVmOptionFirst
     * Get the property from the VM option first, if it does not exist, get from the properties file.
     * The VM option name is determined by the value of getVmOptionFirst, in this example it's "sample.service.id". 
     * That is, set by "-Dsample.service.id=1". 
     * The property name in the properties file is still determined by the method name, in this example it's "serviceId". 
     * That is, set by "serviceId=1". 
     */
    @PropertyInject(getVmOptionFirst = "sample.service.id")
    public void setServiceId(int value) {
        //TO DO logic
    }

    @Override
    public String method() {
        //TO DO logic
    }

}
```

### 2.Properties file

* Add file `META-INF/glaciion/properties/sample.SampleServiceImpl`
* Contents:

```text
dateFormat=yyyy-MM-dd HH:mm:ss
logEnabled=true
serviceId=1
```

* Properties file path: META-INF/glaciion/properties/`implementation-classname`
* The properties file content is the standard properties format.

<br>

## Adjust properties by VM options

### When @PropertyInject specifies `getVmOptionFirst`

* Add VM option: -D`getVmOptionFirst-value`=`property-value`
* Example: -Dsample.service.id=2

### General way

* Add VM option: -Dglaciion.property.`implementation-classname`.`property-name`=`property-value`
* Example: -Dglaciion.property.sample.SampleServiceImpl.serviceId=2

<br>

## Selection mechanism

* Only one properties file will be loaded, and the property in other files will be invalid.
* In the properties file, adjust it's priority by adding the `@priority` property.
* The higher the priority value, the higher the priority, the highest priority properties file will be loaded. the default will be 0 if not set.
* The VM option (glaciion.property) adjust the properties based on the selected properties file. 
* `TIPS: When there is more than one properties file with the highest priority, which one is loaded according to the hash 
of the content of the properties file`

### Example

* Properties file 1 (Fail to be selected):

```text
dateFormat=yyyy-MM-dd HH:mm:ss
logEnabled=true
serviceId=1
```

* Properties file 2 (Fail to be selected):

```text
@priority=1
dateFormat=yyyy-MM-dd HH:mm:ss
logEnabled=true
serviceId=2
```

* Properties file 3 (Selected):

```text
@priority=2
dateFormat=yyyy-MM-dd HH:mm:ss.SSS
logEnabled=true
serviceId=3
```

* VM options:

```text
-Dglaciion.property.sample.SampleServiceImpl.logEnabled=false
-Dsample.service.id=4
```

* Finally, the injected properties are:
* `dateFormat=yyyy-MM-dd HH:mm:ss.SSS`
* `logEnabled=false`
* `serviceId=4`
* The first property is determined by the highest priority properties file
* The last two property are determined by the VM options
