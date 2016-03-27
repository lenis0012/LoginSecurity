package com.lenis0012.bukkit.loginsecurity.session;

import com.avaje.ebean.EbeanServer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.bukkit.loginsecurity.util.ProfileUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SessionManager {
    private final Map<UUID, PlayerSession> activeSessions = Maps.newConcurrentMap();
    private final Cache<UUID, Long> sessionCache;

    public SessionManager(LoginSecurity plugin) {
        int sessionTimeout = plugin.config().getSessionTimeout();
        if(sessionTimeout > 0) {
            this.sessionCache = CacheBuilder.newBuilder()
                    .expireAfterWrite(sessionTimeout, TimeUnit.SECONDS)
                    .build();
        } else {
            this.sessionCache = null;
        }
    }

    public final PlayerSession getPlayerSession(final Player player) {
        if(!player.isOnline()) {
            throw new IllegalStateException("Can't retrieve player session from offline player!");
        }

        final UUID userId = ProfileUtil.getUUID(player);
        final PlayerSession session;
        if(activeSessions.containsKey(userId)) {
            session = activeSessions.get(userId);
        } else {
            session = newSession(userId);
        }

        return session;
    }

    public final PlayerSession getOfflineSession(final UUID profileId) {
        // TODO: Return offline session.
        return null;
    }

    public final PlayerSession getOfflineSession(final String playerName) {
        // TODO: Build local cache from world files. (maybe add cache mode in config)
        final UUID fallback = Bukkit.getOfflinePlayer(playerName).getUniqueId();
        return getOfflineSession(ProfileUtil.getUUID(playerName, fallback));
    }

    public void onPlayerLogout(final Player player) {
        final UUID userId = ProfileUtil.getUUID(player);
        activeSessions.remove(player.getUniqueId());
        if(sessionCache != null) {
            sessionCache.put(userId, System.currentTimeMillis());
        }
    }

    private final PlayerSession newSession(final UUID playerId) {
        final EbeanServer database = LoginSecurity.getInstance().getDatabase();
        PlayerProfile profile = database.find(PlayerProfile.class).where().ieq("unique_user_id", playerId.toString()).findUnique();
        AuthMode authMode = AuthMode.UNAUTHENTICATED;
        if(profile == null) {
            // New user...
            profile = new PlayerProfile();
            profile.setUniqueUserId(playerId.toString());
            authMode = AuthMode.UNREGISTERED;
        }

        return new PlayerSession(profile, authMode);
    }
}
