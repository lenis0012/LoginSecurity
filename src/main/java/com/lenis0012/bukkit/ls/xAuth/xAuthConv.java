package com.lenis0012.bukkit.ls.xAuth;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang.RandomStringUtils;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.database.Table;
import com.cypherx.xauth.password.PasswordType;
import com.lenis0012.bukkit.ls.LoginSecurity;

public class xAuthConv {
	private xAuth auth;
	private LoginSecurity plugin;
	
	public xAuthConv(xAuth auth) {
		this.auth = auth;
		this.plugin = LoginSecurity.instance;
	}
	
	public void convert() {
		try {
			String table = auth.getDatabaseController().getTable(Table.ACCOUNT);
			String sql = "SELECT * FROM "+table;
			Connection con = auth.getDatabaseController().getConnection();
			Statement st = con.createStatement();
			ResultSet result = st.executeQuery(sql);
			while(result.next()) {
				String username = result.getString("playername");
				String password = result.getString("password");
				PasswordType type = PasswordType.getType(result.getInt("pwtype"));
				int key = getEncryptor(type);
				if(!plugin.data.isRegistered(username))
					plugin.data.register(username, password, key, RandomStringUtils.randomAscii(25));
			}
		} catch(SQLException e) {
			//Failed to load xAuth
			e.printStackTrace();
		}
	}
	
	private int getEncryptor(PasswordType type) {
		if(type == PasswordType.AUTHME_SHA256)
			return 10;
		else if(type == PasswordType.DEFAULT)
			return 11;
		else if(type == PasswordType.MD5)
			return 12;
		else if(type == PasswordType.SHA1)
			return 13;
		else if(type == PasswordType.SHA256)
			return 14;
		else if(type == PasswordType.WHIRLPOOL)
			return 15;
		return 0;
	}
}
