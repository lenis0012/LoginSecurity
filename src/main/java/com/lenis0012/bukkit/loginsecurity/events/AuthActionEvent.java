package com.lenis0012.bukkit.loginsecurity.events;

import com.lenis0012.bukkit.loginsecurity.session.AuthAction;
import com.lenis0012.bukkit.loginsecurity.session.AuthActionType;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class AuthActionEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    protected final PlayerSession session;
    protected final AuthAction action;
    private boolean cancelled = false;

    public AuthActionEvent(Player player, PlayerSession session, AuthAction action) {
        super(player);
        this.session = session;
        this.action = action;
    }

    public AuthAction getAction() {
        return action;
    }

    public PlayerSession getSession() {
        return session;
    }

    public AuthActionType getType() {
        return action.getType();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
