package com.sap.fontus.sql.driver;

import java.sql.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class tubsDriver implements Driver {
    private static final Driver instance = new tubsDriver();

    static {
        try {
            DriverManager.registerDriver(instance);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not register tubs driver with DriverManager", e);
        }
    }

    @Override
    public boolean acceptsURL(final String url) {
        return url != null && url.startsWith("jdbc:tubs:");
    }

    /**
     * Parses out the real JDBC connection URL by removing "tubs:".
     *
     * @param url the connection URL
     * @return the parsed URL
     */
    private String extractRealUrl(String url) {
        return this.acceptsURL(url) ? url.replace("tubs:", "") : url;
    }

    static List<Driver> registeredDrivers() {
        List<Driver> result = new ArrayList<>();
        for (Enumeration<Driver> driverEnumeration = DriverManager.getDrivers(); driverEnumeration.hasMoreElements(); ) {
            result.add(driverEnumeration.nextElement());
        }
        return result;
    }

    @Override
    public Connection connect(String url, Properties properties) throws SQLException {
        // if there is no url, we have problems
        if (url == null) {
            throw new SQLException("url is required");
        }

        if( !this.acceptsURL(url) ) {
            return null;
        }

        // find the real driver for the URL
        Driver passThru = this.findPassthru(url);

        final Connection conn;

        conn =  passThru.connect(this.extractRealUrl(url), properties);

        return ConnectionWrapper.wrap(conn);
    }

    private Driver findPassthru(String url) throws SQLException {

        String realUrl = this.extractRealUrl(url);
        Driver passthru = null;
        for (Driver driver: registeredDrivers() ) {
            try {
                if (driver.acceptsURL(this.extractRealUrl(url))) {
                    passthru = driver;
                    break;
                }
            } catch (SQLException e) {
                System.err.println("Error finding passthru: " + e);
            }
        }
        if( passthru == null ) {
            throw new SQLException("Unable to find a driver that accepts " + realUrl);
        }
        return passthru;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties properties) throws SQLException {
        return this.findPassthru(url).getPropertyInfo(url, properties);
    }

    @Override
    public int getMajorVersion() {
        // This is a bit of a problem since there is no URL to determine the passthru!
        return 1;
    }

    @Override
    public int getMinorVersion() {
        // This is a bit of a problem since there is no URL to determine the passthru!
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        // This is a bit of a problem since there is no URL to determine the passthru!
        return true;
    }

    // Note: @Override annotation not added to allow compilation using Java 1.6
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }
}
