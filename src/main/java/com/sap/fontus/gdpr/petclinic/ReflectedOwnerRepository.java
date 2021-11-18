package com.sap.fontus.gdpr.petclinic;

import com.sap.fontus.agent.TaintAgent;
import com.sap.fontus.gdpr.servlet.ReflectedHttpServletRequest;
import com.sap.fontus.gdpr.servlet.ReflectedObject;

import java.lang.reflect.Method;

public class ReflectedOwnerRepository  {

    public static void dumpRequestContextHolder(ReflectedHttpServletRequest request) {
        Class cls = TaintAgent.findLoadedClass("org.springframework.web.context.request.RequestContextHolder");
        try {
            Method m = cls.getMethod("getRequest");
            Object reqAttributeObject = ReflectedObject.callMethodWithReflection(cls, m);
            Method m2 = reqAttributeObject.getClass().getMethod("getAttributeNames");
            String[] names = (String[]) ReflectedObject.callMethodWithReflection(reqAttributeObject.getClass(), m2);
            for (String s : names) {
                System.out.println(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    protected ReflectedOwnerRepository() {
//
//        Method reqAttributeMethod = cls.getMethod("getRequestAttributes");
//        Object reqAttributeObject = reqAttributeMethod.invoke(null);
//        Method respObjectMethod = reqAttributeObject.getClass().getMethod("getResponse");
//        Object respObject = respObjectMethod.invoke(reqAttributeObject);
//
//        super(o);
//    }
//
//    @Entity
//    public class Account {
//        //your code
//
//        public void doAccountRepositoryStuff() {
//            AccountRepository accountRepository = (AccountRepository) SpringConfiguration.contextProvider().getApplicationContext().getBean("accountRepository");
//            // Do your own stuff with accountRepository here...
//        }
//    }
}
