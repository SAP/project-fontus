package de.tubs.cs.ias.asm_test.sql_injection.antiSQLInjection;

import java.util.Map;

public interface GContext {

    void setVars(Map vars);
    Map getVars();

}
