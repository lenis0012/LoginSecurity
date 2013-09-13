package com.lenis0012.bukkit.ls;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

// RangerMauve
/* This allows us to filter commands from the server log, 
 * so that we may replace them with log messages that don't contain
 * the user's password in plaintext. 
 * 
 * The class also checks for existing filters.
 */
public class CommandFilter implements Filter {
	Filter prevFilter = null;

	@Override
	public boolean isLoggable(LogRecord record) {

		String message = record.getMessage();

		if (message.contains("/login") || message.contains("/register") || message.contains("/changepass") || message.contains("/logout")) {
			return false;
		} if (this.prevFilter == null) {
			return true;
		}
		
		return this.prevFilter.isLoggable(record);
	}
}
