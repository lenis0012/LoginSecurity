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
            reply(false, "You are currently not logged in!");
            return;
        }

        // Verify new password
        LoginSecurityConfig config = LoginSecurity.getConfiguration();
        if(changed.length() < config.getPasswordMinLength() || changed.length() > config.getPasswordMaxLength()) {
            reply(false, "Password must be between %s and %s characters!", config.getPasswordMinLength(), config.getPasswordMaxLength());
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

            command.reply(player, true, "Successfully changed password!");
        }
    }
}
