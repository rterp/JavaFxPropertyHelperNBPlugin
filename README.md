# JavaFxPropertyHelper NetBeans Plugin

This NetBeans plugin will help to generate get/set methods for JavaFx properties that are contained
within a POJO.

The standard get/set code generator creates the following get/set methods for a JavaFx property which is not ideal:

```java
private StringProperty name;
public StringProperty getName() { 
    return name;
}
public void setName( StringProperty stringProperty ) {
    name = stringProperty
}
```

This plugin would create the following methods.

```java
public String getName() {
    return name.get();
}

public void setName( String string ) {
    name.set(string);
}

public StringProperty nameProperty() {
    return name;
}
```

#Usage

Press Alt-Insert to get the "Generate" popup menu, and select "JavaFx Props Getters and Setters"
![alt tag](https://rterp.files.wordpress.com/2015/09/ubuntu1.png)

<br><br><br>

Methods for supported property types will automatically be generated.
![alt tag](https://rterp.files.wordpress.com/2015/09/ubuntu2.png)


### Supported Property Types
* StringProperty
* BooleanProperty
* DoubleProperty
* FloatProperty
* IntegerProperty
* LongProperty

<br>
### Unsupported Property Types
* ListProperty
* MapProperty
* ObjectProperty
* SetProperty
