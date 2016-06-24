package com.lenis0012.bukkit.loginsecurity.session.action;

public class ActionResponse {
    private boolean success;
    private String errorMessage;

    public ActionResponse() {
        this.success = true;
    }

    public ActionResponse(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    protected void setSuccess(boolean success) {
        this.success = success;
    }

    protected void setErrorMessage(String message) {
        this.errorMessage = message;
    }
}
