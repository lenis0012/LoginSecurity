package com.lenis0012.bukkit.loginsecurity.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class OpNotifier {
    /**
     * Send out a message to all server OPs
     */
    public static void notify(String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isOp()) {
                p.sendMessage(message);
            }
    	}
    }
}
