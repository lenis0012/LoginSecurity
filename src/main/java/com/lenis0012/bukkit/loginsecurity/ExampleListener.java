package com.lenis0012.bukkit.loginsecurity;

import com.lenis0012.bukkit.loginsecurity.events.AuthActionEvent;
import com.lenis0012.bukkit.loginsecurity.session.AuthAction;
import com.lenis0012.bukkit.loginsecurity.session.AuthActionType;
import com.lenis0012.bukkit.loginsecurity.session.AuthService;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * An example listener using the LoginSecurity 2.1 API.
 *
 * This listeners makes it so whenever a player logs out using /logout.
 * It doesn't log out but instead deletes their password.
 *
 * Pretty useless, but it's just an example.
 */
public class ExampleListener implements Listener {

    @EventHandler
    public void onAuthAction(AuthActionEvent event) {
        // Check if type is logout
        if(event.getType() == AuthActionType.LOGOUT) {
            // Check if ran by player
            if(event.getAction().getService() != AuthService.PLAYER) {
                return;
            }

            // Prevent player from logging out
            event.setCancelled(true);

            // Get player's session
            PlayerSession session = event.getSession();

            // Create action to remove password authentication by your plugin
//            AuthAction deletePassword = new AuthAction(AuthActionType.REMOVE_PASSWORD, AuthService.PLUGIN, LoginSecurity.getInstance());

            // Run the action
//            session.performAction(event.getPlayer(), deletePassword);
        }
    }
}
