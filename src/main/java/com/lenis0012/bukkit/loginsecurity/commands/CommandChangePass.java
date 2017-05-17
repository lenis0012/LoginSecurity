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

package com.lenis0012.bukkit.loginsecurity.commands;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.LoginSecurityConfig;
import com.lenis0012.bukkit.loginsecurity.hashing.Algorithm;
import com.lenis0012.bukkit.loginsecurity.session.AuthAction;
import com.lenis0012.bukkit.loginsecurity.session.AuthService;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionCallback;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionResponse;
import com.lenis0012.bukkit.loginsecurity.session.action.ChangePassAction;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.pluginutils.modules.command.Command;
import org.bukkit.entity.Player;

import static com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys.*;
import static com.lenis0012.bukkit.loginsecurity.LoginSecurity.translate;

public class CommandChangePass extends Command {
    private final LoginSecurity plugin;

    public CommandChangePass(LoginSecurity plugin) {
        this.plugin = plugin;
        setMinArgs(2);
        setAllowConsole(false);
    }

    @Override
    public void execute() {
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        final String password = getArg(0);
        final String changed = getArg(1);

        // Verify auth mode
        if(!session.isLoggedIn()) {
            reply(false, translate(GENERAL_NOT_LOGGED_IN));
            return;
        }

        // Verify new password
        LoginSecurityConfig config = LoginSecurity.getConfiguration();
        if(changed.length() < config.getPasswordMinLength() || changed.length() > config.getPasswordMaxLength()) {
            reply(false, translate(GENERAL_PASSWORD_LENGTH).param("min", config.getPasswordMinLength()).param("max", config.getPasswordMaxLength()));
            return;
        }

        // Retrieve profile data
        final PlayerProfile profile = session.getProfile();
        final Algorithm algorithm = Algorithm.getById(profile.getHashingAlgorithm());
        if(algorithm == null) {
            reply(false, translate(GENERAL_UNKNOWN_HASH));
            return;
        }

        // Verify login
        final boolean validated = algorithm.check(password, profile.getPassword());
        if(!validated) {
            reply(false, translate(CHANGE_FAIL));
            return;
        }

        AuthAction action = new ChangePassAction(AuthService.PLAYER, player, changed);
        session.performActionAsync(action, new ChangePassCallback(this, player));
    }

    private static class ChangePassCallback implements ActionCallback {
        private final CommandChangePass command;
        private final Player player;

        private ChangePassCallback(CommandChangePass command, Player player) {
            this.command = command;
            this.player = player;
        }

        @Override
        public void call(ActionResponse response) {
            if(!response.isSuccess()) {
                command.reply(player, false, response.getErrorMessage());
                return;
            }

            command.reply(player, true, translate(CHANGE_SUCCESS));
        }
    }
}
