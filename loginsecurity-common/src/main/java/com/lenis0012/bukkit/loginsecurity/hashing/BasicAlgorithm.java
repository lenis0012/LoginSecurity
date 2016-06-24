package com.lenis0012.bukkit.loginsecurity.hashing;

import java.security.SecureRandom;

public abstract class BasicAlgorithm {

    /**
     * Generate a secure randoms salt.
     *
     * @param bytes Length of salt
     * @return salt
     */
    protected byte[] generateSalt(int bytes) {
        final SecureRandom random = new SecureRandom();
        byte[] salt = new byte[bytes];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Hash a password.
     *
     * @param pw Raw password
     * @return Hashed password
     */
    public abstract String hash(String pw);

    /**
     * Check if a password matches it's hash.
     *
     * @param pw Raw password.
     * @param hashed Hashed password.
     * @return True if matches, False otherwise.
     */
    public abstract boolean check(String pw, String hashed);
}
