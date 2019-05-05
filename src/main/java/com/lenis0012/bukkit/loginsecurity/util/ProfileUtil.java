package com.lenis0012.bukkit.loginsecurity.util;

import com.google.common.base.Charsets;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * ProfileUtil is a utility that allows us to get the correct UUID for the player.
 * Minecraft by default gets online player UUID as a case sensitive version of their name.
 * We want all accounts with the same case ignored name to have the same profile.
 * Therefor, if online mode is enabled, we get a corrected version of the UUID.
 */
public class ProfileUtil {
    private static final UserIdMode userIdMode = useOnlineUUID() ? UserIdMode.MOJANG : UserIdMode.OFFLINE;

    /**
     * Get what mode of UUID transforming the database uses.
     *
     * @return UUID Mode
     */
    public static UserIdMode getUserIdMode() {
        return userIdMode;
    }

    /**
     * Get UUID for player.
     *
     * @param player to get UUID for
     * @return UUID
     */
    public static UUID getUUID(Player player) {
        return getUUID(player.getName(), player.getUniqueId());
    }

    /**
     * Get UUID by player name and online-mode UUID.
     *
     * @param name of player
     * @param fallback Online-mode UUID of player
     * @return Actual UUID
     */
    public static UUID getUUID(String name, UUID fallback) {
        if(useOnlineUUID()) return fallback;
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name.toLowerCase()).getBytes(Charsets.UTF_8));
    }

    /**
     * Check whether or not we should use online-mode UUIDs.
     *
     * @return Online mode UUID.
     */
    public static boolean useOnlineUUID() {
        return Bukkit.getOnlineMode() || isBungeecord();
    }

    /**
     * Check whether or not bungeecord support is enabled.
     *
     * @return bungeecord support
     */
    public static boolean isBungeecord() {
        return false; // We don't know whether or not bungee is in online-mode
//        try {
//            Class<?> spigotConfig = Class.forName("org.spigotmc.SpigotConfig");
//            Field bungee = spigotConfig.getField("bungee");
//            return bungee.getBoolean(null);
//        } catch(Exception e) {
//            return false; // Couldn't detect spigot
//        }
    }
}
