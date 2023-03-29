package com.sap.fontus.gdpr.servlet;

import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASStringBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

public class ReflectedHttpServletRequest extends ReflectedObject {

    public ReflectedHttpServletRequest(Object object) {
        super(object);
    }

    public IASString getAuthType() {
        return (IASString) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod());
    }

    
    public ReflectedCookie[] getCookies() {
        return ReflectedCookie.reflectedArray(this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod()));
    }

    
    public long getDateHeader(String name) {
        return 0L;
    }

    
    public String getHeader(String name) {
        return null;
    }

    
    public Enumeration<?> getHeaders(String name) {
        return null;
    }

    
    public Enumeration<?> getHeaderNames() {
        return null;
    }

    
    public int getIntHeader(String name) {
        return 0;
    }

    
    public IASString getMethod() {
        return (IASString) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod());
    }

    public String getMethodString() {
        return getMethod().getString();
    }
    
    public IASString getPathInfo() {
        return (IASString) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod());
    }

    
    public String getPathTranslated() {
        return null;
    }

    
    public String getContextPath() {
        return null;
    }

    
    public String getQueryString() {
        return null;
    }

    
    public String getRemoteUser() {
        return null;
    }

    
    public boolean isUserInRole(String role) {
        return false;
    }

    
    public Principal getUserPrincipal() {
        return null;
    }

    
    public String getRequestedSessionId() {
        return null;
    }


    public IASString getRequestURI() {
        return (IASString) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod());
    }

    public String getRequestURIString() {
        return this.getRequestURI().getString();
    }
    
    public IASStringBuffer getRequestURL() {
        return (IASStringBuffer) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod());
    }

    
    public String getServletPath() {
        return null;
    }

    
//    public HttpSession getSession(boolean create) {
//        return (HttpSession) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod(), create);
//    }
//
//
    public ReflectedSession getSession() {
        return new ReflectedSession(this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod()));
    }

    
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    public Object getAttribute(String name) {
        if (name == null) {
            return null;
        }
        return this.getAttribute(new IASString(name));
    }
    
    public Object getAttribute(IASString name) {
        return this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod(), name);
    }

    public Enumeration<?> getAttributeNames() {
        return (Enumeration<?>) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod());
    }

    
    public String getCharacterEncoding() {
        return null;
    }

    
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {

    }

    
    public int getContentLength() {
        return 0;
    }

    
    public String getContentType() {
        return null;
    }

    

    public String getParameter(String name) {
	if (name == null) {
	    return null;
	}
	IASString result = this.getParameter(new IASString(name));
	if (result == null) {
	    return null;
	}
        return result.getString();
    }
    
    public IASString getParameter(IASString name) {
        return (IASString) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod(), name);
    }

    
    public Enumeration<?> getParameterNames() {
        return (Enumeration<?>) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod());
    }

    
    public IASString[] getParameterValues(IASString name) {
        return (IASString[]) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod(), name);
    }

    
    public Map<Object, Object> getParameterMap() {
        return (Map<Object, Object>) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod());
    }

    
    public String getProtocol() {
        return null;
    }

    
    public String getScheme() {
        return null;
    }

    
    public String getServerName() {
        return null;
    }

    
    public int getServerPort() {
        return 0;
    }

    
    public BufferedReader getReader() throws IOException {
        return null;
    }

    
    public String getRemoteAddr() {
        return null;
    }

    
    public String getRemoteHost() {
        return null;
    }

    
    public void setAttribute(String name, Object o) {
        this.setAttribute(new IASString(name), o);
    }

    public void setAttribute(IASString name, Object o) {
        this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod(), name, o);
    }
    
    public void removeAttribute(String name) {

    }

    
    public Locale getLocale() {
        return null;
    }

    
    public Enumeration<?> getLocales() {
        return null;
    }

    
    public boolean isSecure() {
        return false;
    }

    
//    public RequestDispatcher getRequestDispatcher(String path) {
//        return null;
//    }

    
    public String getRealPath(String path) {
        return null;
    }

    
    public int getRemotePort() {
        return 0;
    }

    
    public String getLocalName() {
        return null;
    }

    
    public String getLocalAddr() {
        return null;
    }

    
    public int getLocalPort() {
        return 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
	sb.append("ReflectedHttpServletRequest:  ");
	if (this.o == null) {
	    sb.append("Object is null!");
	} else {
	    sb.append("URL: " + this.getRequestURL());
	    sb.append(System.getProperty("line.separator"));

	    sb.append("PathInfo: " + this.getPathInfo());
	    sb.append(System.getProperty("line.separator"));

	    sb.append("URI: " + this.getRequestURI());
	    sb.append(System.getProperty("line.separator"));

	    Enumeration<?> e = this.getParameterNames();
	    sb.append("Query Parameters:");
	    sb.append(System.getProperty("line.separator"));
	    if (e == null) {
		sb.append("NULL");
	    } else {
		while (e.hasMoreElements()) {
		    IASString s = (IASString) e.nextElement();
		    sb.append(s.getString() + " = ");
		    for (IASString value : this.getParameterValues(s)) {
			sb.append(value.getString() + ", ");
		    }
		    sb.append(System.getProperty("line.separator"));
		}
	    }
	    sb.append(System.getProperty("line.separator"));

	    Enumeration<?> a = this.getAttributeNames();
	    sb.append("Attributes:");
	    sb.append(System.getProperty("line.separator"));
	    if (a == null) {
		sb.append("NULL");
	    } else {
		while (a.hasMoreElements()) {
		    IASString s = (IASString) a.nextElement();
		    sb.append(s.getString() + " = " + this.getAttribute(s));
		    sb.append(System.getProperty("line.separator"));
		}
	    }
	    ReflectedCookie[] cookies = this.getCookies();
	    sb.append("Cookies: " + Arrays.toString(cookies));
	    sb.append(System.getProperty("line.separator"));
	    if (cookies != null) {
		for (ReflectedCookie cookie : cookies) {
		    sb.append(cookie.toString());
		    sb.append(System.getProperty("line.separator"));
		}
	    }
	}
        return sb.toString();
    }
}
