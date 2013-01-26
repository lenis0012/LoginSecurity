package com.lenis0012.bukkit.ls;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.lenis0012.bukkit.ls.commands.ChangePassCommand;
import com.lenis0012.bukkit.ls.commands.LoginCommand;
import com.lenis0012.bukkit.ls.commands.RegisterCommand;
import com.lenis0012.bukkit.ls.commands.RmPassCommand;
import com.lenis0012.bukkit.ls.data.Converter;
import com.lenis0012.bukkit.ls.data.Converter.FileType;
import com.lenis0012.bukkit.ls.data.DataManager;
import com.lenis0012.bukkit.ls.data.MySQL;
import com.lenis0012.bukkit.ls.data.SQLite;
import com.lenis0012.bukkit.ls.util.Metrics;
import com.lenis0012.bukkit.ls.util.Updater;

public class LoginSecurity extends JavaPlugin {
	public DataManager data;
	public static LoginSecurity instance;
	public HashMap<String, Boolean> AuthList = new HashMap<String, Boolean>();
	public boolean required, blindness;
	public Updater updater = null;
	public Logger log = Logger.getLogger("Minecraft");
	
	@Override
	public void onEnable() {
		//setup quickcalls
		FileConfiguration config = this.getConfig();
		PluginManager pm = this.getServer().getPluginManager();
		
		//setup config
		config.addDefault("settings.password-required", false);
		config.addDefault("settings.blindness", true);
		config.addDefault("MySQL.use", false);
		config.addDefault("MySQL.host", "localhost");
		config.addDefault("MySQL.port", 3306);
		config.addDefault("MySQL.database", "LoginSecurity");
		config.addDefault("MySQL.username", "root");
		config.addDefault("MySQL.password", "password");
		config.options().copyDefaults(true);
		saveConfig();
		
		//intalize fields
		instance = (LoginSecurity)pm.getPlugin("LoginSecurity");
		data = this.getDataManager(config);
		data.load();
		data.createDefaultTable("Logins");
		required = config.getBoolean("settings.password-required");
		blindness = config.getBoolean("settings.blindness");
		this.checkConverter();
		
		//register events
		pm.registerEvents(new LoginListener(this), this);
		getCommand("login").setExecutor(new LoginCommand());
		getCommand("register").setExecutor(new RegisterCommand());
		getCommand("changepass").setExecutor(new ChangePassCommand());
		getCommand("rmpass").setExecutor(new RmPassCommand());
		
		//clear old config
		if(config.contains("options")) {
			config.set("options", null);
			this.saveConfig();
		}
		
		//metrics
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
			if(config.getBoolean("settings.update-checker"))
				this.updater = new Updater(this, log, "loginsecurity", this.getFile(), "ls.admin");
		} catch(Exception e) {
			log.info("[LoginSecurity] Failed sending stats to mcstats.org");
		}
	}
	
	@Override
	public void onDisable() {
		data.close();
	}
	
	private DataManager getDataManager(FileConfiguration config) {
		if(config.getBoolean("MySQL.use")) {
			return new MySQL(config);
		} else {
			return new SQLite("plugins/LoginSecurity/data.db");
		}
	}
	
	private void checkConverter() {
		File file;
		file = new File(this.getDataFolder(), "data.yml");
		if(file.exists()) {
			Converter conv = new Converter(FileType.YAML, file);
			conv.convert();
		}
		file = new File(this.getDataFolder(), "data.db");
		if(file.exists() && data instanceof MySQL) {
			Converter conv = new Converter(FileType.SQLite, file);
			conv.convert();
		}
		if(data instanceof MySQL) {
			MySQL mysql = (MySQL)data;
			if(mysql.isCreated("passwords")) {
				Converter conv = new Converter(FileType.OldToNewMySQL, this.getFile());
				conv.convert();
			}
		}
	}
}
