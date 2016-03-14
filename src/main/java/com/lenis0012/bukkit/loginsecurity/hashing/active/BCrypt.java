package com.lenis0012.bukkit.loginsecurity.hashing.active;

import com.lenis0012.bukkit.loginsecurity.hashing.BasicAlgorithm;
import com.lenis0012.bukkit.loginsecurity.hashing.lib.BCryptLib;

/**
 * BCrypt algorithm.
 * A very well known algorithm, it is currently the standard.
 *
 * Each hash takes about 125ms to generate.
 */
public class BCrypt extends BasicAlgorithm {

    @Override
    public String hash(String pw) {
        String salt = BCryptLib.gensalt();
        return BCryptLib.hashpw(pw, salt);
    }

    @Override
    public boolean check(String pw, String hashed) {
        return BCryptLib.checkpw(pw, hashed);
    }
}
