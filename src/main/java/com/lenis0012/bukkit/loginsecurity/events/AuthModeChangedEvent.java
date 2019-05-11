package com.lenis0012.bukkit.loginsecurity.events;

import com.lenis0012.bukkit.loginsecurity.session.AuthMode;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * When a session's Authentication mode was changed.
 */
public class AuthModeChangedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final PlayerSession session;
    private final AuthMode previous;
    private final AuthMode current;

    public AuthModeChangedEvent(PlayerSession session, AuthMode previous, AuthMode current) {
        super(true);
        this.session = session;
        this.previous = previous;
        this.current = current;
    }

    /**
     * Get the session which had it's mode changed.
     *
     * @return Player session
     */
    public PlayerSession getSession() {
        return session;
    }

    /**
     * Get the previous auth mode of the player.
     *
     * @return AuthMode
     */
    public AuthMode getPreviousMode() {
        return previous;
    }

    /**
     * Get the new auth mode of the player.
     *
     * @return AuthMode
     */
    public AuthMode getCurrentMode() {
        return current;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
