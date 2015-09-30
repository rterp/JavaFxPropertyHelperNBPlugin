# JavaFxPropertyHelper NetBeans Plugin

This NetBeans plugin will help to generate get/set methods for JavaFx properties that are contained
within a POJO.

The standard get/set code generator would create the following get/set methods for a JavaFx property:
```

private StringProperty name;
public StringProperty getName() { 
    return name;
}
public void setName( StringProperty stringProperty ) {
    name = stringProperty
}
```

However, what is desired for JavaFx properties would be.

```
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