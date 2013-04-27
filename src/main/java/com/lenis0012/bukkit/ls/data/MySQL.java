package com.lenis0012.bukkit.ls.data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;

import com.lenis0012.bukkit.ls.encryption.EncryptionType;

public class MySQL implements DataManager{
	private Logger log = Logger.getLogger("Minecraft.LoginSecruity");
	private FileConfiguration config;
	private Connection con;
	private String table;
	
	public MySQL(FileConfiguration config, String table) {
		this.config = config;
		this.table = table;
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
			st.executeUpdate("CREATE TABLE IF NOT EXISTS " + table);
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
	public boolean isRegistered(String user) {
		try {
			PreparedStatement ps = con.prepareStatement("SELECT * FROM " + table + " WHERE username=?;");
			ps.setString(1, user);
			ResultSet result = ps.executeQuery();
			return result.next();
		} catch(SQLException e) {
			log.log(Level.SEVERE, "Failed to get data from SQLite db", e);
			return false;
		}
	}

	@Override
	public void register(String user, String password, int encryption, String ip) {
		try {
			PreparedStatement ps = con.prepareStatement("INSERT INTO " + table + "(username,password,encryption,ip) VALUES(?,?,?,?);");
			ps.setString(1, user);
			ps.setString(2, password);
			ps.setInt(3, encryption);
			ps.setString(4, ip);
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to create user", e);
		}
	}

	@Override
	public void updatePassword(String user, String password, int encryption) {
		try {
			PreparedStatement ps = con.prepareStatement("UPDATE " + table + " SET password=?,encryption=? WHERE username=?;");
			ps.setString(1, password);
			ps.setInt(2, encryption);
			ps.setString(3, user);
			ps.executeUpdate();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to update user password", e);
		}
	}

	@Override
	public void updateIp(String user, String ip) {
		try {
			PreparedStatement ps = con.prepareStatement("UPDATE " + table + " SET ip=? WHERE username=?;");
			ps.setString(1, ip);
			ps.setString(2, user);
			ps.executeUpdate();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to update user ip", e);
		}
	}

	@Override
	public String getPassword(String user) {
		try {
			PreparedStatement ps = con.prepareStatement("SELECT * FROM " + table + " WHERE username=?;");
			ps.setString(1, user);
			ResultSet result = ps.executeQuery();
			return result.getString("password");
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to get user password", e);
			return null;
		}
	}

	@Override
	public int getEncryptionTypeId(String user) {
		try {
			PreparedStatement ps = con.prepareStatement("SELECT * FROM " + table + " WHERE username=?;");
			ps.setString(1, user);
			ResultSet result = ps.executeQuery();
			return result.getInt("encryption");
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to get user encryption type", e);
			return EncryptionType.MD5.getTypeId();
		}
	}

	@Override
	public String getIp(String user) {
		try {
			PreparedStatement ps = con.prepareStatement("SELECT * FROM " + table + " WHERE username=?;");
			ps.setString(1, user);
			ResultSet result = ps.executeQuery();
			return result.getString("ip");
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to get user ip", e);
			return null;
		}
	}

	@Override
	public void removeUser(String user) {
		try {
			PreparedStatement ps = con.prepareStatement("DELETE FROM " + table + " WHERE username=?;");
			ps.setString(1, user);
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
