package com.lenis0012.bukkit.loginsecurity.session.action;

import com.lenis0012.bukkit.loginsecurity.session.*;

public class LogoutAction extends AuthAction {

    public <T> LogoutAction(AuthService<T> service, T serviceProvider) {
        super(AuthActionType.LOGOUT, service, serviceProvider);
    }

    @Override
    public AuthMode run(PlayerSession session, ActionResponse response) {
        return AuthMode.UNAUTHENTICATED;
    }
}
