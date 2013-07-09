package com.lenis0012.bukkit.ls;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class CommandFilter implements Filter {
	
	Filter prevFilter = null;

	@Override
	public boolean isLoggable(LogRecord record) {
		boolean res = (this.prevFilter != null) && this.prevFilter.isLoggable(record);
		if(!res)return res;
		
		String message = record.getMessage();
		
		if (message.contains("login") || message.contains("register")) return false;
		
		return true;
	}
}
