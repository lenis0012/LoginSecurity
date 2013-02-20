package com.lenis0012.bukkit.ls.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public enum ValueType {
	INSERT("INSERT INTO {TABLE}{USAGE} VALUES (?,?,?);"),
	UPDATE_PASSWORD("UPDATE {TABLE} SET password=? WHERE username=?;"),
	UPDATE_IP("UPDATE {TABLE} SET ip=? WHERE username=?;"),
	REMOVE("DELETE FROM {TABLE} WHERE username=?;");
	
	private String usage;
	
	private ValueType(String usage) {
		this.usage = usage;
	}
	
	public String getUsage() {
		return this.usage;
	}
	
	public void insert(Logger log, Connection con, Table table, String username, String password, Object crypto) {
		try {
			PreparedStatement ps = con.prepareStatement(getUsage().replace("{TABLE}", table.getName()).replace("{USAGE}", table.getValues()));
			if(this == INSERT) {
				ps.setString(1, username);
				ps.setString(2, password);
				if(crypto instanceof Integer)
					ps.setInt(3, (Integer)crypto);
				else
					ps.setString(3, (String)crypto);
				ps.executeUpdate();
			} else if (this == UPDATE_PASSWORD) {
				ps.setString(1, password);
				ps.setString(2, username);
				ps.executeUpdate();
			} else if (this == UPDATE_IP) {
				ps.setString(1, password);
				ps.setString(2, username);
				ps.executeUpdate();
			} else
				ps.executeUpdate();
		} catch(SQLException e) {
			log.warning("[LoginSecurity] Could not set data to SQL manager: "+e.getMessage());
			e.printStackTrace();
		}
	}
}
