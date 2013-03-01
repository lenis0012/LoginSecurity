package com.lenis0012.bukkit.ls.xAuth;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.lenis0012.bukkit.ls.encryption.Encryptor;

public class CryptoDigest implements Encryptor {
	private String crypto;
	
	public CryptoDigest(String crypto) {
		this.crypto = crypto;
	}

	@Override
	public boolean check(String check, String real) {
		try {
			MessageDigest md = MessageDigest.getInstance(crypto);
			md.update(check.getBytes());
			byte[] digest = md.digest();
			String checked = String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest));
			return checked.equals(real);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public String hash(String value) {
		return null;
	}

}
