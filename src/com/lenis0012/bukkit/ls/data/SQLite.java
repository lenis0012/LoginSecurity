package com.lenis0012.bukkit.ls.data;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class SQLite implements DataManager {
	private String filePath;
	private Logger log = Logger.getLogger("Minecraft");
	private Connection con = null;
	private Statement statement = null;
	private String table;
	
	public SQLite(String filePath) {
		this.filePath = filePath;
	}
	
	@Override
	public void load() {
		File file = new File(filePath);
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				log.warning("[LoginSecurity] Failed to create SQLite file: "+e.getMessage());
			}
		}
		if(this.startDriver()) {
			if(this.openConnection()) {
				log.info("[LoginSecurity] Succesfully loaded SQLite");
			}
		}
	}
	
	private boolean startDriver() {
		try {
			Class.forName("org.sqlite.JDBC");
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
			//open connection
			con = DriverManager.getConnection("jdbc:sqlite:"+filePath);
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
	
	public ResultSet getAllUsers() {
		try {
			return statement.executeQuery("SELECT * FROM "+table);
		} catch(SQLException e) {
			log.warning("[LoginSecurity] Could not get data from SQLite: "+e.getMessage());
			return null;
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