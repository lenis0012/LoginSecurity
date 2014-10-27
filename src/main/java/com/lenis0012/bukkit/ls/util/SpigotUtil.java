package com.lenis0012.bukkit.ls.util;

import org.spigotmc.SpigotConfig;

public class SpigotUtil {
	
	/**
	 * Check if bungeecord support is enabled
	 * 
	 * @return Bungeecord support enabled?
	 */
	public static boolean isBungee() {
		return SpigotConfig.bungee;
	}
}