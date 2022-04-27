package com.sap.fontus.sql.driver;

import java.sql.*;
import java.util.Enumeration;

public class TestApplication {
    private static String url = "";
    private static String user = "";
    private static String password="";
    private static Connection conn=null;

    public static void setup(){
        url="jdbc:tubs:mysql://localhost:3306/petclinic?useLegacyDatetimeCode=false&serverTimezone=Europe/Berlin";
        user="petclinic";
        password="petclinic";
    }

    private static void testUpdate() throws SQLException {
        final String updateOwner = "Update owner set first_name = ?, last_name = ? where id = 1";
        PreparedStatement ps = conn.prepareStatement(updateOwner);
        ps.setString(1, "Greg");
        ps.setString(2, "House");
        System.out.println(ps.toString());
    }

    private static void testInsert() throws SQLException {
        final String insertOwner = "Insert into owners (first_name, last_name, address, city, telephone) "
                + "VALUES(?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(insertOwner);
        ps.setNull(4, Types.INTEGER);
        System.out.println(ps.toString());
    }
    public static void main(String [] args){
        setup();
        try {
            Class.forName("de.tubs.ias.taintdriver.tubsDriver");
            System.out.println("Registered drivers: ");
            for (Enumeration<Driver> driverEnumeration = DriverManager.getDrivers(); driverEnumeration.hasMoreElements(); ) {
                System.out.println((driverEnumeration.nextElement().getClass().getName()));
            }
            System.out.println();
            conn = DriverManager.getConnection(url, user, password);
            testUpdate();
            testInsert();
        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
