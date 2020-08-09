package de.tubs.cs.ias.asm_test.utils;


import de.tubs.cs.ias.asm_test.asm.ClassResolver;
import org.objectweb.asm.Type;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ClassUtilsTest {

    @Test
    public void testGetAllMethods() throws NoSuchMethodException {
        Class<Super> zuper = Super.class;
        Class<Middle> middle = Middle.class;
        Class<Child> child = Child.class;
        Method childchildPublicMethod = child.getDeclaredMethod("childPublicMethod", Child.class);
        Method childchildProtectedMethod = child.getDeclaredMethod("childProtectedMethod", Child.class);
        Method childchildPrivateMethod = child.getDeclaredMethod("childPrivateMethod", Child.class);
        Method childmiddlePublicMethod = child.getDeclaredMethod("middlePublicMethod", Middle.class);
        Method childmiddlePublicNoOverridingMethod = child.getMethod("middlePublicMethodNoOverriding", Middle.class);
        Method childmiddleProtectedMethod = child.getDeclaredMethod("middleProtectedMethod", Middle.class);
        Method childsuperPublicMethod = child.getDeclaredMethod("superPublicMethod", Super.class);
        Method childsuperPublicNoOverridingMethod = child.getMethod("superPublicMethodNoOverriding", Super.class);
        Method childsuperProtectedMethod = child.getDeclaredMethod("superProtectedMethod", Super.class);
        Method childoverriddenThrough = child.getDeclaredMethod("overriddenThrough");

        Method supersuperPublicMethod = zuper.getDeclaredMethod("superPublicMethod", Super.class);
        Method supersuperProtectedMethod = zuper.getDeclaredMethod("superProtectedMethod", Super.class);
        Method supersuperPublicMethodNoOverriding = zuper.getDeclaredMethod("superPublicMethodNoOverriding", Super.class);
        Method supersuperProtectedMethodNoOverriding = zuper.getDeclaredMethod("superProtectedMethodNoOverriding", Super.class);
        Method supersuperPrivateMethodNoOverriding = zuper.getDeclaredMethod("superPrivateMethodNoOverriding", Super.class);
        Method superoverriddenThrough = zuper.getDeclaredMethod("overriddenThrough");

        Method middlemiddlePublicMethod = middle.getDeclaredMethod("middlePublicMethod", Middle.class);
        Method middlemiddleProtectedMethod = middle.getDeclaredMethod("middleProtectedMethod", Middle.class);
        Method middlemiddlePublicMethodNoOverriding = middle.getDeclaredMethod("middlePublicMethodNoOverriding", Middle.class);
        Method middlemiddleProtectedMethodNoOverriding = middle.getDeclaredMethod("middleProtectedMethodNoOverriding", Middle.class);
        Method middlemiddlePrivateMethodNoOverriding = middle.getDeclaredMethod("middlePrivateMethodNoOverriding", Middle.class);
        Method middleoverriddenThrough = middle.getDeclaredMethod("overriddenThrough");

        List<Method> included = new ArrayList<>();
        included.add(childchildPublicMethod);
        included.add(childchildProtectedMethod);
        included.add(childmiddlePublicMethod);
        included.add(childmiddlePublicNoOverridingMethod);
        included.add(childmiddleProtectedMethod);
        included.add(middlemiddleProtectedMethodNoOverriding);
        included.add(childsuperPublicMethod);
        included.add(childsuperPublicNoOverridingMethod);
        included.add(childsuperProtectedMethod);
        included.add(supersuperProtectedMethodNoOverriding);
        included.add(childoverriddenThrough);

        List<Method> excluded = new ArrayList<>();
        excluded.add(childchildPrivateMethod);
        excluded.add(supersuperPublicMethod);
        excluded.add(supersuperProtectedMethod);
        excluded.add(supersuperPrivateMethodNoOverriding);
        excluded.add(superoverriddenThrough);
        excluded.add(middlemiddlePublicMethod);
        excluded.add(middlemiddleProtectedMethod);
        excluded.add(middlemiddlePrivateMethodNoOverriding);
        excluded.add(middleoverriddenThrough);


        List<Method> methods = new ArrayList<>();
        ClassUtils.getAllMethods(Type.getType(Child.class).getInternalName(), new ClassResolver(ClassUtilsTest.class.getClassLoader()), methods);


        assertEquals(11, methods.size());
        for (Method i : included) {
            assertTrue(methods.contains(i), i + " not included");
        }

        assertEquals(childsuperPublicNoOverridingMethod, supersuperPublicMethodNoOverriding);
        assertEquals(childmiddlePublicNoOverridingMethod, middlemiddlePublicMethodNoOverriding);

        for (Method e : excluded) {
            assertFalse(methods.contains(e), e + " not excluded");
        }
    }

    static class Super {
        public void overriddenThrough() {

        }

        public void superPublicMethod(Super s) {

        }

        protected void superProtectedMethod(Super s) {

        }

        public void superPublicMethodNoOverriding(Super s) {

        }

        protected void superProtectedMethodNoOverriding(Super s) {

        }

        private void superPrivateMethodNoOverriding(Super s) {

        }
    }

    static class Middle extends Super {
        @Override
        public void overriddenThrough() {

        }

        public void middlePublicMethod(Middle m) {

        }

        protected void middleProtectedMethod(Middle m) {

        }

        public void middlePublicMethodNoOverriding(Middle m) {

        }

        protected void middleProtectedMethodNoOverriding(Middle m) {

        }

        private void middlePrivateMethodNoOverriding(Middle m) {

        }
    }

    static class Child extends Middle {
        @Override
        public void overriddenThrough() {

        }

        public void childPublicMethod(Child c) {

        }

        protected void childProtectedMethod(Child c) {

        }

        private void childPrivateMethod(Child c) {

        }

        @Override
        public void middlePublicMethod(Middle m) {

        }

        @Override
        public void superPublicMethod(Super s) {

        }

        @Override
        protected void superProtectedMethod(Super s) {

        }

        @Override
        protected void middleProtectedMethod(Middle m) {

        }
    }
}
