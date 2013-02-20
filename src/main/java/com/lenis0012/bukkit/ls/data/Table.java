package com.lenis0012.bukkit.ls.data;

import com.lenis0012.bukkit.ls.LoginSecurity;

public enum Table {
	ACCOUNTS(true, "accounts", "username VARCHAR(150) NOT NULL UNIQUE,password VARCHAR(150) NOT NULL,encrypto INTEGER"),
	LASTLOGIN(true, "lastlogin", "username VARCHAR(150) NOT NULL UNIQUE,ip VARCHAR(150) NOT NULL,lastjoin VARCHAR(50) NOT NULL"),
	OLD(false, "passwords", "username VARCHAR(250) NOT NULL UNIQUE,password VARCHAR(250) NOT NULL,lastlogin VARCHAR(30)");
	
	private String name;
	private String usage;
	private LoginSecurity plugin;
	
	private Table(boolean addPrefix, String name, String usage) {
		this.plugin = LoginSecurity.instance;
		this.name = addPrefix ? plugin.prefix+name : name;
		this.usage = usage;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getUsage() {
		return " ("+this.usage+")";
	}
	
	public String getValues() {
		String v = "";
		String[] a = usage.split(",");
		int i = 0;
		for(String b : a) {
			i += 1;
			String[] c = b.split(" ");
			v += c[0] == null ? "" : c[0] + (i <= 2 ? "," : "");
		}
		
		return "("+v+")";
	}
}
