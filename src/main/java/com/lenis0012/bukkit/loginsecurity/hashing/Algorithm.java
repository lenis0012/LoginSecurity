package com.lenis0012.bukkit.loginsecurity.hashing;

import com.lenis0012.bukkit.loginsecurity.hashing.active.*;
import com.lenis0012.bukkit.loginsecurity.hashing.deprecated.MD5;
import com.lenis0012.bukkit.loginsecurity.hashing.deprecated.PHPBB3;
import com.lenis0012.bukkit.loginsecurity.hashing.deprecated.SHA2;

/**
 * All algorithms supported by LoginSecurity.
 *
 * IDs:
 * 0-10  = Legacy LoginSecurity
 * 10-20 = xAuth
 * 20-30 = Modern LoginSecurity
 */
public enum Algorithm {
    /**
     * LoginSecurity active.
     */
    BCRYPT(false, 7, new BCrypt()), // 7 Because BCrypt is the only legacy algorithm that is still supported
    SCRYPT(false, 21, new SCrypt()),
    ARGON2(false, 22, new Argon2()),
    PBKDF2(false, 23, new PBKDF2()),
    SHA3_256(false, 24, new SHA3(256)),
    WHIRLPOOL(false, 25, new Whirlpool()),

    /**
     * LoginSecurity deprecated.
     */
    MD5(true, 1, new MD5()),
    SHA(true, 4, new SHA2("SHA")),
    SHA1(true, 3, new SHA2("SHA-1")),
    SHA256(true, 5, new SHA2("SHA-256")),
    SHA512(true, 6, new SHA2("SHA-512")),
    PHPBB3(true, 2, new PHPBB3()),

    /**
     * xAuth.
     */
    xAuth_Authme_SHA256(true, 10, null),
    xAuth_DEFAULT(true, 11, null),
    xAuth_MD5(true, 12, null),
    xAuth_SHA1(true, 13, null),
    xAuth_SHA256(true, 14, null),
    xAuth_WHIRLPOOL(true, 15, null);

    private final boolean deprecated;
    private final BasicAlgorithm algorithm;
    private final int id;

    Algorithm(boolean deprecated, int id, BasicAlgorithm algorithm) {
        this.deprecated = deprecated;
        this.algorithm = algorithm;
        this.id = id;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public int getId() {
        return id;
    }

    public String hash(String password) {
        return algorithm.hash(password);
    }

    public boolean check(String password, String hashed) {
        return algorithm.check(password, hashed);
    }
}
