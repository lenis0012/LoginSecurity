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

package com.lenis0012.bukkit.loginsecurity.hashing.deprecated;

import com.lenis0012.bukkit.loginsecurity.hashing.BasicAlgorithm;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * MD5 hashing algorithm.
 * this is no longer "acceptable", thus deprecated
 */
public class MD5 extends BasicAlgorithm {
    @Override
    public String hash(String pw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(pw.getBytes());
            return new BigInteger(1, digest.digest()).toString(16);
        } catch(Exception e) {
            return null;
        }
    }

    @Override
    public boolean check(String pw, String hashed) {
        return hash(pw).compareTo(hashed) == 0;
    }
}
