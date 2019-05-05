package com.lenis0012.bukkit.loginsecurity.session.action;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.session.*;

import java.sql.SQLException;
import java.util.logging.Level;

public class RemovePassAction extends AuthAction {
    public <T> RemovePassAction(AuthService<T> service, T serviceProvider) {
        super(AuthActionType.REMOVE_PASSWORD, service, serviceProvider);
    }

    @Override
    public AuthMode run(PlayerSession session, ActionResponse response) {
        if(!session.isRegistered()) {
            throw new IllegalStateException("User is not registered!");
        }

        try {
            LoginSecurity.getDatastore().getProfileRepository().deleteBlocking(session.getProfile());
            session.resetProfile();
            return LoginSecurity.getConfiguration().isPasswordRequired() ? AuthMode.UNREGISTERED : AuthMode.AUTHENTICATED;
        } catch (SQLException e) {
            LoginSecurity.getInstance().getLogger().log(Level.SEVERE, "Failed to remove user password", e);
            response.setSuccess(false);
            response.setErrorMessage("Could not change your account's password, please try again later.");
            return null;
        }
    }
}
