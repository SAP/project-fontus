import java.lang.reflect.Proxy;

class ProxyLambdaTest {

    interface A {
        public int getA();
    }

    interface B {
        public int getB();
    }

    class ABImpl implements A, B {

        public int getA() { return 1; }

        public int getB() { return 2; }
    }
    
    public static void main(String[] args) {
	ClassLoader cl = ProxyLambdaTest.class.getClassLoader();
	ABImpl ab = new ProxyLambdaTest().new ABImpl();
	Object abProxy = Proxy.newProxyInstance(cl, new Class[] { A.class, B.class },
						(proxy, method, methodArgs) -> {
						    return method.invoke(ab, methodArgs);
						}
						);
	A a = (A)abProxy;
	B b = (B)abProxy;
	System.out.println("Hello " + a.getA()); // Display the string.
    }

}
