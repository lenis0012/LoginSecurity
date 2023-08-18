package com.lenis0012.bukkit.loginsecurity.session.action;

import com.lenis0012.bukkit.loginsecurity.session.AuthAction;
import com.lenis0012.bukkit.loginsecurity.session.AuthActionType;
import com.lenis0012.bukkit.loginsecurity.session.AuthMode;
import com.lenis0012.bukkit.loginsecurity.session.AuthService;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;

public class BypassAction extends AuthAction {

    public <T> BypassAction(AuthService<T> service, T serviceProvider) {
        super(AuthActionType.BYPASS, service, serviceProvider);
    }

    @Override
    public AuthMode run(PlayerSession session, ActionResponse response) {
        return AuthMode.AUTHENTICATED;
    }
}
