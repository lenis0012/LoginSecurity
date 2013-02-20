package com.lenis0012.bukkit.ls.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.lenis0012.bukkit.ls.xAuth.Whirlpool;

public class EncryptionUtil {
	public static String getMD5(String value) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(value.getBytes(), 0 ,value.length());
			String md5 = new BigInteger(1, md.digest()).toString(16);
			return md5;
		} catch (NoSuchAlgorithmException e) {
			return value;
		}
	}
	
	public static String getSaltedWhirlpool(String realPass, String checkPass) {
		int saltPos = (checkPass.length() >= realPass.length() ? realPass.length() - 1 : checkPass.length());
		String salt = realPass.substring(saltPos, saltPos + 12);
		String hash = getWhirlpool(salt + checkPass);
		return hash.substring(0, saltPos) + salt + hash.substring(saltPos);
	}
	
	public static String getWhirlpool(String value) {
		Whirlpool w = new Whirlpool();
		byte[] digest = new byte[Whirlpool.DIGESTBYTES];
		w.NESSIEinit();
		w.NESSIEadd(value);
		w.NESSIEfinalize(digest);
		return Whirlpool.display(digest);
	}
}
