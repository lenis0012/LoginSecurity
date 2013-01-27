package com.lenis0012.bukkit.ls.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public enum ValueType {
	INSERT("INSERT INTO {TABLE} (username,password,encrypto) VALUES (?,?,?);"),
	UPDATE("UPDATE {TABLE} SET password=? WHERE username=?;"),
	REMOVE("DELETE FROM {TABLE} WHERE username=?;");
	
	private String usage;
	
	private ValueType(String usage) {
		this.usage = usage;
	}
	
	public String getUsage() {
		return this.usage;
	}
	
	public void insert(Logger log, Connection con, String table, String username, String password, int crypto) {
		try {
			PreparedStatement ps = con.prepareStatement(getUsage().replace("{TABLE}", table));
			if(this == INSERT) {
				ps.setString(1, username);
				ps.setString(2, password);
				ps.setInt(3, crypto);
				ps.executeUpdate();
			} else if (this == UPDATE) {
				ps.setString(1, password);
				ps.setString(2, username);
				ps.executeUpdate();
			} else
				ps.executeUpdate();
		} catch(SQLException e) {
			log.warning("[LoginSecurity] Could not set data from SQLite: "+e.getMessage());
		}
	}
}
