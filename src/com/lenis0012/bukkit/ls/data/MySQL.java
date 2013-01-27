package com.lenis0012.bukkit.ls.data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
				log.info("[LoginSecurity] Succesfully loaded MySQL");
			}
		}
	}
	
	private boolean startDriver() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			return true;
		} catch (ClassNotFoundException e) {
			log.warning("[LoginSecurity] Could not load MySQL driver: "+e.getMessage());
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
			log.warning("[LoginSecurity] Could not create MySQL table: "+e.getMessage());
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
			log.warning("[LoginSecurity] Could not open MySQL connection: "+e.getMessage());
			return false;
		}
	}
	
	@Override
	public Object getValue(String username, String value) {
		try {
			PreparedStatement ps = con.prepareStatement("SELECT * FROM "+table+" WHERE username=?;");
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				return rs.getObject(value);
			else
				return null;
		} catch(SQLException e) {
			log.warning("[LoginSecurity] Could not get data from MySQL: "+e.getMessage());
			return null;
		}
	}
	
	@Override
	public boolean isSet(String username) {
		try {
			ResultSet rs = statement.executeQuery("SELECT * FROM "+table+" WHERE username='"+username+"';");
			return rs.next();
		} catch(SQLException e) {
			log.warning("[LoginSecurity] Could not get data from MySQL: "+e.getMessage());
			return false;
		}
	}
	
	@Override
	public void setValue(String username, ValueType type, String value, int crypto) {
		type.insert(log, con, table, username, value, crypto);
	}
	
	public boolean isCreated(String tbl) {
		try {
			DatabaseMetaData dbm = con.getMetaData();
			ResultSet tables = dbm.getTables(null, null, tbl, null);
			return tables.next();
		} catch(SQLException e) {
			log.warning("[LoginSecurity] Could not set data from MySQL: "+e.getMessage());
			return false;
		}
	}
	
	
	public ResultSet getAllUsers() {
		try {
			return statement.executeQuery("SELECT * FROM "+table);
		} catch(SQLException e) {
			log.warning("[LoginSecurity] Could not get data from MySQL: "+e.getMessage());
			return null;
		}
	}
	
	public void removeTable(String tbl) {
		try {
			statement.executeUpdate("DROP TABLE "+tbl);
		} catch(SQLException e) {
			log.warning("[LoginSecurity] Could not drop data from MySQL: "+e.getMessage());
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
			log.warning("[LoginSecurity] Could not close MySQL connection: "+e.getMessage());
		}
	}
}
