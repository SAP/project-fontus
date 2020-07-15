package outerpackage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Anno {
    String value() default "Hello";

    String[] array() default {"Hello", "World"};
}
