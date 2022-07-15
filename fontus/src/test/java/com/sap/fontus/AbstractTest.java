package com.sap.fontus;

import com.sap.fontus.taintaware.IASTaintAware;

public abstract class AbstractTest {
    private final Tainter tainter = new Tainter();

    public static class Tainter {
        public void setTaint(IASTaintAware str, boolean taint) {
            str.setTaint(taint);
        }

        public boolean getTaint(IASTaintAware str) {
            return str.isTainted();
        }
    }

    protected Tainter getTaintChecker() {
        return this.tainter;
    }
}
