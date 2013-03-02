package com.lenis0012.bukkit.ls.util;

import java.util.regex.Pattern;

public class StringUtil {
	private static final Pattern pattern = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");
	
	public static String cleanString(String from) {
		return pattern.matcher(from).replaceAll("");
	}
}
