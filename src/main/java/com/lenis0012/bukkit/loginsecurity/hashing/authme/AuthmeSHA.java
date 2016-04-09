package com.lenis0012.bukkit.loginsecurity.hashing.authme;

import com.lenis0012.bukkit.loginsecurity.hashing.BasicAlgorithm;
import org.bouncycastle.jcajce.provider.digest.SHA256;

import java.math.BigInteger;
import java.security.MessageDigest;

public class AuthmeSHA extends BasicAlgorithm {
    private final MessageDigest digest;
    private final String prefix;

    public AuthmeSHA(String algorithm) {
        switch(algorithm.toLowerCase()) {
            case "sha256":
                this.digest = new SHA256.Digest();
                this.prefix = "SHA";
                break;
            default:
                throw new IllegalArgumentException("Unknown SHA digest: " + algorithm);
        }
    }

    @Override
    public String hash(String pw) {
        return null;
    }

    @Override
    public boolean check(String pw, String hashed) {
        String[] line = hashed.split("\\$");
        return line.length == 4 && hashed.equals(getHash(pw, line[2]));
    }

    private String getHash(String pw, String salt) {
        return "$" + prefix + "$" + salt + "$" + compute(compute(pw) + salt);
    }

    private String compute(String message) {
        digest.reset();
        digest.update(message.getBytes());
        byte[] hash = digest.digest();
        return String.format("%0" + (hash.length << 1) + "x", new BigInteger(1, hash));
    }
}
