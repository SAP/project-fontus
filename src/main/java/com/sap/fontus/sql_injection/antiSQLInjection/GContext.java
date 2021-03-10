package com.sap.fontus.sql_injection.antiSQLInjection;

import java.util.Map;

public interface GContext {

    void setVars(Map vars);
    Map getVars();

}
