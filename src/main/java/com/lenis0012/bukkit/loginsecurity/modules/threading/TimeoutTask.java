package com.lenis0012.bukkit.loginsecurity.modules.threading;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TimeoutTask extends BukkitRunnable {
    private final LoginSecurity plugin;
    private long loginTimeout;

    public TimeoutTask(LoginSecurity plugin) {
        this.plugin = plugin;
        reload();
    }

    @Override
    public void run() {
        for(final Player player : Bukkit.getOnlinePlayers()) {
            final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
            if(session.isLoggedIn()) {
                // Player is logged in, don't time out.
                continue;
            }

            long lastLogin = session.getProfile().getLastLogin().getTime();
            if(lastLogin + loginTimeout < System.currentTimeMillis()) {
                player.kickPlayer("Login timed out!");
            }
        }
    }

    public void reload() {
        this.loginTimeout = plugin.config().getLoginTimeout() * 1000L;
    }
}
