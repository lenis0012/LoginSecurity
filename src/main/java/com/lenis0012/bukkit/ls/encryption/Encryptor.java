package com.lenis0012.bukkit.ls.encryption;

public interface Encryptor {
	/**
	 * Check if 2 passwords mach
	 * 
	 * @param check		password to check
	 * @param real		real password from database
	 * @return			passwords the same?
	 */
	public boolean check(String check, String real);
}
