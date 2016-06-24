package com.lenis0012.bukkit.loginsecurity.session.action;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.hashing.Algorithm;
import com.lenis0012.bukkit.loginsecurity.session.*;
import com.lenis0012.bukkit.loginsecurity.session.exceptions.ProfileRefreshException;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;

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
        final Algorithm algorithm = plugin.config().getHashingAlgorithm();
        final String hash = algorithm.hash(newPassword);
        profile.setPassword(hash);
        profile.setHashingAlgorithm(algorithm.getId());
        save(profile);
        return AuthMode.AUTHENTICATED;
    }
}
