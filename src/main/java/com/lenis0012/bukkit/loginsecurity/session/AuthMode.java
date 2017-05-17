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

package com.lenis0012.bukkit.loginsecurity.session;

import com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys;

import static com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys.*;
import static com.lenis0012.bukkit.loginsecurity.LoginSecurity.translate;

public enum AuthMode {
    /**
     * When the player is logged in.
     */
    AUTHENTICATED(null),
    /**
     * When the player registered but not logged in.
     */
    UNAUTHENTICATED(MESSAGE_LOGIN),
    /**
     * When the player is not registered and not logged in.
     */
    UNREGISTERED(MESSAGE_REGISTER);

    private final String authMessage;

    AuthMode(LanguageKeys authMessage) {
        this.authMessage = authMessage != null ? translate(authMessage).toString() : null;
    }

    public boolean hasAuthMessage() {
        return authMessage != null;
    }

    public String getAuthMessage() {
        return authMessage;
    }
}
