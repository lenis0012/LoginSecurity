package com.lenis0012.bukkit.loginsecurity.session;

import com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys;

import static com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys.*;
import static com.lenis0012.bukkit.loginsecurity.LoginSecurity.translate;

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
        this.authMessage = translate(authMessage).toString();
    }

    public boolean hasAuthMessage() {
        return authMessage != null;
    }

    public String getAuthMessage() {
        return authMessage;
    }
}
