package com.lenis0012.bukkit.loginsecurity.hashing.active;

import com.lenis0012.bukkit.loginsecurity.hashing.BasicAlgorithm;
import org.bouncycastle.jcajce.provider.digest.SHA3.*;
import org.bouncycastle.util.encoders.Base64;

import java.util.regex.Pattern;

/**
 * A more modern version of SHA2.
 * It is eventually supposed to replace SHA2 entirely.
 *
 * It is smart to iterate this, here are some examples.
 * 500   iterations: 50ms (min)
 * 4000  iterations: 100ms (best)
 * 8000  iterations: 120ms
 * 32000 iterations: 270ms
 * 64000 iterations: 480ms (very secure)
 * 128000 iterations: 900ms
 */
public class SHA3 extends BasicAlgorithm {
    private static final int ITERATIONS = 4000;
    private final DigestSHA3 digest;

    public SHA3(int bits) {
        switch(bits) {
            case 224:
                this.digest = new Digest224();
                break;
            case 256:
                this.digest = new Digest256();
                break;
            case 384:
                this.digest = new Digest384();
                break;
            case 512:
                this.digest = new Digest512();
                break;
            default:
                throw new IllegalArgumentException("Unknown hashing algorithm length: " + bits);
        }
    }

    @Override
    public String hash(String pw) {
        // Return password with a new random salt.
        return hash(pw, generateSalt(16), ITERATIONS);
    }

    public String hash(String pw, byte[] salt, int iterations) {
        // Generate hash
        byte[] hash = pw.getBytes();
        for(int i = 0; i < iterations; i++) { // iterate multiple times to slow process
            digest.reset();
            digest.update(salt);
            hash = digest.digest(pw.getBytes());
        }

        // Generate string
        return Base64.toBase64String(hash) + "$" + Base64.toBase64String(salt) + "$" + iterations;
    }

    @Override
    public boolean check(String pw, String hashed) {
        // Get salt
        String[] components = hashed.split(Pattern.quote("$"));
        byte[] salt = Base64.decode(components[1]);
        int iterations = Integer.parseInt(components[2]);

        // Check
        return hash(pw, salt, iterations).compareTo(hashed) == 0;
    }
}
