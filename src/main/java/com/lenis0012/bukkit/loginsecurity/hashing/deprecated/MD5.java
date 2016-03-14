package com.lenis0012.bukkit.loginsecurity.hashing.deprecated;

import com.lenis0012.bukkit.loginsecurity.hashing.BasicAlgorithm;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * MD5 hashing algorithm.
 * this is no longer "acceptable", thus deprecated
 */
public class MD5 extends BasicAlgorithm {
    @Override
    public String hash(String pw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(pw.getBytes());
            return new BigInteger(1, digest.digest()).toString(16);
        } catch(Exception e) {
            return null;
        }
    }

    @Override
    public boolean check(String pw, String hashed) {
        return hash(pw).compareTo(hashed) == 0;
    }
}
