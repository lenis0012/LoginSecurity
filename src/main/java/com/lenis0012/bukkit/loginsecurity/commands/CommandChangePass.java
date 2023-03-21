package com.lenis0012.bukkit.loginsecurity.commands;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.LoginSecurityConfig;
import com.lenis0012.bukkit.loginsecurity.hashing.Algorithm;
import com.lenis0012.bukkit.loginsecurity.session.AuthAction;
import com.lenis0012.bukkit.loginsecurity.session.AuthService;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionResponse;
import com.lenis0012.bukkit.loginsecurity.session.action.ChangePassAction;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.pluginutils.command.Command;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static com.lenis0012.bukkit.loginsecurity.LoginSecurity.translate;
import static com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys.*;

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
        final Player player = this.player;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            final boolean validated = algorithm.check(password, profile.getPassword());
            if(!validated) {
                reply(player, false, translate(CHANGE_FAIL));
                return;
            }

            final AuthAction action = new ChangePassAction(AuthService.PLAYER, player, changed);
            final ActionResponse response = session.performAction(action);
            if(!response.isSuccess()) {
                reply(player, false, response.getErrorMessage());
                return;
            }

            reply(player, true, translate(CHANGE_SUCCESS));
        });
    }
}
