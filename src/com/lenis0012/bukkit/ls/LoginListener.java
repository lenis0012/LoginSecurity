package com.lenis0012.bukkit.ls;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class LoginListener implements Listener {
	private LoginSecurity plugin;
	public LoginListener(LoginSecurity i) { this.plugin = i; }
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		
		if(plugin.data.isSet(name)) {
			plugin.AuthList.add(name);
			player.sendMessage(ChatColor.RED+"Please login using /login <password>");
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		
		if(plugin.AuthList.contains(name))
			player.teleport(event.getFrom());
	}
}
