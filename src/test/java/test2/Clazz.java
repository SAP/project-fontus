package test2;

import test.Super;

public class Clazz extends Super {
    void toOverwriteClazz() {
        System.out.println("test2.Clazz called in test2.Clazz");
    }
}
