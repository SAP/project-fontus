package com.sap.fontus.gdpr.servlet;

import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASStringBuffer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

public class ReflectedHttpServlet extends ReflectedObject {

    public ReflectedHttpServlet(Object object) {
        super(object);
    }

    public IASString getAuthType() {
        return (IASString) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod());
    }

    
    public ReflectedCookie[] getCookies() {
        return ReflectedCookie.reflectedArray(this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod()));
    }

    
    public long getDateHeader(String name) {
        return 0;
    }

    
    public String getHeader(String name) {
        return null;
    }

    
    public Enumeration getHeaders(String name) {
        return null;
    }

    
    public Enumeration getHeaderNames() {
        return null;
    }

    
    public int getIntHeader(String name) {
        return 0;
    }

    
    public String getMethod() {
        return null;
    }

    
    public String getPathInfo() {
        return null;
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

    
    public String getRequestURI() {
        return null;
    }

    
    public IASStringBuffer getRequestURL() {
        return (IASStringBuffer) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod());
    }

    
    public String getServletPath() {
        return null;
    }

    
    public HttpSession getSession(boolean create) {
        return (HttpSession) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod(), create);
    }

    
    public HttpSession getSession() {
        return (HttpSession) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod());
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
        return null;
    }

    
    public Enumeration getAttributeNames() {
        return null;
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

    
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    
    public IASString getParameter(IASString name) {
        return (IASString) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod(), name);
    }

    
    public Enumeration getParameterNames() {
        return (Enumeration) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod());
    }

    
    public IASString[] getParameterValues(IASString name) {
        return (IASString[]) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod(), name);
    }

    
    public Map getParameterMap() {
        return (Map) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod());
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

    }

    
    public void removeAttribute(String name) {

    }

    
    public Locale getLocale() {
        return null;
    }

    
    public Enumeration getLocales() {
        return null;
    }

    
    public boolean isSecure() {
        return false;
    }

    
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    
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
}
