package com.lenis0012.bukkit.loginsecurity.session;

import com.lenis0012.bukkit.loginsecurity.events.AuthActionEvent;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Player session
 */
public class PlayerSession {
    private final PlayerProfile profile;
    private AuthMode mode;

    protected PlayerSession(PlayerProfile profile, AuthMode mode) {
        this.profile = profile;
        this.mode = mode;
    }

    /**
     * Check whether or not the player's auth mode is "AUTHENTICATED".
     *
     * @return Logged in
     */
    public boolean isLoggedIn() {
        return mode == AuthMode.AUTHENTICATED;
    }

    /**
     * Get the player's current auth mode.
     *
     * @return Auth mode
     */
    public AuthMode getAuthMode() {
        return mode;
    }

    /**
     * Get the player for this session if player is online.
     *
     * @return Player
     */
    public Player getPlayer() {
        return Bukkit.getPlayer(profile.getLastName());
    }

    /**
     * Perform an action on this session.
     *
     * @param player to perform on
     * @param action to perform
     */
    public void performAction(Player player, AuthAction action) {
        AuthActionEvent event = new AuthActionEvent(player, this, action);
        Bukkit.getPluginManager().callEvent(event);
        if(!event.isCancelled()) {
            this.mode = action.run();
        }
    }
}
