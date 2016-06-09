package com.lenis0012.bukkit.loginsecurity.session.action;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.hashing.Algorithm;
import com.lenis0012.bukkit.loginsecurity.session.*;
import com.lenis0012.bukkit.loginsecurity.session.exceptions.ProfileRefreshException;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;

public class RegisterAction extends AuthAction {
    private final String password;

    public <T> RegisterAction(AuthService<T> service, T serviceProvider, String password) {
        super(AuthActionType.REGISTER, service, serviceProvider);
        this.password = password;
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
        final String hash = algorithm.hash(password);
        profile.setPassword(hash);
        profile.setHashingAlgorithm(algorithm.getId());
        rehabPlayer(session);
        plugin.getDatabase().save(profile);
        return AuthMode.AUTHENTICATED;
    }
}
