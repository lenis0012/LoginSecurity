package com.lenis0012.bukkit.ls.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
}
