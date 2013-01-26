package com.lenis0012.bukkit.ls.data;

public enum ValueType {
	INSERT("INSERT INTO {TABLE} (username,password) VALUES ('{USER}','{VALUE}');"),
	UPDATE("UPDATE {TABLE} SET password='{VALUE}' WHERE username='{USER}';"),
	REMOVE("DELETE FROM {TABLE} WHERE userame='{USER}';");
	
	private String usage;
	
	private ValueType(String usage) {
		this.usage = usage;
	}
	
	public String getUsage() {
		return this.usage;
	}
}
