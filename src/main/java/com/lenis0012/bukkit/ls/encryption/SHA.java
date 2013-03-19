package com.lenis0012.bukkit.ls.encryption;

import com.lenis0012.bukkit.ls.LoginSecurity;
import com.lenis0012.bukkit.ls.util.EncryptionUtil;

public class SHA implements Encryptor {
	private String type;
	
	public SHA(String type) {
		this.type = type;
	}
	
	@Override
	public boolean check(String check, String real) {
		String hashed = hash(check);
		return hashed.equals(real);
	}

	@Override
	public String hash(String value) {
		return EncryptionUtil.encrypt(value, this.type, LoginSecurity.encoder);
	}
}