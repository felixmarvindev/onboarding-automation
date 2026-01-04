package com.onboarding.events;

public abstract class BaseFailureEvent extends BaseEvent {
    private String errorCode;
    private String errorMessage;
    private int retryCount;

    protected BaseFailureEvent() {
        super();
    }

    protected BaseFailureEvent(String requestId, String errorCode, String errorMessage, int retryCount) {
        super(requestId);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.retryCount = retryCount;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}
