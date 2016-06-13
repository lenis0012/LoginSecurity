package com.lenis0012.bukkit.loginsecurity.util;

import com.avaje.ebean.annotation.EnumValue;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;

import java.util.UUID;

public enum UserIdMode {
    @EnumValue("U")
    UNKNOWN,
    @EnumValue("M")
    MOJANG,
    @EnumValue("O")
    OFFLINE;

    public String getUserId(final PlayerProfile profile) {
        if(profile.getUniqueIdMode() == this) {
            return profile.getUniqueUserId();
        }

        switch(this) {
            case OFFLINE:
                return UUID.nameUUIDFromBytes(("OfflinePlayer:" + profile.getLastName().toLowerCase()).getBytes()).toString();
            case MOJANG:
                return profile.getUniqueUserId();
            default:
                throw new IllegalStateException("Invalid uuid mode: " + toString());
        }
    }
}
