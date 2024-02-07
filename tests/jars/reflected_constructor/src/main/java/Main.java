import java.lang.reflect.*;

class Main {


    public static void main(String[] args) throws Exception {
        Widget w = ReflectionUtils.makeInstance(Widget.class);
        System.out.printf("%s%n", w);

    }

}
