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

package com.lenis0012.bukkit.loginsecurity.hashing;

import java.security.SecureRandom;

public abstract class BasicAlgorithm {

    /**
     * Generate a secure randoms salt.
     *
     * @param bytes Length of salt
     * @return salt
     */
    protected byte[] generateSalt(int bytes) {
        final SecureRandom random = new SecureRandom();
        byte[] salt = new byte[bytes];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Hash a password.
     *
     * @param pw Raw password
     * @return Hashed password
     */
    public abstract String hash(String pw);

    /**
     * Check if a password matches it's hash.
     *
     * @param pw Raw password.
     * @param hashed Hashed password.
     * @return True if matches, False otherwise.
     */
    public abstract boolean check(String pw, String hashed);
}
