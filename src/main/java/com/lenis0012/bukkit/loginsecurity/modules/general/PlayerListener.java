package com.lenis0012.bukkit.loginsecurity.modules.general;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.events.AuthModeChangedEvent;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * PlayerListener.
 *
 * Handles player management.
 * e.x Prevent players from moving when not logged in.
 */
public class PlayerListener implements Listener {
//    private final LoginSecurity plugin;
//
//    public PlayerListener(LoginSecurity plugin) {
//        this.plugin = plugin;
//    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        LoginSecurity.getSessionManager().preloadSession(event.getName(), event.getUniqueId());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);

        // TODO: Either make person fly or teleport them to a safe place... :)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAuthModeChange(AuthModeChangedEvent event) {
        // TODO: Rehab player
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isLoggedIn()) return;

        // Prevent moving
        event.setTo(event.getFrom());
    }
}
