package com.lenis0012.bukkit.ls.data;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lenis0012.bukkit.ls.encryption.EncryptionType;

public class SQLite implements DataManager {
	private final Logger log = Logger.getLogger("Minecraft.LoginSecurity");
	private File file;
	private Connection con;
	
	public SQLite(File file) {
		this.file = file;
		File dir = file.getParentFile();
		dir.mkdir();
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch(IOException e) {
				log.log(Level.SEVERE, "Failed to create file", e);
			}
		}
		
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			log.log(Level.SEVERE, "Failed to load SQLite driver", e);
		}
		
	}
	
	@Override
	public void openConnection() {
		try {
			this.con = DriverManager.getConnection("jdbc:sqlite:" + file.getPath());
			Statement st = con.createStatement();
			
			st.setQueryTimeout(30);
			st.executeUpdate("CREATE TABLE IF NOT EXISTS users (username VARCHAR(130) NOT NULL UNIQUE,password VARCHAR(300) NOT NULL,encryption INT,ip VARCHAR(130) NOT NULL);");
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to open SQLite connection", e);
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
			PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE username=?;");
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
			PreparedStatement ps = con.prepareStatement("INSERT INTO users(username,password,encryption,ip) VALUES(?,?,?,?);");
			ps.setString(1, user);
			ps.setString(2, password);
			ps.setInt(3, encryption);
			ps.setString(4, ip);
			ps.executeUpdate();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to create user", e);
		}
	}

	@Override
	public void updatePassword(String user, String password, int encryption) {
		try {
			PreparedStatement ps = con.prepareStatement("UPDATE users SET password=?,encryption=? WHERE username=?;");
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
			PreparedStatement ps = con.prepareStatement("UPDATE users SET ip=? WHERE username=?;");
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
			PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE username=?;");
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
			PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE username=?;");
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
			PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE username=?;");
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
			PreparedStatement ps = con.prepareStatement("DELETE FROM users WHERE username=?;");
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
			PreparedStatement ps = con.prepareStatement("SELECT * FROM users");
			return ps.executeQuery();
		} catch (SQLException e) {
			return null;
		}
	}
}