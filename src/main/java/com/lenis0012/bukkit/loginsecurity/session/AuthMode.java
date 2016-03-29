package com.lenis0012.bukkit.loginsecurity.session;

public enum AuthMode {
    /**
     * When the player is logged in.
     */
    AUTHENTICATED(null),
    /**
     * When the player registered but not logged in.
     */
    UNAUTHENTICATED("Please log in using /login <password>"),
    /**
     * When the player is not registered and not logged in.
     */
    UNREGISTERED("Please register using /register <password>");

    private final String authMessage;

    AuthMode(String authMessage) {
        this.authMessage = authMessage;
    }

    public boolean hasAuthMessage() {
        return authMessage != null;
    }

    public String getAuthMessage() {
        return authMessage;
    }
}
