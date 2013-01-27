package com.lenis0012.bukkit.ls;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
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
import com.lenis0012.bukkit.ls.util.Updater.UpdateResult;
import com.lenis0012.bukkit.ls.util.Updater.UpdateType;

public class LoginSecurity extends JavaPlugin {
	public DataManager data;
	public static LoginSecurity instance;
	public HashMap<String, Boolean> AuthList = new HashMap<String, Boolean>();
	public boolean required, blindness, sesUse;
	public int sesDelay;
	public Updater updater = null;
	public Logger log = Logger.getLogger("Minecraft");
	public ThreadManager thread;
	
	@Override
	public void onEnable() {
		//setup quickcalls
		FileConfiguration config = this.getConfig();
		PluginManager pm = this.getServer().getPluginManager();
		
		//setup config
		config.addDefault("settings.password-required", false);
		config.addDefault("settings.blindness", true);
		config.addDefault("settings.session.use", true);
		config.addDefault("settings.session.timeout (sec)", 60);
		config.addDefault("settings.table prefix", "ls_");
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
		data.createDefaultTable(config.getString("settings.table prefix")+"Logins");
		thread = new ThreadManager(this);
		thread.startMsgTask();
		required = config.getBoolean("settings.password-required");
		blindness = config.getBoolean("settings.blindness");
		sesUse = config.getBoolean("settings.session.use");
		sesDelay = config.getInt("settings.session.timeout (sec)");
		if(sesUse)
			thread.startSessionTask();
		
		//convert everything
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
		thread.stopMsgTask();
		thread.stopSessionTask();
	}
	
	private DataManager getDataManager(FileConfiguration config) {
		if(config.getBoolean("MySQL.use")) {
			return new MySQL(config);
		} else {
			return new SQLite("plugins/LoginSecurity/data.db");
		}
	}
	
	private void checkConverter() {
		PluginManager pm = this.getServer().getPluginManager();
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
		Plugin xAuth = pm.getPlugin("xAuth");
		if(xAuth != null) {
			if(xAuth.isEnabled()) {
				Converter conv = new Converter(FileType.xAuth, this.getFile());
				conv.convert();
				log.info("[LoginSecurity] Converted data from xAuth to LoginSecurity");
			}
		}
	}
	
	public void showVersion(final Player p) {
		this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				Player pla = p;
				if(pla.hasPermission("ls.admin")) {
					if(updater != null) {
						updater.update(UpdateType.NO_DOWNLOAD, false);
						if(updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
							pla.sendMessage(ChatColor.GREEN+"LoginSecurity has a new update, check BukkitDev");
						} else
							pla.sendMessage(ChatColor.GREEN+"LoginSecurity did not find updates");
					}
				}
			}
		}, 25);
	}
}
