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

package com.lenis0012.bukkit.loginsecurity.util;

import com.avaje.ebean.annotation.EnumValue;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;

import java.util.UUID;

public enum UserIdMode {
    @EnumValue("U")
    UNKNOWN("U"),
    @EnumValue("M")
    MOJANG("M"),
    @EnumValue("O")
    OFFLINE("O");

    private final String id;

    UserIdMode(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getUserId(final PlayerProfile profile) {
        if(profile.getUniqueIdMode() == this) {
            return profile.getUniqueUserId();
        }

        switch(this) {
            case OFFLINE:
                return profile.getLastName() == null ? profile.getUniqueUserId() : UUID.nameUUIDFromBytes(("OfflinePlayer:" + profile.getLastName().toLowerCase()).getBytes()).toString();
            case MOJANG:
                return profile.getUniqueUserId();
            default:
                throw new IllegalStateException("Invalid uuid mode: " + toString());
        }
    }

    public static UserIdMode fromId(String id) {
        for(UserIdMode mode : values()) {
            if(mode.id.equalsIgnoreCase(id)) {
                return mode;
            }
        }
        throw new IllegalStateException("Invalid uuid mode: " + id);
    }
}
