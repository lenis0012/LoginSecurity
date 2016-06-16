package com.lenis0012.bukkit.loginsecurity.session;

import com.avaje.ebean.EbeanServer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
    private final LoadingCache<UUID, PlayerSession> preloadCache;

    public SessionManager() {
        this.preloadCache = CacheBuilder.newBuilder().expireAfterWrite(30L, TimeUnit.SECONDS).build(new CacheLoader<UUID, PlayerSession>() {
            @Override
            public PlayerSession load(UUID uuid) throws Exception {
                return newSession(uuid);
            }
        });
    }

    public void preloadSession(final String playerName, final UUID playerUUID) {
        final UUID profileId = ProfileUtil.getUUID(playerName, playerUUID);
        preloadCache.getUnchecked(profileId);
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
            session = preloadCache.getUnchecked(userId);
            activeSessions.put(userId, session);
            preloadCache.invalidate(userId);
        }

        return session;
    }

    public final PlayerSession getOfflineSession(final UUID profileId) {
        return newSession(profileId);
    }

    public final PlayerSession getOfflineSession(final String playerName) {
        // TODO: Build local cache from world files. (maybe add cache mode in config)
        final UUID fallback = Bukkit.getOfflinePlayer(playerName).getUniqueId();
        return getOfflineSession(ProfileUtil.getUUID(playerName, fallback));
    }

    public void onPlayerLogout(final Player player) {
        final UUID userId = ProfileUtil.getUUID(player);
        CollisionE
        activeSessions.remove(userId);
    }

    private final PlayerSession newSession(final UUID playerId) {
        final EbeanServer database = LoginSecurity.getInstance().getDatabase();
        PlayerProfile profile = database.find(PlayerProfile.class).where().ieq("unique_user_id", playerId.toString()).findUnique();
        AuthMode authMode = AuthMode.UNAUTHENTICATED;
        if(profile == null) {
            // New user...
            profile = new PlayerProfile();
            profile.setUniqueUserId(playerId.toString());
            profile.setUniqueIdMode(ProfileUtil.getUserIdMode());
            authMode = LoginSecurity.getConfiguration().isPasswordRequired() ? AuthMode.UNREGISTERED : AuthMode.AUTHENTICATED;
        }

        return new PlayerSession(profile, authMode);
    }
}
