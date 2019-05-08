package com.lenis0012.bukkit.loginsecurity.session.action;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.hashing.Algorithm;
import com.lenis0012.bukkit.loginsecurity.session.*;
import com.lenis0012.bukkit.loginsecurity.session.exceptions.ProfileRefreshException;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;

import java.sql.SQLException;
import java.util.logging.Level;

public class ChangePassAction extends AuthAction {
    private final String newPassword;

    public <T> ChangePassAction(AuthService<T> service, T serviceProvider, String newPassword) {
        super(AuthActionType.CHANGE_PASSWORD, service, serviceProvider);
        this.newPassword = newPassword;
    }

    @Override
    public AuthMode run(PlayerSession session, ActionResponse response) {
        try {
            session.refreshProfile();
        } catch(ProfileRefreshException e) {
            response.setSuccess(false);
            response.setErrorMessage("Your account was modified by a third party, please rejoin!");
            return null;
        }

        final LoginSecurity plugin = (LoginSecurity) LoginSecurity.getInstance();
        final PlayerProfile profile = session.getProfile();
        final String hash = Algorithm.BCRYPT.hash(newPassword);
        profile.setPassword(hash);
        profile.setHashingAlgorithm(Algorithm.BCRYPT.getId());
        try {
            plugin.datastore().getProfileRepository().updateBlocking(profile);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to register user", e);
            response.setSuccess(false);
            response.setErrorMessage("Failed to update your account, try again later.");
            return null;
        }
        return AuthMode.AUTHENTICATED;
    }
}
