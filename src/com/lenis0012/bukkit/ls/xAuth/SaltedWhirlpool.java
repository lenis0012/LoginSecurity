package com.lenis0012.bukkit.ls.xAuth;

import com.lenis0012.bukkit.ls.encryption.Encryptor;
import com.lenis0012.bukkit.ls.util.EncryptionUtil;

public class SaltedWhirlpool implements Encryptor {
	@Override
	public boolean check(String check, String real) {
		check = EncryptionUtil.getSaltedWhirlpool(real, check);
		return check.equals(real);
	}
}
