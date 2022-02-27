package com.sap.fontus.utils;

import com.sap.fontus.taintaware.unified.IASString;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NetworkResponseObject {

    private static Object getResponse() {
        Object respObject = null;

        try {
            Class cls = InstrumentationFactory.createClassFinder().findClass("org.springframework.web.context.request.RequestContextHolder");
            Method reqAttributeMethod = cls.getMethod("getRequestAttributes");
            Object reqAttributeObject = reqAttributeMethod.invoke(null);
            Method respObjectMethod = reqAttributeObject.getClass().getMethod("getResponse");
            respObject = respObjectMethod.invoke(reqAttributeObject);
        } catch (IllegalArgumentException | SecurityException | IllegalAccessException |
                 NoSuchMethodException | InvocationTargetException e) {
            System.out.println("Error getting Spring response object via reflection!");
            System.out.println(e.toString());
        }

        return respObject;
    }

    private static void setSqlDetectedResponse(NetworkRequestObject reqObj) {
        Object respObject = getResponse();
        if (respObject != null) {
            try {
                respObject.getClass().getMethod("addHeader", IASString.class, IASString.class).invoke(respObject, new IASString("message"), new IASString("sql_injected"));
                respObject.getClass().getMethod("setHeader", IASString.class, IASString.class).invoke(respObject, new IASString("Content-Type"), new IASString("text/plain"));
                respObject.getClass().getMethod("setStatus", int.class).invoke(respObject, 418);
                PrintWriter pw_res = (PrintWriter) respObject.getClass().getMethod("getWriter").invoke(respObject);
                pw_res.getClass().getMethod("print",IASString.class).invoke(pw_res,new IASString(reqObj.getEncodedRequestBody()));
                pw_res.getClass().getMethod("flush").invoke(pw_res);
                pw_res.getClass().getMethod("close").invoke(pw_res);
            } catch (IllegalArgumentException | SecurityException | IllegalAccessException |
                 NoSuchMethodException | InvocationTargetException e) {
                System.out.println("Error getting Spring response object via reflection!");
                System.out.println(e.toString());
            }
        }
    }

    private static void setNoSqlDetectedResponse() {
        Object respObject = getResponse();
        if (respObject != null) {
            try {
                respObject.getClass().getMethod("addHeader", IASString.class, IASString.class).invoke(respObject, new IASString("message"), new IASString("sql_not_injected"));
            } catch (IllegalArgumentException | SecurityException | IllegalAccessException |
                 NoSuchMethodException | InvocationTargetException e) {
                System.out.println("Error getting Spring response object via reflection!");
                System.out.println(e.toString());
            }
        }
    }

    public static void setResponseMessage(NetworkRequestObject reqObj, boolean sql_injected) {
        if (sql_injected) {
            setSqlDetectedResponse(reqObj);
            throw new RuntimeException("SQL Injection Error");
        } else {
            setNoSqlDetectedResponse();
        }
    }
}
