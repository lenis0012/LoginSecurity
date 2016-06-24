package com.lenis0012.bukkit.loginsecurity.session.exceptions;

public class ProfileRefreshException extends Exception {
    public ProfileRefreshException() {
        super();
    }

    public ProfileRefreshException(String message) {
        super(message);
    }

    public ProfileRefreshException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProfileRefreshException(Throwable cause) {
        super(cause);
    }

    protected ProfileRefreshException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
