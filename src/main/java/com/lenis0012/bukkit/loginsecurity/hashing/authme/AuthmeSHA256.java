package com.lenis0012.bukkit.loginsecurity.hashing.authme;

import com.lenis0012.bukkit.loginsecurity.hashing.BasicAlgorithm;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthmeSHA256 extends BasicAlgorithm {

    public AuthmeSHA256() {

    }

    @Override
    public String hash(String pw) {
        return null;
    }

    @Override
    public boolean check(String pw, String hashed) {
        final String[] components = hashed.split("\\$");
        final String salt = components[2];
        final String compareHash = "$SHA$" + salt + "$" + hashSha256(hashSha256(pw) + salt);
        return hashed.equals(compareHash);
    }

    private String hashSha256(String message) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update(message.getBytes());
            byte[] output = digest.digest();
            return String.format("%0" + (output.length << 1) + "x", new BigInteger(1, output));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
