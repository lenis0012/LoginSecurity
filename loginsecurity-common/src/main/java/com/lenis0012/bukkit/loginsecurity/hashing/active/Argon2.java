package com.lenis0012.bukkit.loginsecurity.hashing.active;

import com.lenis0012.bukkit.loginsecurity.hashing.BasicAlgorithm;
import de.mkammerer.argon2.Argon2Factory;

public class Argon2 extends BasicAlgorithm {
    private de.mkammerer.argon2.Argon2 argon;

    @Override
    public String hash(String pw) {
        checkArgon();
        return argon.hash(2, 65536, 1, pw);
    }

    @Override
    public boolean check(String pw, String hashed) {
        checkArgon();
        return argon.verify(hashed, pw);
    }

    private void checkArgon() {
        if(argon == null) {
            checkArgonFound();
            this.argon = Argon2Factory.create();
        }
    }

    private void checkArgonFound() {
        try {
            Class.forName("de.mkammerer.argon2.Argon2");
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("Tried to use Argon2, but the library isn't installed!");
        }
    }
}
