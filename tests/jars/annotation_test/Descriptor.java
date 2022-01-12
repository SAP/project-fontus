import java.lang.annotation.Target;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Descriptor {

    String   value();
    String   name();
    int      age();
    String[] newNames();
    Class clazz() default String.class;
    Class defaultClazz() default String.class;
}
