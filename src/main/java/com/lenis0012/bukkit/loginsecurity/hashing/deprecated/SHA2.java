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
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.security.MessageDigest;

/**
 * SHA2 hashing algorithm.
 * this is no longer "acceptable", thus deprecated
 */
public class SHA2 extends BasicAlgorithm {
    private final String algorithm;

    public SHA2(String algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public String hash(String pw) {
        return hash(pw, "UTF-8");
    }

    public String hash(String pw, String encoding) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            digest.update(pw.getBytes("UTF-8"));
            byte[] rawDigest = digest.digest();
            return Base64Coder.encodeLines(rawDigest);
        } catch(Exception e) {
            return null;
        }
    }

    @Override
    public boolean check(String pw, String hashed) {
        return hash(pw, "UTF-8").compareTo(hashed) == 0 || hash(pw, "UTF-16").compareTo(hashed) == 0;
    }
}
