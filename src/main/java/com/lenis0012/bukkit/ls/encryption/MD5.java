package com.lenis0012.bukkit.ls.encryption;

import com.lenis0012.bukkit.ls.util.EncryptionUtil;

public class MD5 implements Encryptor {

	@Override
	public boolean check(String check, String real) {
		check = EncryptionUtil.getMD5(check);
		return check.equals(real);
	}
}
