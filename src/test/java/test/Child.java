package test;

import test2.Clazz;

public class Child extends Clazz {
    // Not overridden
    void toOverwriteClazz() {
        System.out.println("test2.Clazz called in test.Child");
    }

    @Override
    public void toOverwriteSuper() {
        System.out.println("test.Super called in test.Child");
    }

    public static void main(String[] args) {
        Child c = new Child();
        c.toOverwriteClazz();
        c.toOverwriteSuper();
    }
}
