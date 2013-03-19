package com.lenis0012.bukkit.ls.data;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;

import com.cypherx.xauth.xAuth;
import com.lenis0012.bukkit.ls.LoginSecurity;
import com.lenis0012.bukkit.ls.util.EncryptionUtil;
import com.lenis0012.bukkit.ls.util.ReflectionUtil;
import com.lenis0012.bukkit.ls.xAuth.xAuthConv;

public class Converter {
	public static enum FileType {
		YAML,
		SQLite,
		OldToNewMySQL,
		xAuth;
	}
	
	private FileType type;
	private File file;
	private Logger log = Logger.getLogger("Minecraft");
	private File fileDir;
	
	public Converter(FileType type, File file) {
		this.type = type;
		this.file = file;
		this.fileDir = file == null ? null : file.getParentFile();
	}
	
	public void convert() {
		LoginSecurity plugin = LoginSecurity.instance;
		if(type == FileType.YAML) {
			boolean md5 = plugin.getConfig().getBoolean("options.use-MD5 Enryption", true);
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			if(config.getConfigurationSection("password.password") != null) {
				Set<String> set = config.getConfigurationSection("password.password").getKeys(false);
				for(String user : set) {
					String pass = config.getString("password.password."+user);
					if(!md5)
						pass = EncryptionUtil.getMD5(pass);
					plugin.data.setValue(user, ValueType.INSERT, pass, 1);
				}
			}
			file.delete();
		} else if(type == FileType.SQLite && !(plugin.data instanceof SQLite)) {
			try {
				SQLite manager = new SQLite(fileDir.getPath(), file.getName());
				manager.load();
				manager.setTable(Table.ACCOUNTS);
				ResultSet result = manager.getAllUsers();
				while(result.next()) {
					String user = result.getString("username");
					if(!plugin.data.isSet(user)) {
						String pass = result.getString("password");
						plugin.data.setValue(user, ValueType.INSERT, pass, 1);
					}
				}
				manager.close();
				file.delete();
			} catch(SQLException e) {
				System.out.println("[LoginSecurity] FAILED CONVERTING FROM SQLITE TO MYSQL");
				log.warning("[LoginSecurity] "+e.getMessage());
			}
		} else if(type == FileType.OldToNewMySQL) {
			try {
				MySQL from = new MySQL(plugin.getConfig());
				from.load();
				from.setTable(Table.OLD);
				boolean shouldEncrypt = plugin.getConfig().getBoolean("options.use-MD5 Enryption", true);
				ResultSet result = from.getAllUsers();
				while(result.next()) {
					String user = result.getString("username");
					if(!plugin.data.isSet(user)) {
						String pass = result.getString("password");
						if(!shouldEncrypt)
							pass = EncryptionUtil.getMD5(pass);
						plugin.data.setValue(user, ValueType.INSERT, pass, 1);
					}
				}
				from.removeTable("passwords");
				from.close();
			} catch(SQLException e) {
				System.out.println("[LoginSecurity] FAILED CONVERTING FROM SQLITE TO MYSQL");
				log.warning("[LoginSecurity] "+e.getMessage());
			}
		} else if(type == FileType.xAuth) {
			PluginManager pm = Bukkit.getServer().getPluginManager();
			xAuth xauth = (xAuth)pm.getPlugin("xAuth");
			xAuthConv conv = new xAuthConv(xauth);
			conv.convert();
			try {
				ReflectionUtil.unloadPlugin("xAuth");
				plugin.registerCommands();
			} catch (NoSuchFieldException e) {
				log.warning("[LoginSecurity] Failed to unload xAuth: "+e.getMessage());
			} catch (IllegalAccessException e) {
				log.warning("[LoginSecurity] Failed to unload xAuth: "+e.getMessage());
			}
		}
	}
}
