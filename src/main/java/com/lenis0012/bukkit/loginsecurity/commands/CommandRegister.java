package com.lenis0012.bukkit.loginsecurity.commands;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.LoginSecurityConfig;
import com.lenis0012.bukkit.loginsecurity.modules.captcha.CaptchaManager;
import com.lenis0012.bukkit.loginsecurity.session.AuthAction;
import com.lenis0012.bukkit.loginsecurity.session.AuthMode;
import com.lenis0012.bukkit.loginsecurity.session.AuthService;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionCallback;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionResponse;
import com.lenis0012.bukkit.loginsecurity.session.action.RegisterAction;
import com.lenis0012.pluginutils.modules.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
            reply(false, "Password must be between %s and %s characters!", config.getPasswordMinLength(), config.getPasswordMaxLength());
            return;
        }

        if(session.getAuthMode() != AuthMode.UNREGISTERED) {
            reply(false, "You are already registered under this account!");
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
            reply(true, "Please enter the captcha on the map in the chat!");
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

            command.reply(player, true, "Successfully registered, you are now logged in.");
        }
    }
}
