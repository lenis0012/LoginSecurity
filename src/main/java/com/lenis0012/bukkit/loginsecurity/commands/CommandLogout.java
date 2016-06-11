package com.lenis0012.bukkit.loginsecurity.commands;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.session.AuthAction;
import com.lenis0012.bukkit.loginsecurity.session.AuthService;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionResponse;
import com.lenis0012.bukkit.loginsecurity.session.action.LogoutAction;
import com.lenis0012.pluginutils.modules.command.Command;

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
            reply(false, "You are currently not logged in!");
            return;
        }

        AuthAction action = new LogoutAction(AuthService.PLAYER, player);
        ActionResponse response = session.performAction(action);
        if(!response.isSuccess()) {
            reply(false, "Couldn't log out! " + response.getErrorMessage());
            return;
        }
        reply(true, "You have successfully logged out!");
    }
}
