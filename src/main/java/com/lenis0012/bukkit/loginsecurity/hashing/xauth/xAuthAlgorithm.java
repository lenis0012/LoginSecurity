/*
 * This file is a part of LoginSecurity.
 *
 * Copyright (c) 2017 Lennart ten Wolde
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
