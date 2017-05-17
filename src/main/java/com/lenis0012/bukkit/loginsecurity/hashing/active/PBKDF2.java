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
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Base64;

import java.util.regex.Pattern;

public class PBKDF2 extends BasicAlgorithm {
    @Override
    public String hash(String pw) {
        return hash(pw, generateSalt(16));
    }

    public String hash(String pw, byte[] salt) {
        PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(new SHA256Digest());
        generator.init(pw.getBytes(), salt, 4096);
        byte[] hash = ((KeyParameter) generator.generateDerivedParameters(256)).getKey();
        return Base64.toBase64String(hash) + "$" + Base64.toBase64String(salt);
    }

    @Override
    public boolean check(String pw, String hashed) {
        String[] comp = hashed.split(Pattern.quote("$"));
        byte[] salt = Base64.decode(comp[1]);
        return hash(pw, salt).compareTo(hashed) == 0;
    }
}
