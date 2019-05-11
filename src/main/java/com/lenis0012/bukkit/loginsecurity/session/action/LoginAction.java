package com.lenis0012.bukkit.loginsecurity.session.action;

import com.lenis0012.bukkit.loginsecurity.session.*;
import com.lenis0012.bukkit.loginsecurity.session.exceptions.ProfileRefreshException;

import java.sql.Timestamp;

public class LoginAction extends AuthAction {

    public <T> LoginAction(AuthService<T> service, T serviceProvider) {
        super(AuthActionType.LOGIN, service, serviceProvider);
    }

    @Override
    public AuthMode run(final PlayerSession session, ActionResponse response) {
        try {
            session.refreshProfile();
        } catch(ProfileRefreshException e) {
            response.setSuccess(false);
            response.setErrorMessage("Your account was modified by a third party, please rejoin!");
            return null;
        }
        rehabPlayer(session);
        if(session.isRegistered()) {
            session.getProfile().setLastLogin(new Timestamp(System.currentTimeMillis()));
            session.getProfile().setIpAddress(session.getPlayer().getAddress().getAddress().toString());
            session.saveProfileAsync();
        }
        return AuthMode.AUTHENTICATED;
    }
}
