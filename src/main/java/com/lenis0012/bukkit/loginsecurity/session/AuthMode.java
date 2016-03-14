package com.lenis0012.bukkit.loginsecurity.session;

public enum AuthMode {
    /**
     * When the player is logged in.
     */
    AUTHENTICATED,
    /**
     * When the player registered but not logged in.
     */
    UNAUTHENTICATED,
    /**
     * When the player is not registered and not logged in.
     */
    UNREGISTERED
}
