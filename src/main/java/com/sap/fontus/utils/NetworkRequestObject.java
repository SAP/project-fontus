package com.sap.fontus.utils;

import com.sap.fontus.agent.TaintAgent;
import com.sap.fontus.taintaware.unified.IASString;
import org.json.Cookie;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Map;

public class NetworkRequestObject implements Serializable {

    private final Object reqObject;

    private static Object fillRequestObject() {
        Object req = null;
        try {
            Class cls = TaintAgent.findLoadedClass("org.springframework.web.context.request.RequestContextHolder");
            Method reqAttributeMethod = cls.getMethod("getRequestAttributes");
            Object reqAttributeObject = reqAttributeMethod.invoke(null);
            Method reqObjectMethod = reqAttributeObject.getClass().getMethod("getRequest");
            req = reqObjectMethod.invoke(reqAttributeObject);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            System.err.println("Error getting Spring HTTP request via reflection");
        }
        return req;
    }
    
    public NetworkRequestObject() {
        this.reqObject = fillRequestObject();
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

    public String getEncodedRequestBody() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Main request json
        JSONObject req_json = new JSONObject();

        // Construct Method in json
        String method = this.reqObject.getClass().getMethod("getMethod").invoke(this.reqObject).toString();
        req_json.put("method", method);

        // Construct Path in json
        String path = this.reqObject.getClass().getMethod("getServletPath").invoke(this.reqObject).toString();
        req_json.put("path", path);

        // Construct Path in json
        String protocol = this.reqObject.getClass().getMethod("getProtocol").invoke(this.reqObject).toString();
        req_json.put("protocol", protocol);

        // Construct Headers in json
        Enumeration<IASString> header_names = (Enumeration<IASString>) this.reqObject.getClass().getMethod("getHeaderNames").invoke(this.reqObject);
        JSONArray headers = new JSONArray();
        if (header_names != null) {
            JSONObject header = new JSONObject();
            while (header_names.hasMoreElements()) {
                IASString header_name_ias = header_names.nextElement();
                String header_name = header_name_ias.toString();
                String header_value = this.reqObject.getClass().getMethod("getHeader", IASString.class).invoke(this.reqObject, header_name_ias).toString();
                header.put(header_name, header_value);
            }
            headers.put(header);
        }
        req_json.put("headers", headers);

        // Construct Parameters in json
        JSONArray parameters = new JSONArray();
        Map<IASString, IASString[]> parameter_names = (Map<IASString, IASString[]>) this.reqObject.getClass().getMethod("getParameterMap").invoke(this.reqObject);
        for (IASString key : parameter_names.keySet()) {
            IASString[] strArr = parameter_names.get(key);
            for (IASString val : strArr) {
                JSONObject parameter = new JSONObject();
                parameter.put(key.toString(), val.toString());
                parameters.put(parameter);
            }
        }
        req_json.put("parameters", parameters);

        //Construct body if present
        StringBuilder bb = new StringBuilder();
        String line;
        try {
            BufferedReader bbr = (BufferedReader) this.reqObject.getClass().getMethod("getReader").invoke(this.reqObject);
            while ((line = bbr.readLine()) != null) {
                bb.append(line);
            }
        }
        catch (Exception ignored){}
        req_json.put("body",bb.toString());

        String encoded_req = Base64.getEncoder().encodeToString(req_json.toString().getBytes(StandardCharsets.UTF_8));

//        byte[] decodedBytes = Base64.getDecoder().decode(encoded_req);
//        String decodedString = new String(decodedBytes);
//        System.out.println(decodedString);

        return encoded_req;
    }
}

/*    // testing
    //ObjectMapper objMapper = new ObjectMapper();
    NetworkRequestObject reqObject2 = new NetworkRequestObject();
        try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(reqObject2);
                oos.flush();
                byte[] yourBytes = bos.toByteArray();
                //System.out.println(objMapper.writeValueAsString(reqObject));
                } catch (IOException e) {
                e.printStackTrace();
                }
//
 */
