package com.lenis0012.bukkit.loginsecurity.session.action;

public class ActionResponse {
    private final boolean success;
    private final String errorMessage;

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
}
