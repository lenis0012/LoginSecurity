package com.lenis0012.bukkit.loginsecurity.hashing.deprecated;

import com.lenis0012.bukkit.loginsecurity.hashing.BasicAlgorithm;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.security.MessageDigest;

/**
 * SHA2 hashing algorithm.
 * this is no longer "acceptable", thus deprecated
 */
public class SHA2 extends BasicAlgorithm {
    private final String algorithm;

    public SHA2(String algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public String hash(String pw) {
        return hash(pw, "UTF-8");
    }

    public String hash(String pw, String encoding) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            digest.update(pw.getBytes("UTF-8"));
            byte[] rawDigest = digest.digest();
            return Base64Coder.encodeLines(rawDigest);
        } catch(Exception e) {
            return null;
        }
    }

    @Override
    public boolean check(String pw, String hashed) {
        return hash(pw, "UTF-8").compareTo(hashed) == 0 || hash(pw, "UTF-16").compareTo(hashed) == 0;
    }
}
