package com.lenis0012.bukkit.loginsecurity.session.action;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.session.*;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;

public class RemovePassAction extends AuthAction {
    public <T> RemovePassAction(AuthService<T> service, T serviceProvider) {
        super(AuthActionType.REMOVE_PASSWORD, service, serviceProvider);
    }

    @Override
    public AuthMode run(PlayerSession session, ActionResponse response) {
        if(!session.isRegistered()) {
            throw new IllegalStateException("User is not registered!");
        }
        LoginSecurity.getInstance().getDatabase().delete(session.getProfile());
        session.resetProfile();
        return LoginSecurity.getConfiguration().isPasswordRequired() ? AuthMode.UNREGISTERED : AuthMode.AUTHENTICATED;
    }
}
