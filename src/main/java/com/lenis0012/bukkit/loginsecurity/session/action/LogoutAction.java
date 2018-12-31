package com.lenis0012.bukkit.loginsecurity.session.action;

import com.lenis0012.bukkit.loginsecurity.session.*;
import com.lenis0012.bukkit.loginsecurity.util.MetaData;

public class LogoutAction extends AuthAction {

    public <T> LogoutAction(AuthService<T> service, T serviceProvider) {
        super(AuthActionType.LOGOUT, service, serviceProvider);
    }

    @Override
    public AuthMode run(PlayerSession session, ActionResponse response) {
        if(session.getPlayer() != null) {
            // Reset login time to prevent immediately getting kicked
            MetaData.set(session.getPlayer(), "ls_login_time", System.currentTimeMillis());
        }

        return AuthMode.UNAUTHENTICATED;
    }
}
