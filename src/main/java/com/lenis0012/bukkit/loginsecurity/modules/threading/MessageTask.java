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
