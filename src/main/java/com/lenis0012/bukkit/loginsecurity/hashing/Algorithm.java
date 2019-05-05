package com.lenis0012.bukkit.loginsecurity.hashing;

import com.lenis0012.bukkit.loginsecurity.hashing.active.BCrypt;
import com.lenis0012.bukkit.loginsecurity.hashing.authme.AuthmeSHA256;
import com.lenis0012.bukkit.loginsecurity.hashing.xauth.xAuthAlgorithm;

/**
 * All algorithms supported by LoginSecurity.
 *
 * IDs:
 * 0-10  = Legacy LoginSecurity
 * 10-19 = xAuth
 * 20-29 = Modern LoginSecurity
 * 30-39 = AuthMe
 */
public enum Algorithm {
    /**
     * LoginSecurity active.
     */
    BCRYPT(false, 7, new BCrypt()), // 7 Because BCrypt is the only legacy algorithm that is still supported

    /**
     * LoginSecurity deprecated.
     */
//    SCRYPT(true, 21, new SCrypt()),
//    PBKDF2(true, 23, new PBKDF2()),
//    SHA3_256(true, 24, new SHA3(256)),
//    WHIRLPOOL(true, 25, new Whirlpool()),

    /**
     * LoginSecurity Legacy & Deprecated
     */
//    MD5(true, 1, new MD5()),
//    SHA(true, 4, new SHA2("SHA")),
//    SHA1(true, 3, new SHA2("SHA-1")),
//    SHA256(true, 5, new SHA2("SHA-256")),
//    SHA512(true, 6, new SHA2("SHA-512")),
//    PHPBB3(true, 2, new PHPBB3()),

    /**
     * xAuth.
     * MD5(12), SHA1(13) and SHA256(14) are no longer supported.
     */
    xAuth_Authme_SHA256(true, 10, new AuthmeSHA256()),
    xAuth_DEFAULT(true, 11, new xAuthAlgorithm(true)),
    xAuth_WHIRLPOOL(true, 15, new xAuthAlgorithm(false)),

    /**
     * AuthMe (Reloaded).
     * SHA256 is the only supported algorithm.
     */
    AuthMe_SHA256(true, 30, new AuthmeSHA256());

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

    public static Algorithm getById(int id) {
        for(Algorithm algorithm : values()) {
            if(algorithm.id == id) {
                return algorithm;
            }
        }

        return null;
    }
}
