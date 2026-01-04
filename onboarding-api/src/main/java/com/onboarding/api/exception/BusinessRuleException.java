package com.onboarding.api.exception;

public class BusinessRuleException extends RuntimeException {
    private String errorCode;

    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
