package com.lenis0012.bukkit.loginsecurity.hashing;

public interface HashFunction {

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
