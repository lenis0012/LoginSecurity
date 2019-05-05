package com.lenis0012.bukkit.loginsecurity.commands;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.LoginSecurityConfig;
import com.lenis0012.bukkit.loginsecurity.hashing.Algorithm;
import com.lenis0012.bukkit.loginsecurity.session.AuthAction;
import com.lenis0012.bukkit.loginsecurity.session.AuthMode;
import com.lenis0012.bukkit.loginsecurity.session.AuthService;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionCallback;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionResponse;
import com.lenis0012.bukkit.loginsecurity.session.action.ChangePassAction;
import com.lenis0012.bukkit.loginsecurity.session.action.LoginAction;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.bukkit.loginsecurity.util.MetaData;
import com.lenis0012.pluginutils.modules.command.Command;
import org.bukkit.entity.Player;

import java.util.logging.Level;

import static com.lenis0012.bukkit.loginsecurity.LoginSecurity.translate;
import static com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys.*;

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
            player.kickPlayer("[LoginSecurity] " + translate(LOGIN_TRIES_EXCEEDED).param("max", config.getMaxLoginTries()).toString());
            return;
        }

        // Verify auth mode
        if(session.getAuthMode() != AuthMode.UNAUTHENTICATED) {
            reply(false, translate(GENERAL_NOT_AUTHENTICATED));
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
            reply(false, translate(LOGIN_FAIL));
            return;
        }

        // Perform login
        AuthAction action = new LoginAction(AuthService.PLAYER, player);
        session.performActionAsync(action, new LoginCallback(this, player, algorithm.isDeprecated() ? password : null));
    }

    private static final class LoginCallback implements ActionCallback {
        private final CommandLogin command;
        private final Player player;
        private final String migrate;

        private LoginCallback(CommandLogin command, Player player, String migrate) {
            this.command = command;
            this.player = player;
            this.migrate = migrate;
        }

        @Override
        public void call(ActionResponse response) {
            if(!response.isSuccess()) {
                command.reply(player, false, response.getErrorMessage());
                return;
            }

            command.reply(player, true, translate(LOGIN_SUCCESS));

            // Re-hash if algorithm deprecated
            if(migrate != null) {
                final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
                LoginSecurity.getInstance().getLogger().log(Level.INFO, "Migrating password for user " + player.getName());
                ChangePassAction changePassAction = new ChangePassAction(AuthService.PLUGIN, LoginSecurity.getInstance(), migrate);
                session.performActionAsync(changePassAction, (r) -> LoginSecurity.getInstance().getLogger().log(Level.INFO, "Password migration successfully finished for " + player.getName()));
            }
        }
    }
}
