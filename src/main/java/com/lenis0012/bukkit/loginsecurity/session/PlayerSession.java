package com.lenis0012.bukkit.loginsecurity.session;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.events.AuthActionEvent;
import com.lenis0012.bukkit.loginsecurity.events.AuthModeChangedEvent;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionCallback;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionResponse;
import com.lenis0012.bukkit.loginsecurity.session.exceptions.ProfileRefreshException;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Player session
 */
public class PlayerSession {
    private PlayerProfile profile;
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
     * Save the profile on a separate thread.
     */
    public void saveProfileAsync() {
        if(!isRegistered()) {
            throw new IllegalStateException("Can't save profile when not registered!");
        }
        LoginSecurity.getDatastore().getProfileRepository().update(profile, result -> {
            if(!result.isSuccess()) LoginSecurity.getInstance().getLogger().log(Level.SEVERE, "Failed to save user profile", result.getError());
        });
    }

    /**
     * Refreshes player's profile.
     */
    public void refreshProfile() throws ProfileRefreshException {
        PlayerProfile newProfile;
        try {
            newProfile = LoginSecurity.getDatastore().getProfileRepository().findByUniqueUserIdBlocking(UUID.fromString(profile.getUniqueUserId()));
        } catch (SQLException e) {
            throw new ProfileRefreshException("Failed to load profile from database", e);
        }

        if(newProfile != null && !isRegistered()) {
            throw new ProfileRefreshException("Profile was registered while in database!");
        }

        if(newProfile == null && isRegistered()) {
            throw new ProfileRefreshException("Profile was not found, even though it should be there!");
        }

        if(newProfile == null) {
            // Player isn't registered, nothing to update.
            return;
        }

        this.profile = newProfile;
    }

    /**
     * Reset the player's profile to a blank profile.
     */
    public void resetProfile() {
        String lastName = profile.getLastName();
        this.profile = LoginSecurity.getSessionManager().createBlankProfile(UUID.fromString(profile.getUniqueUserId()));
        profile.setLastName(lastName);
    }

    /**
     * Check whether the player has an account and is logged in.
     * Note: You're probably looking for {@link #isAuthorized() isAuthorized}.
     *
     * @return Logged in
     */
    public boolean isLoggedIn() {
        return isAuthorized() && profile.getPassword() != null;
    }

    /**
     * Check whether or not the player's auth mode is "AUTHENTICATED".
     * This means they're allowed to move etc.
     * Returns true when player is logged in OR password is not required and player has no account.
     *
     * @return Authorized
     */
    public boolean isAuthorized() {
        return mode == AuthMode.AUTHENTICATED;
    }

    /**
     * Check whether or not player is registered.
     *
     * @return True if registered, False otherwise
     */
    public boolean isRegistered() {
        return profile.getPassword() != null;
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
     * Perform an action in an async task.
     * Runs callback when action is finished.
     *
     * @param action Action to perform
     * @param callback To run when action has been performed.
     */
    public void performActionAsync(final AuthAction action, final ActionCallback callback) {
        LoginSecurity.getExecutorService().execute(() -> {
            final ActionResponse response = performAction(action);
            Bukkit.getScheduler().runTask(LoginSecurity.getInstance(), () -> callback.call(response));
        });
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
            return new ActionResponse(false, event.getCancelledMessage());
        }

        // Run
        final ActionResponse response = new ActionResponse();
        AuthMode previous = mode;
        AuthMode current = action.run(this, response);
        if(current == null || !response.isSuccess()) return response; // Something went wrong
        this.mode = current;

        // If auth mode changed, run event
        if(previous != mode) {
            AuthModeChangedEvent event1 = new AuthModeChangedEvent(this, previous, mode);
            Bukkit.getPluginManager().callEvent(event1);
        }

        // Complete
        return response;
    }
}
