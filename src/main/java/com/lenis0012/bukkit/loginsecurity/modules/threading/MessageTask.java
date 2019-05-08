package com.lenis0012.bukkit.loginsecurity.modules.threading;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.session.AuthMode;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import com.lenis0012.bukkit.loginsecurity.util.MetaData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MessageTask extends BukkitRunnable {
    private final LoginSecurity plugin;
    private long messageDelay;

    public MessageTask(LoginSecurity plugin) {
        this.plugin = plugin;
        reload();
    }

    @Override
    public void run() {
        for(final Player player : Bukkit.getOnlinePlayers()) {
            if(!player.isOnline()) continue; // NPC hotfix
            final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
            final AuthMode authMode = session.getAuthMode();
            if(!authMode.hasAuthMessage()) {
                // Auth mode does not have login message
                continue;
            }

            final long lastMessage = MetaData.get(player, "ls_last_message", 0L);
            if(lastMessage + messageDelay > System.currentTimeMillis()) {
                continue;
            }

            player.sendMessage(ChatColor.RED + authMode.getAuthMessage());
            MetaData.set(player, "ls_last_message", System.currentTimeMillis());
        }
    }

    public void reload() {
        this.messageDelay = plugin.config().getLoginMessageDelay() * 1000L;
    }
}
