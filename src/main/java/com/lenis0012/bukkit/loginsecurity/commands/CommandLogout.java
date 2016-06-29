package com.lenis0012.bukkit.loginsecurity.commands;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.session.AuthAction;
import com.lenis0012.bukkit.loginsecurity.session.AuthService;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionResponse;
import com.lenis0012.bukkit.loginsecurity.session.action.LogoutAction;
import com.lenis0012.pluginutils.modules.command.Command;

import static com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys.*;
import static com.lenis0012.bukkit.loginsecurity.LoginSecurity.translate;

public class CommandLogout extends Command {
    private final LoginSecurity plugin;

    public CommandLogout(LoginSecurity plugin) {
        this.plugin = plugin;
        setAllowConsole(false);
    }

    @Override
    public void execute() {
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);

        // Verify auth mode
        if(!session.isLoggedIn()) {
            reply(false, translate(GENERAL_NOT_LOGGED_IN));
            return;
        }

        AuthAction action = new LogoutAction(AuthService.PLAYER, player);
        ActionResponse response = session.performAction(action);
        if(!response.isSuccess()) {
            reply(false, translate(LOGOUT_FAIL).param("error", response.getErrorMessage()));
            return;
        }
        reply(true, translate(LOGOUT_SUCCESS));
    }
}
