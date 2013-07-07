package com.lenis0012.bukkit.ls;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.lenis0012.bukkit.ls.commands.AdminCommand;
import com.lenis0012.bukkit.ls.commands.ChangePassCommand;
import com.lenis0012.bukkit.ls.commands.LoginCommand;
import com.lenis0012.bukkit.ls.commands.LogoutCommand;
import com.lenis0012.bukkit.ls.commands.RegisterCommand;
import com.lenis0012.bukkit.ls.commands.RmPassCommand;
import com.lenis0012.bukkit.ls.data.Converter;
import com.lenis0012.bukkit.ls.data.Converter.FileType;
import com.lenis0012.bukkit.ls.data.DataManager;
import com.lenis0012.bukkit.ls.data.MySQL;
import com.lenis0012.bukkit.ls.data.SQLite;
import com.lenis0012.bukkit.ls.encryption.EncryptionType;
import com.lenis0012.bukkit.ls.util.Metrics;
import org.bukkit.GameMode;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LoginSecurity extends JavaPlugin {
	public DataManager data;
	public static LoginSecurity instance;
	public Map<String, Boolean> AuthList = new HashMap<String, Boolean>();
	public Map<String, Location> loginLocations = new HashMap<String, Location>();
	public List<String> messaging = new ArrayList<String>();
	public boolean required, blindness, sesUse, timeUse, messager, spawntp, godMode;
	public GameMode oldGameMode;
	public int sesDelay, timeDelay;
	public Logger log = Logger.getLogger("Minecraft");
	public ThreadManager thread;
	public String prefix;
	public EncryptionType hasher;
	public Map<String, CommandExecutor> commandMap = new HashMap<String, CommandExecutor>();
	public static int PHP_VERSION;
	public static String encoder;
	
	@Override
	public void onEnable() {
		//setup quickcalls
		FileConfiguration config = this.getConfig();
		PluginManager pm = this.getServer().getPluginManager();
		
		//setup config
		config.addDefault("settings.password-required", false);
		config.addDefault("settings.encryption", "MD5");
		config.addDefault("settings.encoder", "UTF-8");
		config.addDefault("settings.PHP_VERSION", 4);
		config.addDefault("settings.messager-api", true);
		config.addDefault("settings.blindness", true);
		config.addDefault("settings.godMode", true);
		config.addDefault("settings.fake-location", false);
		config.addDefault("settings.session.use", true);
		config.addDefault("settings.session.timeout (sec)", 60);
		config.addDefault("settings.timeout.use", true);
		config.addDefault("settings.timeout.timeout (sec)", 60);
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
		messager = config.getBoolean("settings.messager-api", false);
		prefix = config.getString("settings.table prefix");
		data = this.getDataManager(config, "users.db");
		data.openConnection();
		thread = new ThreadManager(this);
		thread.startMsgTask();
		required = config.getBoolean("settings.password-required");
		blindness = config.getBoolean("settings.blindness");
		godMode = config.getBoolean("settings.godMode");
		spawntp = config.getBoolean("settings.fake-location");
		sesUse = config.getBoolean("settings.session.use", true);
		sesDelay = config.getInt("settings.session.timeout (sec)", 60);
		timeUse = config.getBoolean("settings.timeout.use", true);
		timeDelay = config.getInt("settings.timeout.timeout (sec)", 60);
		PHP_VERSION = config.getInt("settings.PHP_VERSION", 4);
		this.hasher = EncryptionType.fromString(config.getString("settings.encryption"));
		String enc = config.getString("settings.encoder");
		if(enc.equalsIgnoreCase("utf-16"))
			encoder = "UTF-16";
		else
			encoder = "UTF-8";
		
		if(sesUse)
			thread.startSessionTask();
		if(timeUse)
			thread.startTimeoutTask();
		
		thread.startMainTask();
		thread.startMsgTask();
		
		//convert everything
		this.checkConverter();
		
		//register events
		if(messager) {
			Bukkit.getMessenger().registerIncomingPluginChannel(this, "LoginSecurity", new LoginMessager(this));
			Bukkit.getMessenger().registerOutgoingPluginChannel(this, "LoginSecurity");
		}
		pm.registerEvents(new LoginListener(this), this);
		this.registerCommands();
		
		//clear old config
		if(config.contains("options")) {
			config.set("options", null);
			this.saveConfig();
		}
		
		//metrics
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
			if(config.getBoolean("settings.update-checker")) {
				//TODO: Recode update checker
			}
		} catch(Exception e) {
			log.info("[LoginSecurity] Failed sending stats to mcstats.org");
		}
	}
	
	@Override
	public void onDisable() {
		if(data != null)
			data.closeConnection();
		if(thread != null) {
			thread.stopMsgTask();
			thread.stopSessionTask();
		}
	}
	
	private DataManager getDataManager(FileConfiguration config, String fileName) {
		if(config.getBoolean("MySQL.use")) {
			return new MySQL(config, "users");
		} else {
			return new SQLite(new File(this.getDataFolder(), fileName));
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
			if(mysql.tableExists("passwords")) {
				Converter conv = new Converter(FileType.OldToNewMySQL, null);
				conv.convert();
			}
		}
		Plugin xAuth = pm.getPlugin("xAuth");
		if(xAuth != null) {
			if(xAuth.isEnabled()) {
				Converter conv = new Converter(FileType.xAuth, null);
				conv.convert();
				log.info("[LoginSecurity] Converted data from xAuth to LoginSecurity");
			}
		}
	}
	
	public void registerCommands() {
		this.commandMap.clear();
		this.commandMap.put("login", new LoginCommand());
		this.commandMap.put("register", new RegisterCommand());
		this.commandMap.put("changepass", new ChangePassCommand());
		this.commandMap.put("rmpass", new RmPassCommand());
		this.commandMap.put("logout", new LogoutCommand());
		this.commandMap.put("lac", new AdminCommand());
		
		for(Entry<String, CommandExecutor> entry : this.commandMap.entrySet()) {
			String cmd = entry.getKey();
			CommandExecutor ex = entry.getValue();
			
			this.getCommand(cmd).setExecutor(ex);
		}
	}
	
	
	public boolean checkLastIp(Player player) {
		String name = player.getName().toLowerCase();
		if(data.isRegistered(name)) {
			String lastIp = data.getIp(name);
			String currentIp = player.getAddress().getAddress().toString();
			return lastIp.equalsIgnoreCase(currentIp);
		}
		
		return false;
	}
	
	public void debilitatePlayer(Player player) {
		final String name = player.getName().toLowerCase();
		if (godMode) 
			oldGameMode = player.getGameMode();
			player.setGameMode(GameMode.CREATIVE);
		if (blindness)
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1728000, 15));	
		if (spawntp) {
			loginLocations.put(name, player.getLocation().clone());
			player.teleport(player.getWorld().getSpawnLocation());
		}
			
	}
	
	public void rehabPlayer(Player player, String name) {
		final String name = player.getName().toLowerCase();
		player.setGameMode(oldGameMode);
		player.removePotionEffect(PotionEffectType.BLINDNESS);
		if (spawntp) {
				if(loginLocations.containsKey(name))
				player.teleport(loginLocations.remove(name));
		}
	}
	
	public void sendCustomPayload(Player player, String msg) {
		if(!player.getListeningPluginChannels().contains(this.getName()))
			return;
		
		player.sendPluginMessage(this, this.getName(), msg.getBytes());
	}

}
