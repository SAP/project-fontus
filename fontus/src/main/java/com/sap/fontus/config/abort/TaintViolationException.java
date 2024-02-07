package com.sap.fontus.config.abort;

public class TaintViolationException extends RuntimeException {

    public TaintViolationException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }

    public TaintViolationException(String errorMessage) {
        super(errorMessage);
    }

}
