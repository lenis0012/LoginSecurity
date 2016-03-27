package com.lenis0012.bukkit.loginsecurity.session;

import org.bukkit.entity.Player;

public abstract class AuthAction {
    private final AuthActionType type;
    private final AuthService service;
    private final Object serviceProvider;

    public <T> AuthAction(AuthActionType type, AuthService<T> service, T serviceProvider) {
        this.type = type;
        this.service = service;
        this.serviceProvider = serviceProvider;
    }

    public AuthActionType getType() {
        return type;
    }

    public AuthService getService() {
        return service;
    }

    protected Object getServiceProvider() {
        return serviceProvider;
    }

    public abstract AuthMode run(PlayerSession session);
}
