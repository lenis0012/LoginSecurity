package com.lenis0012.bukkit.ls.data;

import java.sql.Connection;

public interface DataManager {
	/**
	 * Load the manager
	 */
	public void load();
	
	/**
	 * Close the connection
	 */
	public void close();
	
	/**
	 * Get a value from the manager
	 * 
	 * @param 			username username
	 * @param 			value value to recive
	 * @return 			value
	 */
	public Object getValue(String username, String value);
	
	/**
	 * Check if a user is set to the table
	 * 
	 * @param username 	username
	 * @return 			user is set?
	 */
	public boolean isSet(String username);
	
	/**
	 * Insert data into the manager
	 * 
	 * @param username	username
	 * @param type		data type
	 * @param value		value
	 * @param crypto	encryption type
	 */
	public void setValue(String username, ValueType type, String value, Object value2);
	
	/**
	 * Set the default table
	 * 
	 * @param table table
	 */
	public void setTable(Table table);
	
	/**
	 * Get the database connection
	 * 
	 * @return conenction
	 */
	public Connection getConnection();
	
	/**
	 * Open the database connection
	 * 
	 * @return Connection open?
	 */
	public boolean openConnection();
}
