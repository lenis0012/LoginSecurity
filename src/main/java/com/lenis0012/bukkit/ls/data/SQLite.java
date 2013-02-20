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
	private String fileName;
	private String fileDir;
	private Logger log = Logger.getLogger("Minecraft");
	private Connection con = null;
	private Statement statement = null;
	private Table table;
	
	public SQLite(String fileDir, String fileName) {
		this.fileName = fileName;
		this.fileDir = fileDir;
	}
	
	@Override
	public void load() {
		File file = new File(fileDir+fileName);
		File dir = new File(fileDir);
		dir.mkdirs();
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
	public void setTable(Table table) {
		try {
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS "+table.getName()+
					table.getUsage()+";");
			this.table = table;
		} catch(SQLException e) {
			log.warning("[LoginSecurity] Could not create SQLite table: "+e.getMessage());
		}
	}
	
	private boolean openConnection() {
		try {
			//open connection
			con = DriverManager.getConnection("jdbc:sqlite:"+fileDir+fileName);
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
			ResultSet rs = statement.executeQuery("select * from "+table.getName()+" where username='"+username+"'");
			return rs.getObject(value);
		} catch(SQLException e) {
			log.warning("[LoginSecurity] Could not get data from SQLite: "+e.getMessage());
			return null;
		}
	}
	
	@Override
	public boolean isSet(String username) {
		try {
			ResultSet rs = statement.executeQuery("select * from "+table.getName()+" where username='"+username+"'");
			return rs.next();
		} catch(SQLException e) {
			log.warning("[LoginSecurity] Could not get data from SQLite: "+e.getMessage());
			return false;
		}
	}
	
	@Override
	public void setValue(String username, ValueType type, String value, Object value2) {
		type.insert(log, con, table, username, value, value2);
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
	public Connection getConnection() {
		return this.con;
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