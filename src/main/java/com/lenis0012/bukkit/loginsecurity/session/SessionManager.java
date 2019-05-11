package com.lenis0012.bukkit.loginsecurity.session;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.bukkit.loginsecurity.util.ProfileUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

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

    public PlayerSession preloadSession(final String playerName, final UUID playerUUID) {
        final UUID profileId = ProfileUtil.getUUID(playerName, playerUUID);
        return preloadCache.getUnchecked(profileId);
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
        try {
            PlayerProfile profile = LoginSecurity.getDatastore().getProfileRepository().findByLastNameBlocking(playerName);
            if(profile == null) {
                OfflinePlayer offline = Bukkit.getOfflinePlayer(playerName);
                if(offline == null || offline.getUniqueId() == null) {
                    return null;
                }

                return getOfflineSession(ProfileUtil.getUUID(playerName, offline.getUniqueId()));
            }
            return new PlayerSession(profile, AuthMode.UNAUTHENTICATED);
        } catch (SQLException e) {
            LoginSecurity.getInstance().getLogger().log(Level.SEVERE, "Failed to load profile", e);
            return null;
        }
    }

    public void onPlayerLogout(final Player player) {
        final UUID userId = ProfileUtil.getUUID(player);
        activeSessions.remove(userId);
    }

    private final PlayerSession newSession(final UUID playerId) {
        try {
            PlayerProfile profile = LoginSecurity.getDatastore().getProfileRepository().findByUniqueUserIdBlocking(playerId);
            AuthMode authMode = AuthMode.UNAUTHENTICATED;
            if(profile == null) {
                // New user...
                profile = createBlankProfile(playerId);
                authMode = LoginSecurity.getConfiguration().isPasswordRequired() ? AuthMode.UNREGISTERED : AuthMode.AUTHENTICATED;
            }

            return new PlayerSession(profile, authMode);
        } catch (SQLException e) {
            LoginSecurity.getInstance().getLogger().log(Level.SEVERE, "Failed to load profile", e);
            return null;
        }
    }

    protected final PlayerProfile createBlankProfile(final UUID playerId) {
        PlayerProfile profile = new PlayerProfile();
        profile.setUniqueUserId(playerId.toString());
        profile.setUniqueIdMode(ProfileUtil.getUserIdMode());
        return profile;
    }
}
