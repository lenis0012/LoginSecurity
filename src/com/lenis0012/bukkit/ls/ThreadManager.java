package com.lenis0012.bukkit.ls;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ThreadManager {
	private LoginSecurity plugin;
	private int msg = 0;
	
	public ThreadManager(LoginSecurity plugin) {
		this.plugin = plugin;
	}
	
	public void startMsgTask() {
		msg = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				for(Player player : Bukkit.getServer().getOnlinePlayers()) {
					String name = player.getName();
					if(plugin.AuthList.containsKey(name)) {
						boolean register = plugin.AuthList.get(name);
						if(register)
							player.sendMessage(ChatColor.RED+"Please register using /register <password>");
						else
							player.sendMessage(ChatColor.RED+"Please login using /login <password>");
					}
				}
			}
		}, 200L, 200L);
	}
	
	public void stopMsgTask() {
		if(msg > 0)
			plugin.getServer().getScheduler().cancelTask(msg);
	}
}
