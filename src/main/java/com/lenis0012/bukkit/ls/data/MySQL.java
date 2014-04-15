package com.lenis0012.bukkit.ls.data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import com.lenis0012.bukkit.ls.LoginSecurity;
import com.lenis0012.bukkit.ls.encryption.EncryptionType;

public class MySQL implements DataManager {
	public static boolean IS_CONVERTING = false;
	
	private Logger log = Logger.getLogger("Minecraft.LoginSecruity");
	private FileConfiguration config;
	private Connection con;
	private String table;
	
	public MySQL(FileConfiguration config, final String table) {
		this.config = config;
		this.table = table;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			log.log(Level.SEVERE, "Failed to load MySQL driver", e);
		}
		
		this.openConnection();
		try {
			DatabaseMetaData md = con.getMetaData();
			ResultSet rs = md.getColumns(null, null, table, "username");
			if(rs.next()) {
				log.log(Level.INFO, "Username column was detected, conversion to UUID will begin in 10 seconds.");
				log.log(Level.INFO, "This can not be reversed, stop the server NOW if you don't want this.");
				IS_CONVERTING = true;
				Bukkit.getScheduler().runTaskLaterAsynchronously(LoginSecurity.instance, new Runnable() {

					@Override
					public void run() {
						try {
							log.log(Level.INFO, "Conversion to UUID has started, this may take some time");
							List<Object[]> loginData = new ArrayList<Object[]>();
							PreparedStatement ps = con.prepareStatement("SELECT * FROM " + table);
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
							
							log.log(Level.INFO, "Loaded " + loginData.size() + " columns, starting to convert usernames to uuid");
							long beginTime = System.currentTimeMillis();
							Converter.getUUIDByUsername("lenis0012");
							long timeTaken = System.currentTimeMillis() - beginTime;
							long etaDuration = timeTaken * loginData.size() / 1000;
							log.log(Level.INFO, "The total process will take at least " + etaDuration + " seconds");
							
							dropTable(table);
							closeConnection();
							openConnection();
							for(Object[] data : loginData) {
								String uuid = Converter.getUUIDByUsername((String) data[0]);
								register(uuid, (String) data[1], (Integer) data[2], (String) data[3]);
							}
							
							log.log(Level.INFO, "Conversion completed.");
							IS_CONVERTING = false;
						} catch (SQLException e) {
							log.log(Level.SEVERE, "Failed to convert to uuid, this is bad.");
						}
					}
				}, 20L * 10);
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to check if username column exists");
		}
	}

	@Override
	public void openConnection() {
		String host = config.getString("MySQL.host", "localhost");
		String port = String.valueOf(config.getInt("MySQL.port", 3306));
		String database = config.getString("MySQL.database", "bukkit");
		String user = config.getString("MySQL.username", "root");
		String pass = config.getString("MySQL.password", "");
		
		try {
			this.con = DriverManager.getConnection("jdbc:mysql://"+host+':'+port+
					'/'+database+'?'+"user="+user+"&password="+pass);
			
			Statement st = con.createStatement();
			st.setQueryTimeout(30);
			st.executeUpdate("CREATE TABLE IF NOT EXISTS " + table + " (unique_user_id VARCHAR(130) NOT NULL UNIQUE,password VARCHAR(300) NOT NULL,encryption INT,ip VARCHAR(130) NOT NULL);");
		} catch(SQLException e) {
			log.log(Level.SEVERE, "Faield to load MySQL", e);
		}
	}

	@Override
	public void closeConnection() {
		try {
			if(con != null)
				con.close();
		} catch(SQLException e) {
			log.log(Level.SEVERE, "Failed to close SQLite connection", e);
		}
	}

	@Override
	public boolean isRegistered(String uuid) {
		try {
			PreparedStatement ps = con.prepareStatement("SELECT * FROM " + table + " WHERE unique_user_id=?;");
			ps.setString(1, uuid);
			ResultSet result = ps.executeQuery();
			return result.next();
		} catch(SQLException e) {
			log.log(Level.SEVERE, "Failed to get data from SQLite db", e);
			return false;
		}
	}

	@Override
	public void register(String uuid, String password, int encryption, String ip) {
		try {
			PreparedStatement ps = con.prepareStatement("INSERT INTO " + table + "unique_user_id,password,encryption,ip) VALUES(?,?,?,?);");
			ps.setString(1, uuid);
			ps.setString(2, password);
			ps.setInt(3, encryption);
			ps.setString(4, ip);
			ps.executeUpdate();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to create user", e);
		}
	}

	@Override
	public void updatePassword(String uuid, String password, int encryption) {
		try {
			PreparedStatement ps = con.prepareStatement("UPDATE " + table + " SET password=?,encryption=? WHERE unique_user_id=?;");
			ps.setString(1, password);
			ps.setInt(2, encryption);
			ps.setString(3, uuid);
			ps.executeUpdate();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to update user password", e);
		}
	}

	@Override
	public void updateIp(String uuid, String ip) {
		try {
			PreparedStatement ps = con.prepareStatement("UPDATE " + table + " SET ip=? WHERE unique_user_id=?;");
			ps.setString(1, ip);
			ps.setString(2, uuid);
			ps.executeUpdate();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to update user ip", e);
		}
	}

	@Override
	public String getPassword(String uuid) {
		try {
			PreparedStatement ps = con.prepareStatement("SELECT * FROM " + table + " WHERE unique_user_id=?;");
			ps.setString(1, uuid);
			ResultSet result = ps.executeQuery();
			if(result.next())
				return result.getString("password");
			else
				return null;
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to get user password", e);
			return null;
		}
	}

	@Override
	public int getEncryptionTypeId(String uuid) {
		try {
			PreparedStatement ps = con.prepareStatement("SELECT * FROM " + table + " WHERE unique_user_id=?;");
			ps.setString(1, uuid);
			ResultSet result = ps.executeQuery();
			if(result.next())
				return result.getInt("encryption");
			else
				return EncryptionType.MD5.getTypeId();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to get user encryption type", e);
			return EncryptionType.MD5.getTypeId();
		}
	}

	@Override
	public String getIp(String uuid) {
		try {
			PreparedStatement ps = con.prepareStatement("SELECT * FROM " + table + " WHERE unique_user_id=?;");
			ps.setString(1, uuid);
			ResultSet result = ps.executeQuery();
			if(result.next())
				return result.getString("ip");
			else
				return null;
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to get user ip", e);
			return null;
		}
	}

	@Override
	public void removeUser(String uuid) {
		try {
			PreparedStatement ps = con.prepareStatement("DELETE FROM " + table + " WHERE unique_user_id=?;");
			ps.setString(1, uuid);
			ps.executeUpdate();
		} catch(SQLException e) {
			log.log(Level.SEVERE, "Failed to remove user", e);
		}
	}

	@Override
	public Connection getConnection() {
		return this.con;
	}

	@Override
	public ResultSet getAllUsers() {
		try {
			PreparedStatement ps = con.prepareStatement("SELECT * FROM " + table + "");
			return ps.executeQuery();
		} catch (SQLException e) {
			return null;
		}
	}
	
	public boolean tableExists(String name) {
		try {
			DatabaseMetaData dbm = con.getMetaData();
			ResultSet tables = dbm.getTables(null, null, name, null);
			return tables.next();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to check if table exists", e);
			return false;
		}
	}
	
	public void dropTable(String name) {
		try {
			Statement st = con.createStatement();
			st.setQueryTimeout(30);
			st.executeUpdate("DROP TABLE " + name);
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to drop table", e);
		}
	}
}
