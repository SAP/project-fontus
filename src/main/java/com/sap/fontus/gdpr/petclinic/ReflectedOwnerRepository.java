package com.sap.fontus.gdpr.petclinic;

import com.sap.fontus.agent.TaintAgent;
import com.sap.fontus.gdpr.servlet.ReflectedObject;

public class ReflectedOwnerRepository extends ReflectedObject {

    protected ReflectedOwnerRepository(Object o) {
        super(o);
    }

//    protected ReflectedOwnerRepository(String repositoryName) {
//        Class cls = TaintAgent.findLoadedClass("org.springframework.web.context.request.RequestContextHolder");
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
