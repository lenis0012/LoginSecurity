package com.lenis0012.bukkit.ls.xAuth;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import com.lenis0012.bukkit.ls.encryption.Encryptor;
import com.lenis0012.bukkit.ls.util.EncryptionUtil;

public class Sha256 implements Encryptor {

	@Override
	public boolean check(String check, String real) {
		String salt = real.split("\\$")[2];
        String checked = "$SHA$" + salt + "$" + hash(hash(check, "SHA-256") + salt, "SHA-256");
		return checked.equals(real);
	}
	
    // xAuth's custom hashing technique
    public String hash(String toHash) {
        String salt = whirlpool(UUID.randomUUID().toString()).substring(0, 12);
        String hash = whirlpool(salt + toHash);
        int saltPos = (toHash.length() >= hash.length() ? hash.length() - 1 : toHash.length());
        return hash.substring(0, saltPos) + salt + hash.substring(saltPos);
    }

    private String hash(String toHash, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(toHash.getBytes());
            byte[] digest = md.digest();
            return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private String whirlpool(String toHash) {
    	return EncryptionUtil.getWhirlpool(toHash);
    }
}
