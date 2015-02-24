# Spit-DI

Spit is a lightweight dependency injection class for Java. It sets fields annotated with @Resource based on the singleton bindings in the container.

## Usage

In the class you wish to inject dependencies, annotate your fields with javax.annotation.Resource.

```java
class Hello {
   @Resource
   private String message;
   @Resource
   private Integer number;
   // getters...
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
      	  .inject(hello);
      System.out.println(hello.getMessage());
      System.out.println(hello.getNumber());
   }
}
```
