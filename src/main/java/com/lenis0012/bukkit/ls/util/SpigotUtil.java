package com.lenis0012.bukkit.ls.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class SpigotUtil {
	
	/**
	 * Check if bungeecord support is enabled
	 * 
	 * @return Bungeecord support enabled?
	 */
	public static boolean isBungee() {
        File file = new File("spigot.yml");
        if(!file.exists()) {
            return false; // No spigot file, we can't detect it
        }

        try {
            Class.forName("org.spigotmc.CustomTimingsHandler");
        } catch(ClassNotFoundException e) {
            return false; // Not running spigot
        }

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.load(file);
            return config.getBoolean("settings.bungeecord", false);
        } catch(Throwable t) {
            return false; // Somethign went wrong, let's assume we're running on CB.
        }
    }
}
