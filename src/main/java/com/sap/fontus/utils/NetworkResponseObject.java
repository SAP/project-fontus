package com.sap.fontus.utils;

import com.sap.fontus.agent.TaintAgent;
import com.sap.fontus.taintaware.unified.IASString;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NetworkResponseObject {
    public static void setResponseMessage(NetworkRequestObject reqObj, boolean sql_injected) throws RuntimeException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException {
        Class cls = TaintAgent.findLoadedClass("org.springframework.web.context.request.RequestContextHolder");
        Method reqAttributeMethod = cls.getMethod("getRequestAttributes");
        Object reqAttributeObject = reqAttributeMethod.invoke(null);
        Method respObjectMethod = reqAttributeObject.getClass().getMethod("getResponse");
        Object respObject = respObjectMethod.invoke(reqAttributeObject);

        if(sql_injected){
            respObject.getClass().getMethod("addHeader", IASString.class, IASString.class).invoke(respObject, new IASString("message"), new IASString("sql_injected"));
            respObject.getClass().getMethod("setHeader", IASString.class, IASString.class).invoke(respObject, new IASString("Content-Type"), new IASString("text/plain"));
            respObject.getClass().getMethod("setStatus", int.class).invoke(respObject, 418);
            PrintWriter pw_res = (PrintWriter) respObject.getClass().getMethod("getWriter").invoke(respObject);
            pw_res.getClass().getMethod("print",IASString.class).invoke(pw_res,new IASString(reqObj.getEncodedRequestBody()));
            pw_res.getClass().getMethod("flush").invoke(pw_res);
            pw_res.getClass().getMethod("close").invoke(pw_res);
            throw new InterruptedException("SQL Injection Error");
        }
        else{
            respObject.getClass().getMethod("addHeader", IASString.class, IASString.class).invoke(respObject, new IASString("message"), new IASString("sql_not_injected"));
        }
    }
}
