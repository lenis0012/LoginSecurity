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
import com.lenis0012.pluginutils.command.Command;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import static com.lenis0012.bukkit.loginsecurity.LoginSecurity.translate;
import static com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys.*;

import java.util.logging.Level;

public class CommandRegister extends Command {
    private final LoginSecurity plugin;

    public CommandRegister(LoginSecurity plugin) {
        this.plugin = plugin;
        setAllowConsole(false);
        if(plugin.config().isRegisterConfirmPassword()) {
            setMinArgs(2);
        } else {
            setMinArgs(1);
        }
    }

    @Override
    public void execute() {
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        final String password = getArg(0);

        LoginSecurityConfig config = LoginSecurity.getConfiguration();
        if(!config.isRegistrationEnabled()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getRegistrationDisabledMessage()));
            return;
        }

        if(password.length() < config.getPasswordMinLength() || password.length() > config.getPasswordMaxLength()) {
            reply(false, translate(GENERAL_PASSWORD_LENGTH)
                .param("min", config.getPasswordMinLength())
                .param("max", config.getPasswordMaxLength()));
            return;
        }

        if(session.isRegistered()) {
            reply(false, translate(REGISTER_ALREADY));
            return;
        }

        if(config.isRegisterConfirmPassword() && !password.equals(getArg(1))) {
            reply(false, translate(ERROR_MATCH_PASSWORD));
            return;
        }

        if(config.isRegisterCaptcha()) {
            CaptchaManager captcha = plugin.getModule(CaptchaManager.class);
            captcha.giveMapItem(player, () -> {
                AuthAction action = new RegisterAction(AuthService.PLAYER, player, password);
                session.performActionAsync(action, new RegisterCallback(this.plugin, CommandRegister.this, player));
            });
            reply(true, translate(REGISTER_CAPTCHA));
        } else {
            AuthAction action = new RegisterAction(AuthService.PLAYER, player, password);
            session.performActionAsync(action, new RegisterCallback(this.plugin, this, player));
        }
    }

    private static final class RegisterCallback implements ActionCallback {
        private final LoginSecurity plugin;
        private final CommandRegister command;
        private final Player player;

        private RegisterCallback(LoginSecurity plugin, CommandRegister command, Player player) {
            this.plugin = plugin;
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

            /** If enabled in the config, upon register we teleport the player */
            LoginSecurityConfig config = LoginSecurity.getConfiguration();
            String rawLocation = config.getJoinRegisterLocation(); // example valid value "-123.3,75,34.3"
            // If empty we should do nothing
            if (rawLocation != null && !rawLocation.isEmpty()) {
                String[] coords = rawLocation.split(",");
                try {
                    // Parse the x, y, z coordinates
                    double x = Double.parseDouble(coords[0]);
                    double y = Double.parseDouble(coords[1]);
                    double z = Double.parseDouble(coords[2]);

                    player.teleport(new Location(player.getWorld(), x, y, z));
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Error while teleporting just registered player", e);
                }
            }
        }
    }
}
