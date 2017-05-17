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
import com.lenis0012.bukkit.loginsecurity.hashing.lib.BCryptLib;

/**
 * BCrypt algorithm.
 * A very well known algorithm, it is currently the standard.
 *
 * Each hash takes about 125ms to generate.
 */
public class BCrypt extends BasicAlgorithm {

    @Override
    public String hash(String pw) {
        String salt = BCryptLib.gensalt();
        return BCryptLib.hashpw(pw, salt);
    }

    @Override
    public boolean check(String pw, String hashed) {
        return BCryptLib.checkpw(pw, hashed);
    }
}
