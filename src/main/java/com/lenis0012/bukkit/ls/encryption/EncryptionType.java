package com.lenis0012.bukkit.ls.encryption;

import com.lenis0012.bukkit.ls.xAuth.CryptoDigest;
import com.lenis0012.bukkit.ls.xAuth.SaltedWhirlpool;
import com.lenis0012.bukkit.ls.xAuth.Sha256;
import com.lenis0012.bukkit.ls.xAuth.Whirlpool;

public enum EncryptionType {
	MD5(1, new MD5()),
	PHPBB3(2, new PHPBB3()),
	xAuth_Authme_SHA256(10, new Sha256()),
	xAuth_DEFAULT(11, new SaltedWhirlpool()),
	xAuth_MD5(12, new CryptoDigest("MD5")),
	xAuth_SHA1(13, new CryptoDigest("SHA1")),
	xAuth_SHA256(14, new CryptoDigest("SHA-256")),
	xAuth_WHIRLPOOL(15, new Whirlpool());
	
	private Encryptor cryp;
	private int type;
	
	private EncryptionType(int type, Encryptor cryp) {
		this.cryp = cryp;
		this.type = type;
	}
	
	public boolean checkPass(String check, String real) {
		return cryp.check(check, real);
	}
	
	public String hash(String value) {
		return cryp.hash(value);
	}
	
	public int getTypeId() {
		return type;
	}
	
	public static EncryptionType fromInt(int from) {
		for(EncryptionType type : values()) {
			if(type.type == from)
				return type;
		}
		return null;
	}
	
	public static EncryptionType fromString(String from) {
		if(from.equalsIgnoreCase("md5"))
			return MD5;
		else if(from.equalsIgnoreCase("phpbb3"))
			return PHPBB3;
		else
			return MD5;
	}
}
