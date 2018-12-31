package com.lenis0012.bukkit.loginsecurity.hashing.deprecated;

import com.lenis0012.bukkit.loginsecurity.hashing.BasicAlgorithm;
import org.bouncycastle.jcajce.provider.digest.Whirlpool.Digest;
import org.bouncycastle.util.encoders.Base64;

import java.util.regex.Pattern;

/**
 * Whirlpool is an AES-like block-cipher.
 *
 * Its useful to iterate, some examples include:
 * 1000  iterations: 20ms
 * 4000  iterations: 60ms (min)
 * 8000  iterations: 81ms
 * 12000 iterations: 116ms (best)
 * 16000 iterations: 148ms
 */
public class Whirlpool extends BasicAlgorithm {
    private static final int ITERATIONS = 12000;
    private final Digest digest;

    public Whirlpool() {
        this.digest = new Digest();
    }

    @Override
    public String hash(String pw) {
        return hash(pw, generateSalt(16), ITERATIONS);
    }

    public String hash(String pw, byte[] salt, int iterations) {
        byte[] hash = pw.getBytes();
        for(int i = 0; i < iterations; i++) {
            digest.reset();
            digest.update(salt);
            hash = digest.digest(hash);
        }
        return Base64.toBase64String(hash) + "$" + Base64.toBase64String(salt) + "$" + iterations;
    }

    @Override
    public boolean check(String pw, String hashed) {
        String[] comp = hashed.split(Pattern.quote("$"));
        byte[] salt = Base64.decode(comp[1]);
        int iterations = Integer.parseInt(comp[2]);
        return hash(pw, salt, iterations).compareTo(hashed) == 0;
    }
}
