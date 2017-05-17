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
import com.lenis0012.bukkit.loginsecurity.modules.captcha.CaptchaManager;
import com.lenis0012.bukkit.loginsecurity.session.AuthAction;
import com.lenis0012.bukkit.loginsecurity.session.AuthService;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionCallback;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionResponse;
import com.lenis0012.bukkit.loginsecurity.session.action.RegisterAction;
import com.lenis0012.pluginutils.modules.command.Command;
import org.bukkit.entity.Player;

import static com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys.*;
import static com.lenis0012.bukkit.loginsecurity.LoginSecurity.translate;

public class CommandRegister extends Command {
    private final LoginSecurity plugin;

    public CommandRegister(LoginSecurity plugin) {
        this.plugin = plugin;
        setMinArgs(1);
        setAllowConsole(false);
    }

    @Override
    public void execute() {
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        final String password = getArg(0);

        LoginSecurityConfig config = LoginSecurity.getConfiguration();
        if(password.length() < config.getPasswordMinLength() || password.length() > config.getPasswordMaxLength()) {
            reply(false, translate(GENERAL_PASSWORD_LENGTH).param("min", config.getPasswordMinLength()).param("max", config.getPasswordMaxLength()));
            return;
        }

        if(session.isRegistered()) {
            reply(false, translate(REGISTER_ALREADY));
            return;
        }

        if(config.isRegisterCaptcha()) {
            CaptchaManager captcha = plugin.getModule(CaptchaManager.class);
            captcha.giveMapItem(player, new Runnable() {
                @Override
                public void run() {
                    AuthAction action = new RegisterAction(AuthService.PLAYER, player, password);
                    session.performActionAsync(action, new RegisterCallback(CommandRegister.this, player));
                }
            });
            reply(true, translate(REGISTER_CAPTCHA));
        } else {
            AuthAction action = new RegisterAction(AuthService.PLAYER, player, password);
            session.performActionAsync(action, new RegisterCallback(this, player));
        }
    }

    private static final class RegisterCallback implements ActionCallback {
        private final CommandRegister command;
        private final Player player;

        private RegisterCallback(CommandRegister command, Player player) {
            this.command = command;
            this.player = player;
        }

        @Override
        public void call(ActionResponse response) {
            if(!response.isSuccess()) {
                command.reply(player, false, response.getErrorMessage());
                return;
            }

            command.reply(player, true, translate(REGISTER_SUCCESS));
        }
    }
}
