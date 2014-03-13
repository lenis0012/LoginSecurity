package com.lenis0012.bukkit.ls.util;

public class StringUtil {
	
	/**
	 * Cleans a string from special chars.
	 * Only allows a-z, A-Z, 0-9 and _
	 * 
	 * @param from String to clean
	 * @return Cleaned string
	 */
	public static String cleanString(String from) {
		return from.replaceAll("[^a-zA-Z_0-9]", "");
	}
}
