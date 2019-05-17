package com.lenis0012.bukkit.loginsecurity.commands;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.hashing.Algorithm;
import com.lenis0012.bukkit.loginsecurity.session.AuthService;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionResponse;
import com.lenis0012.bukkit.loginsecurity.session.action.RemovePassAction;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.pluginutils.modules.command.Command;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static com.lenis0012.bukkit.loginsecurity.LoginSecurity.translate;
import static com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys.*;

public class CommandUnregister extends Command {
    private final LoginSecurity plugin;

    public CommandUnregister(LoginSecurity plugin) {
        this.plugin = plugin;
        setMinArgs(1);
        setAllowConsole(false);
    }

    @Override
    public void execute() {
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        final String password = getArg(0);


        // Verify auth mode
        if(!session.isLoggedIn()) {
            reply(false, translate(GENERAL_NOT_LOGGED_IN));
            return;
        }

        // Disable if password required
        if(LoginSecurity.getConfiguration().isPasswordRequired()) {
            reply(false, translate(UNREGISTER_NOT_POSSIBLE));
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
                reply(player, false, translate(UNREGISTER_FAIL));
                return;
            }

            final RemovePassAction action = new RemovePassAction(AuthService.PLAYER, player);
            final ActionResponse response = session.performAction(action);

            if(!response.isSuccess()) {
                reply(player, false, response.getErrorMessage());
                return;
            }
            reply(player, true, translate(UNREGISTER_SUCCESS));
        });
    }
}
