package com.lenis0012.bukkit.loginsecurity.modules.threading;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.LoginSecurityConfig;
import com.lenis0012.bukkit.loginsecurity.session.AuthService;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import com.lenis0012.bukkit.loginsecurity.session.action.LoginAction;
import com.lenis0012.bukkit.loginsecurity.util.MetaData;
import com.lenis0012.bukkit.loginsecurity.util.ProfileUtil;
import com.lenis0012.pluginutils.Module;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.lenis0012.bukkit.loginsecurity.LoginSecurity.translate;
import static com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys.SESSION_CONTINUE;

public class ThreadingModule extends Module<LoginSecurity> implements Listener {
    private Cache<UUID, Long> sessionCache;
    private TimeoutTask timeout;
    private MessageTask message;

    public ThreadingModule(LoginSecurity plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        reload();

        // threads
        (this.timeout = new TimeoutTask(plugin)).runTaskTimer(plugin, 20L, 20L);
        (this.message = new MessageTask(plugin)).runTaskTimer(plugin, 20L, 20L);
        register(this);

        Bukkit.getOnlinePlayers()
                .stream()
                .filter(Player::isOnline) // NPC hotfix
                .forEach(player -> MetaData.set(player, "ls_login_time", System.currentTimeMillis()));
    }

    @Override
    public void disable() {
    }

    @Override
    public void reload() {
        final LoginSecurityConfig config = plugin.config();
        final int sessionTimeout = config.getSessionTimeout();

        this.sessionCache = CacheBuilder.newBuilder().expireAfterWrite(Math.max(1, sessionTimeout), TimeUnit.SECONDS).build();
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        MetaData.unset(player, "ls_last_message");
        MetaData.unset(player, "ls_login_time");
        if(session.isLoggedIn()) {
            sessionCache.put(ProfileUtil.getUUID(player), System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final UUID profileId = ProfileUtil.getUUID(player);
        final Long sessionTime = sessionCache.getIfPresent(profileId);
        MetaData.set(player, "ls_login_time", System.currentTimeMillis());
        if(sessionTime == null) {
            return;
        }

        final long lastLogout = sessionTime;

        // Ip check
        final String ipAddress = player.getAddress().getAddress().toString();
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(!ipAddress.equals(session.getProfile().getIpAddress())) {
            // Invalid IP
            return;
        }

        // Allow log in once
        final int seconds = (int) ((System.currentTimeMillis() - lastLogout) / 1000L);
        session.performActionAsync(new LoginAction(AuthService.SESSION, plugin), response -> {
            if(response.isSuccess()) {
                player.sendMessage(translate(SESSION_CONTINUE).param("sec", seconds).toString());
            }
        });
    }
}
