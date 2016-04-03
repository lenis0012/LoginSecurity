package com.lenis0012.bukkit.loginsecurity.session;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * AuthService.
 *
 * An auth service is a service that is able to perform actions.
 * It explains who/what authorized the action.
 */
public class AuthService<T> {
    /**
     * Player performed actions.
     * ex. /login, /register, ect.
     *
     * Provider: Player
     */
    public static AuthService<Player> PLAYER = new AuthService<>(Player.class);
    /**
     * Admin performed actions.
     * ex. /lac rmpass
     *
     * Provider: Admin (console or player)
     */
    public static AuthService<CommandSender> ADMIN = new AuthService<>(CommandSender.class);
    /**
     * Session performed actions.
     * When a player logs in before timeout
     *
     * Provider: null
     */
    public static AuthService<LoginSecurity> SESSION = new AuthService<>(LoginSecurity.class);
    /**
     * Channel API performed actions.
     * Anything that uses plugin messaging
     *
     * Provider: Name of channel
     */
    public static AuthService<String> CHANNEL_API = new AuthService<>(String.class);
    /**
     * Plugin performed actions.
     * Performed by external plugins
     *
     * Provider: plugin instance
     */
    public static AuthService<Plugin> PLUGIN = new AuthService<>(Plugin.class);

    private final Class<T> type;

    private AuthService(Class<T> type) {
        this.type = type;
    }

    /**
     * Get the provider of an action.
     *
     * @param action Action
     * @return Provider
     */
    public T getProvider(AuthAction action) {
        return type.cast(action.getServiceProvider());
    }

    /**
     * Format provider in to string.
     * Use for ex. storage.
     *
     * @param provider Provider
     * @return Formatted provider name
     */
    public String format(T provider) {
        if(this == PLAYER) {
            return ((Player) provider).getName();
        } else if(this == ADMIN) {
            return ((CommandSender) provider).getName();
        } else if(this == SESSION) {
            return "LoginSecurity";
        } else if(this == CHANNEL_API) {
            return (String) provider;
        } else if(this == PLUGIN) {
            return ((Plugin) provider).getName();
        } else {
            return provider.toString();
        }
    }
}
