/*
 * This file is a part of LoginSecurity.
 *
 * Copyright (c) 2017 Lennart ten Wolde
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
//        save(profile);
        session.saveProfileAsync();
        return AuthMode.AUTHENTICATED;
    }
}
