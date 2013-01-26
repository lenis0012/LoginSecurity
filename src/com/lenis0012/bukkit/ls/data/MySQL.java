package com.lenis0012.bukkit.ls.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;

public class MySQL implements DataManager{
	private Logger log = Logger.getLogger("Minecraft");
	private FileConfiguration config;
	private Connection con = null;
	private Statement statement = null;
	private String table;
	
	public MySQL(FileConfiguration config) {
		this.config = config;
	}
	
	@Override
	public void load() {
		if(this.startDriver()) {
			if(this.openConnection()) {
				log.info("[LoginSecurity] Succesfully loaded SQLite");
			}
		}
	}
	
	private boolean startDriver() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			return true;
		} catch (ClassNotFoundException e) {
			log.warning("[LoginSecurity] Could not load SQLite driver: "+e.getMessage());
			return false;
		}
	}
	
	@Override
	public void createDefaultTable(String table) {
		try {
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + table +
					" (" + "username VARCHAR(250) NOT NULL UNIQUE,password VARCHAR(250) NOT NULL);");
			this.table = table;
		} catch(SQLException e) {
			log.warning("[LoginSecurity] Could not create SQLite table: "+e.getMessage());
		}
	}
	
	private boolean openConnection() {
		try {
			//get config info
			String host = config.getString("MySQL.host", "localhost");
			String port = String.valueOf(config.getInt("MySQL.port", 3306));
			String databse = config.getString("MySQL.database", "loginsecurity");
			String username = config.getString("MySQL.username", "root");
			String pass = config.getString("MySQL.password", "");
			
			//open connection
			con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port +"/" + databse + "?" +
					"user=" + username + "&password=" + pass);
			statement = con.createStatement();
			
			//set the timeout
			statement.setQueryTimeout(30);
			return true;
		} catch (SQLException e) {
			log.warning("[LoginSecurity] Could not open SQLite connection: "+e.getMessage());
			return false;
		}
	}
	
	@Override
	public Object getValue(String username, String value) {
		try {
			ResultSet rs = statement.executeQuery("select * from "+table+" where username='"+username+"'");
			return rs.getObject(value);
		} catch(SQLException e) {
			log.warning("[LoginSecurity] Could not get data from SQLite: "+e.getMessage());
			return null;
		}
	}
	
	@Override
	public boolean isSet(String username) {
		try {
			ResultSet rs = statement.executeQuery("select * from "+table+" where username='"+username+"'");
			return rs.next();
		} catch(SQLException e) {
			log.warning("[LoginSecurity] Could not get data from SQLite: "+e.getMessage());
			return false;
		}
	}
	
	@Override
	public void setValue(String username, ValueType type, String value) {
		try {
			String data = type.getUsage().replace("{TABLE}", table).
					replace("{USER}", username).replace("{VALUE}", value);
			statement.executeUpdate(data);
		} catch(SQLException e) {
			log.warning("[LoginSecurity] Could not set data from SQLite: "+e.getMessage());
		}
	}
	
	@Override
	public void close() {
		try {
			if(statement != null)
				statement.close();
			if(con != null)
				con.close();
		} catch(SQLException e) {
			log.warning("[LoginSecurity] Could not close SQLite connection: "+e.getMessage());
		}
	}
}
