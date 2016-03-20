package com.lenis0012.bukkit.loginsecurity.hashing.xauth;

import com.lenis0012.bukkit.loginsecurity.hashing.BasicAlgorithm;

public class xAuthAlgorithm extends BasicAlgorithm {
    private final boolean salted;

    public xAuthAlgorithm(boolean salted) {
        this.salted = salted;
    }

    @Override
    public String hash(String pw) {
        return null;
    }

    @Override
    public boolean check(String pw, String hashed) {
        String pwHash = salted ? saltedWhilrpool(pw, hashed) : whirlpool(pw);
        return pwHash.equals(hashed);
    }

    private String saltedWhilrpool(String checkPass, String realPass) {
        int saltPos = (checkPass.length() >= realPass.length() ? realPass.length() - 1 : checkPass.length());
        String salt = realPass.substring(saltPos, saltPos + 12);
        String hash = whirlpool(salt + checkPass);
        return hash.substring(0, saltPos) + salt + hash.substring(saltPos);
    }

    private String whirlpool(String toHash) {
        xAuthWhirlpool whirlpool = new xAuthWhirlpool();
        byte[] digest = new byte[xAuthWhirlpool.DIGESTBYTES];
        whirlpool.NESSIEinit();
        whirlpool.NESSIEadd(toHash);
        whirlpool.NESSIEfinalize(digest);
        return xAuthWhirlpool.display(digest);
    }
}
