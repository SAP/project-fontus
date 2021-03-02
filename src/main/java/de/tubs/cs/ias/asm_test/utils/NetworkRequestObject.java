package de.tubs.cs.ias.asm_test.utils;

import de.tubs.cs.ias.asm_test.agent.TaintAgent;
import de.tubs.cs.ias.asm_test.taintaware.lazybasic.IASString;
import org.json.Cookie;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NetworkRequestObject {

    private final Object reqObject;

    public NetworkRequestObject() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class cls = TaintAgent.findLoadedClass("org.springframework.web.context.request.RequestContextHolder");
        Method reqAttributeMethod = cls.getMethod("getRequestAttributes");
        Object reqAttributeObject = reqAttributeMethod.invoke(null);
        Method reqObjectMethod = reqAttributeObject.getClass().getMethod("getRequest");
        this.reqObject = reqObjectMethod.invoke(reqAttributeObject);
    }

    public String getHeaderByName(String header_name) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        IASString header = (IASString) this.reqObject.getClass().getMethod("getHeader", IASString.class).invoke(this.reqObject, new IASString(header_name));
        return header.toString();
    }

    public String getServletPath() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        IASString header = (IASString) this.reqObject.getClass().getMethod("getServletPath").invoke(this.reqObject);
        return header.toString();
    }

    public JSONArray getCookies() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        IASString cookies_temp = (IASString) this.reqObject.getClass().getMethod("getHeader", IASString.class).invoke(this.reqObject, new IASString("cookie"));
        String cookies_string = cookies_temp.toString();
        String[] rawCookieParams = cookies_string.split(";");
        JSONArray cookies = new JSONArray();
        for (String cookie:rawCookieParams){
            cookies.put(Cookie.toJSONObject(cookie));
        }
        return cookies;
    }

    public String getCookieByName(String cookie_name) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        IASString cookies_temp = (IASString) this.reqObject.getClass().getMethod("getHeader", IASString.class).invoke(this.reqObject, new IASString("cookie"));
        String cookies_string = cookies_temp.toString();
        String[] rawCookieParams = cookies_string.split(";");
        for (String cookie:rawCookieParams){
            if(Cookie.toJSONObject(cookie).get("name").equals(cookie_name)){
                return (String) Cookie.toJSONObject(cookie).get("value");
            }
        }
        return null;
    }

    public static void setResponseMessage(boolean sql_injection) throws RuntimeException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException {
        Class cls = TaintAgent.findLoadedClass("org.springframework.web.context.request.RequestContextHolder");
        Method reqAttributeMethod = cls.getMethod("getRequestAttributes");
        Object reqAttributeObject = reqAttributeMethod.invoke(null);
        Method respObjectMethod = reqAttributeObject.getClass().getMethod("getResponse");
        Object respObject = respObjectMethod.invoke(reqAttributeObject);
        if(sql_injection){
            respObject.getClass().getMethod("addHeader", IASString.class, IASString.class).invoke(respObject, new IASString("message"), new IASString("sql_not_injected"));
        }
        else{
            respObject.getClass().getMethod("addHeader", IASString.class, IASString.class).invoke(respObject, new IASString("message"), new IASString("sql_injected"));
            respObject.getClass().getMethod("sendError", int.class).invoke(respObject, 500);
            throw new InterruptedException("SQL Injection Error");
            //Thread.currentThread().stop();
            //System.exit(1);
        }
    }
}
