package com.lenis0012.bukkit.loginsecurity.commands;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.LoginSecurityConfig;
import com.lenis0012.bukkit.loginsecurity.hashing.Algorithm;
import com.lenis0012.bukkit.loginsecurity.session.*;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionCallback;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionResponse;
import com.lenis0012.bukkit.loginsecurity.session.action.LoginAction;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.bukkit.loginsecurity.util.MetaData;
import com.lenis0012.pluginutils.modules.command.Command;
import org.bukkit.entity.Player;

public class CommandLogin extends Command {
    public CommandLogin(LoginSecurity plugin) {
        setMinArgs(1);
        setAllowConsole(false);
    }

    @Override
    public void execute() {
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        final String password = getArg(0);

        LoginSecurityConfig config = LoginSecurity.getConfiguration();
        int tries = MetaData.get(player, "ls_login_tries", 0) + 1;
        if(tries > config.getMaxLoginTries()) {
            player.kickPlayer("[LoginSecurity] Exceeded max login tries: " + config.getMaxLoginTries());
            return;
        }

        // Verify auth mode
        if(session.getAuthMode() != AuthMode.UNAUTHENTICATED) {
            reply(false, "You are not registered or already logged in!");
            return;
        }

        // Retrieve profile data
        final PlayerProfile profile = session.getProfile();
        final Algorithm algorithm = Algorithm.getById(profile.getHashingAlgorithm());
        if(algorithm == null) {
            reply(false, "You account uses an unknown hashing algorithm, please report this to an admin.");
            return;
        }

        // Verify login
        final boolean validated = algorithm.check(password, profile.getPassword());
        if(!validated) {
            reply(false, "Login failed! Invalid password.");
            return;
        }

        // Perform login
        AuthAction action = new LoginAction(AuthService.PLAYER, player);
        session.performActionAsync(action, new LoginCallback(this, player));
    }

    private static final class LoginCallback implements ActionCallback {
        private final CommandLogin command;
        private final Player player;

        private LoginCallback(CommandLogin command, Player player) {
            this.command = command;
            this.player = player;
        }

        @Override
        public void call(ActionResponse response) {
            if(!response.isSuccess()) {
                command.reply(player, false, response.getErrorMessage());
                return;
            }

            command.reply(player, true, "Successfully logged in.");
        }
    }
}
