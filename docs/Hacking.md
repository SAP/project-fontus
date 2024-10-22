# Hacking on Fontus

## ASM Type Class

```java
Type t = Type.getType(String.class);
System.out.printf("ClassName: %s\n", t.getClassName());
System.out.printf("InternalName: %s\n", t.getInternalName());
System.out.printf("Descriptor: %s\n", t.getDescriptor());
System.out.printf("Size: %s\n", t.getSize());
```

Prints:
```text
ClassName: java.lang.String
InternalName: java/lang/String
Descriptor: Ljava/lang/String;
Size: 1
```