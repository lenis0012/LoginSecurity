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

package com.lenis0012.bukkit.loginsecurity.hashing.authme;

import com.lenis0012.bukkit.loginsecurity.hashing.BasicAlgorithm;
import org.bouncycastle.jcajce.provider.digest.SHA256;

import java.math.BigInteger;
import java.security.MessageDigest;

public class AuthmeSHA extends BasicAlgorithm {
    private final MessageDigest digest;
    private final String prefix;

    public AuthmeSHA(String algorithm) {
        switch(algorithm.toLowerCase()) {
            case "sha256":
                this.digest = new SHA256.Digest();
                this.prefix = "SHA";
                break;
            default:
                throw new IllegalArgumentException("Unknown SHA digest: " + algorithm);
        }
    }

    @Override
    public String hash(String pw) {
        return null;
    }

    @Override
    public boolean check(String pw, String hashed) {
        String[] line = hashed.split("\\$");
        return line.length == 4 && hashed.equals(getHash(pw, line[2]));
    }

    private String getHash(String pw, String salt) {
        return "$" + prefix + "$" + salt + "$" + compute(compute(pw) + salt);
    }

    private String compute(String message) {
        digest.reset();
        digest.update(message.getBytes());
        byte[] hash = digest.digest();
        return String.format("%0" + (hash.length << 1) + "x", new BigInteger(1, hash));
    }
}
