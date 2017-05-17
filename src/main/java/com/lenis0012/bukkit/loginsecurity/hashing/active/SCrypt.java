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

package com.lenis0012.bukkit.loginsecurity.hashing.active;

import com.lenis0012.bukkit.loginsecurity.hashing.BasicAlgorithm;
import org.bouncycastle.util.encoders.Base64;

import java.util.regex.Pattern;

/**
 * SCrypt encryption algorithm.
 * This algorithm takes 4 parameters to generate a key.
 * It consumes a lot of memory and is therefor useful against brute force attacks, or so i've heard.
 *
 * N = CPU Cost; iteration count
 * r = Block size; fine-tunes memory cost
 * p = Parallelization; fine-runes relative cpu cost
 * dkLen = derived key length
 *
 * Some examples:
 * (N=2^12, r=8, p=1) 150ms
 * (N=2^14, r=8, p=1) 200ms (best)
 * (N=2^16, r=8, p=1) 400ms
 * (N=2^18, r=8, p=1) 1400ms
 * (N=2^20, r=8, p=1) 5000ms
 */
public class SCrypt extends BasicAlgorithm {
    private static final int COST = 16384; // N (2**14)
    private static final int BLOCK_SIZE = 8; // r
    private static final int PARALLELIZATION = 1; // p
    private static final int LENGTH = 256; // dkLen

    @Override
    public String hash(String pw) {
        return hash(pw, generateSalt(16));
    }

    public String hash(String pw, byte[] salt) {
        byte[] hash = org.bouncycastle.crypto.generators.SCrypt.generate(
                pw.getBytes(), salt,
                COST, BLOCK_SIZE, PARALLELIZATION, LENGTH
        );
        return Base64.toBase64String(hash) + "$" + Base64.toBase64String(salt);
    }

    @Override
    public boolean check(String pw, String hashed) {
        String[] comp = hashed.split(Pattern.quote("$"));
        byte[] salt = Base64.decode(comp[1]);
        return hash(pw, salt).compareTo(hashed) == 0;
    }
}
