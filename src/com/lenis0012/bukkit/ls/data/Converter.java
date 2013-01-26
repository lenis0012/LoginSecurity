package com.lenis0012.bukkit.ls.data;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.lenis0012.bukkit.ls.LoginSecurity;

public class Converter {
	public static enum FileType {
		YAML,
		SQLite;
	}
	
	private FileType type;
	private File file;
	
	public Converter(FileType type, File file) {
		this.type = type;
		this.file = file;
	}
	
	public void convert() {
		LoginSecurity plugin = LoginSecurity.instance;
		if(type == FileType.YAML) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			if(config.getConfigurationSection("password.password") != null) {
				Set<String> set = config.getConfigurationSection("password.password").getKeys(false);
				for(String user : set) {
					plugin.data.setValue(user, ValueType.INSERT, config.getString("password.password."+user));
				}
			}
			file.delete();
		} else if(type == FileType.SQLite && !(plugin.data instanceof SQLite)) {
			try {
				SQLite manager = new SQLite(file.getPath());
				manager.load();
				manager.createDefaultTable("Logins");
				ResultSet result = manager.getAllUsers();
				while(result.next()) {
					String user = result.getString("username");
					if(!plugin.data.isSet(user)) {
						String pass = result.getString("password");
						plugin.data.setValue(user, ValueType.INSERT, pass);
					}
				}
				file.delete();
			} catch(SQLException e) {
				System.out.println("[LoginSecurity] FAILED CONVERTING FROM SQLITE TO MYSQL");
				Logger.getLogger("Minecraft").warning("[LoginSecurity] "+e.getMessage());
			}
		}
	}
}
