package com.lenis0012.bukkit.loginsecurity.session;

import com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys;

import static com.lenis0012.bukkit.loginsecurity.LoginSecurity.translate;
import static com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys.MESSAGE_LOGIN;
import static com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys.MESSAGE_REGISTER;

public enum AuthMode {
    /**
     * When the player is logged in.
     */
    AUTHENTICATED(null),
    /**
     * When the player registered but not logged in.
     */
    UNAUTHENTICATED(MESSAGE_LOGIN),
    /**
     * When the player is not registered and not logged in.
     */
    UNREGISTERED(MESSAGE_REGISTER);

    private final String authMessage;

    AuthMode(LanguageKeys authMessage) {
        this.authMessage = authMessage != null ? translate(authMessage).toString() : null;
    }

    public boolean hasAuthMessage() {
        return authMessage != null;
    }

    public String getAuthMessage() {
        return authMessage;
    }
}
