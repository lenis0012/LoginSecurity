package com.lenis0012.bukkit.loginsecurity.modules.threading;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import com.lenis0012.bukkit.loginsecurity.util.MetaData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static com.lenis0012.bukkit.loginsecurity.LoginSecurity.translate;
import static com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys.KICK_TIME_OUT;

public class TimeoutTask extends BukkitRunnable {
    private final LoginSecurity plugin;
    private long loginTimeout;

    public TimeoutTask(LoginSecurity plugin) {
        this.plugin = plugin;
        reload();
    }

    @Override
    public void run() {
        if(loginTimeout < 0) return; // Disabled

        for(final Player player : Bukkit.getOnlinePlayers()) {
            if(!player.isOnline()) continue; // NPC hotfix
            final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
            if(session.isAuthorized()) {
                // Player is logged in, don't time out.
                continue;
            }

            Long loginTime = MetaData.get(player, "ls_login_time", Long.class);
            if(loginTime != null && loginTime + loginTimeout < System.currentTimeMillis()) {
                player.kickPlayer(translate(KICK_TIME_OUT).toString());
            }
        }
    }

    public void reload() {
        this.loginTimeout = plugin.config().getLoginTimeout() * 1000L;
    }
}
