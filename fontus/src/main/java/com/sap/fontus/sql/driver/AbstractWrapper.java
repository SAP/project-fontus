package com.sap.fontus.sql.driver;

import java.sql.SQLException;
import java.sql.Wrapper;

public abstract class AbstractWrapper implements Wrapper {

    private final Object delegate;

    protected AbstractWrapper(Object delegate) {
        this.delegate = delegate;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        final Object result;
        if (iface.isAssignableFrom(this.getClass())) {
            // if the proxy directly implements the interface or extends it, return the proxy
            result = this;
        } else if (iface.isAssignableFrom(this.delegate.getClass())) {
            // if the proxied object directly implements the interface or extends it, return
            // the proxied object
            result = this.unwrapProxy();
        } else if (Wrapper.class.isAssignableFrom(this.delegate.getClass())) {
            // if the proxied object implements the wrapper interface, then
            // return the result of it's unwrap method.
            result = ((Wrapper) this.unwrapProxy()).unwrap(iface);
        } else {
      /*
         This line of code can only be reached when the underlying object does not implement the wrapper
         interface.  This would mean that either the JDBC driver or the wrapper of the underlying object
         does not implement the JDBC 4.0 API.
       */
            throw new SQLException("Can not unwrap to " + iface.getName());
        }
        return iface.cast(result);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        if (iface.isAssignableFrom(this.getClass())) {
            // if the proxy directly proxy the interface or extends it, return true
            return true;
        } else if (iface.isAssignableFrom(this.delegate.getClass())) {
            // if the proxied object directly implements the interface or extends it, return true
            return true;
        } else if (Wrapper.class.isAssignableFrom(this.delegate.getClass())) {
            // if the proxied object implements the wrapper interface, then
            // return the result of it's isWrapperFor method.
            return ((Wrapper) this.unwrapProxy()).isWrapperFor(iface);
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return this.delegate.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.delegate.hashCode();
    }

    public Object unwrapProxy() {
        return this.delegate;
    }
}

