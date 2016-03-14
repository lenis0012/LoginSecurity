package com.lenis0012.bukkit.loginsecurity.hashing.active;

import com.lenis0012.bukkit.loginsecurity.hashing.BasicAlgorithm;
import de.mkammerer.argon2.Argon2Factory;

public class Argon2 extends BasicAlgorithm {
    private final de.mkammerer.argon2.Argon2 argon;

    public Argon2() {
        this.argon = Argon2Factory.create();
    }

    @Override
    public String hash(String pw) {
        return argon.hash(2, 65536, 1, pw);
    }

    @Override
    public boolean check(String pw, String hashed) {
        return argon.verify(hashed, pw);
    }
}
