# Spit-DI

Spit is a lightweight dependency injection class for Java. Use it to:
 * Set fields annotated with @Resource
 * Call corresponding setField methods annotated with @Resource    

It's based on the singleton bindings in the container. Read how to use this for [Dependency Injection on Hadoop](http://paulmazak.blogspot.com/2015/06/dependency-injection-on-hadoop.html).

## Usage

In the class you wish to inject dependencies, annotate your fields or setters with javax.annotation.Resource.

```java
class Hello {
   @Resource
   private String message;
   @Resource
   private Integer number;
   
   private Set<String> items;
   @Resource
   private void setItems(Set<String> i) {
       this.items = i;
   }
}
```

Create a Spit container. Setup the bindings for your dependencies. Finally, inject will inject all classes in the container, including your `Hello` instance.

```java
class Main {
   public static void main(String[] args) {
      Hello hello = new Hello();
      SpitDI spit = new SpitDI();
      spit.bindByName(String.class, "message", "World")
          .bindByType(Integer.class, 4)
          .bindByName(Set.class, "items", new LinkedHashSet<String>())
          .inject(hello);
      System.out.println(hello.getMessage());
      System.out.println(hello.getNumber());
   }
}
```

## Install

Download the jar (it has only one class in it) and add as a dependency.

Gradle
```
dependencies {
    implementation files("libs/spit-di.jar")
}
```

## Package

```
javac SpitDI.java -d .
zip -r spit-di.jar com
```
