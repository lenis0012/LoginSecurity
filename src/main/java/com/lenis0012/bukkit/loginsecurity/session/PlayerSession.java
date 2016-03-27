package com.lenis0012.bukkit.loginsecurity.session;

import com.lenis0012.bukkit.loginsecurity.events.AuthActionEvent;
import com.lenis0012.bukkit.loginsecurity.events.AuthModeChangedEvent;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionCallback;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionResponse;
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
     * Get session player's profile.
     *
     * @return Profile
     */
    public PlayerProfile getProfile() {
        return profile;
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

    public void performActionAsync(AuthAction action, ActionCallback callback) {
        // TODO: Actually make async
        ActionResponse response = performAction(action);
        callback.call(response);
    }

    /**
     * Perform an action on this session.
     *
     * @param action to perform
     */
    public ActionResponse performAction(AuthAction action) {
        AuthActionEvent event = new AuthActionEvent(this, action);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) {
            return new ActionResponse(false, "Cancelled");
        }

        // Run
        AuthMode previous = mode;
        this.mode = action.run(this);

        // If auth mode changed, run event
        if(previous != mode) {
            AuthModeChangedEvent event1 = new AuthModeChangedEvent(this, previous, mode);
            Bukkit.getPluginManager().callEvent(event1);
        }

        // Complete
        return new ActionResponse(true, event.getCancelledMessage());
    }
}
