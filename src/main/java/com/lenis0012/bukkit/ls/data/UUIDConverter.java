package com.lenis0012.bukkit.ls.data;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

import com.google.common.base.Charsets;
import com.lenis0012.bukkit.ls.LoginSecurity;
import com.lenis0012.bukkit.ls.util.SpigotUtil;
import com.lenis0012.bukkit.ls.util.UUIDFetcher;

public class UUIDConverter {
	public static boolean IS_CONVERTING = false;
	
	private final boolean isUsingSQLite;
	private final DataManager database;
	private final Logger logger;
	private final String table;
	
	public UUIDConverter(DataManager database, Logger logger, String table) {
		this.isUsingSQLite = database instanceof SQLite;
		this.database = database;
		this.logger = logger;
		this.table = table;
	}
	
	public void convert() {
		try {
			DatabaseMetaData md = database.getConnection().getMetaData();
			ResultSet rs = md.getColumns(null, null, table, "username");
			if(rs.next()) {
				logger.log(Level.INFO, "Username column was detected, conversion to UUID will begin in 20 seconds.");
				logger.log(Level.INFO, "This can not be reversed, stop the server NOW if you don't want this.");
				IS_CONVERTING = true;
				Bukkit.getScheduler().runTaskLaterAsynchronously(LoginSecurity.instance, new Runnable() {

					@Override
					public void run() {
						try {
							logger.log(Level.INFO, "Conversion to UUID has started, this may take some time");
							List<String> usernames = new ArrayList<String>();
							List<Object[]> loginData = new ArrayList<Object[]>();
							PreparedStatement ps = database.getConnection().prepareStatement("SELECT * FROM " + table);
							ResultSet result = ps.executeQuery();
							while(result.next()) {
								Object[] data = new Object[] {
									result.getString("username"),
									result.getString("password"),
									result.getInt("encryption"),
									result.getString("ip")
								};
								
								loginData.add(data);
							}
							
							logger.log(Level.INFO, "Loaded " + loginData.size() + " columns, starting to convert usernames to uuid");
							UUIDFetcher fetcher = new UUIDFetcher(usernames);
							Map<String, UUID> uuids;
							
							try {
								if(Bukkit.getOnlineMode()) {
									uuids = fetcher.call();
								} else {
									if(SpigotUtil.isBungee()) {
										logger.log(Level.INFO, "Bungeecord-mode detected. uuids will be converted with online-mode");
										uuids = fetcher.call();
									} else {
										logger.log(Level.INFO, "Offline-mode detected. uuids will be converted locally");
										uuids = new HashMap<String, UUID>();
										for(String name : usernames) {
											uuids.put(name, UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)));
										}
									}
								}
							} catch (Exception e) {
								logger.log(Level.SEVERE, "Failed to convert uuids", e);
								return;
							}
							
							if(isUsingSQLite) {
								database.closeConnection();
								database.openConnection();
								database.getConnection().createStatement().executeUpdate("DROP TABLE users;");
							} else {
								((MySQL) database).dropTable(table);
							}
							
							database.closeConnection();
							database.openConnection();
							for(Object[] data : loginData) {
								String uuid = uuids.get((String) data[0]).toString();
								database.register(uuid, (String) data[1], (Integer) data[2], (String) data[3]);
							}
							
							logger.log(Level.INFO, "Conversion completed.");
							IS_CONVERTING = false;
						} catch (SQLException e) {
							logger.log(Level.SEVERE, "Failed to convert to uuid, this is bad.");
						}
					}
				}, 20L * 20);
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to check if username column exists");
		}
	}
}
