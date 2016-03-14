package com.lenis0012.bukkit.loginsecurity.hashing.deprecated;

import com.lenis0012.bukkit.loginsecurity.hashing.BasicAlgorithm;
import com.lenis0012.bukkit.loginsecurity.hashing.lib.PHPBB3Lib;

/**
 * PHPBB3 hashing algorithm.
 * this is no longer "acceptable", thus deprecated
 */
public class PHPBB3 extends BasicAlgorithm {
    private final PHPBB3Lib library;

    public PHPBB3() {
        this.library = new PHPBB3Lib();
    }

    @Override
    public String hash(String pw) {
        return library.phpbb_hash(pw);
    }

    @Override
    public boolean check(String pw, String hashed) {
        return library.phpbb_check_hash(pw, hashed);
    }
}
