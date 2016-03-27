package com.lenis0012.bukkit.loginsecurity.session.action;

import com.lenis0012.bukkit.loginsecurity.session.AuthAction;
import com.lenis0012.bukkit.loginsecurity.session.AuthActionType;
import com.lenis0012.bukkit.loginsecurity.session.AuthMode;
import com.lenis0012.bukkit.loginsecurity.session.AuthService;

public class LoginAction extends AuthAction {

    public <T> LoginAction(AuthService<T> service, T serviceProvider) {
        super(AuthActionType.LOGIN, service, serviceProvider);
    }

    @Override
    public AuthMode run() {
        return AuthMode.AUTHENTICATED;
    }
}
