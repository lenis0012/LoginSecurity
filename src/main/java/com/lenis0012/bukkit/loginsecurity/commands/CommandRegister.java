package com.lenis0012.bukkit.loginsecurity.commands;

import com.lenis0012.bukkit.loginsecurity.session.AuthAction;
import com.lenis0012.bukkit.loginsecurity.session.AuthMode;
import com.lenis0012.bukkit.loginsecurity.session.AuthService;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionCallback;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionResponse;
import com.lenis0012.bukkit.loginsecurity.session.action.RegisterAction;
import com.lenis0012.pluginutils.modules.command.Command;
import org.bukkit.entity.Player;

public class CommandRegister extends Command {

    @Override
    public void execute() {
        final PlayerSession session = null; // TODO: Get session
        final String password = getArg(0);

        // TODO: Validate password length

        if(session.getAuthMode() != AuthMode.UNREGISTERED) {
            reply(false, "You are already registered under this account!");
            return;
        }

        AuthAction action = new RegisterAction(AuthService.PLAYER, player, password);
        session.performActionAsync(action, new RegisterCallback(this, player));
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
