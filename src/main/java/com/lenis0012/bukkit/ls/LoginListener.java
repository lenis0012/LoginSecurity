package com.lenis0012.bukkit.ls;

import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.lenis0012.bukkit.ls.util.StringUtil;
import org.bukkit.GameMode;

@SuppressWarnings("deprecation")
public class LoginListener implements Listener {
	private LoginSecurity plugin;
	public LoginListener(LoginSecurity i) { this.plugin = i; }
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		final String name = player.getName().toLowerCase();
		
		if(!player.getName().equals(StringUtil.cleanString(player.getName()))) {
			player.kickPlayer("Invalid username!");
			return;
		}
		
		if(plugin.sesUse && plugin.thread.getSession().containsKey(name) && plugin.checkLastIp(player)) {
			player.sendMessage(ChatColor.GREEN+"Extended session from last login");
			return;
		} else if(plugin.data.isRegistered(name)) {
			plugin.AuthList.put(name, false);
			if(!plugin.messager)
				player.sendMessage(ChatColor.RED+"Please login using /login <password>");
		} else if(plugin.required) {
			plugin.AuthList.put(name, true);
			if(!plugin.messager)
				player.sendMessage(ChatColor.RED+"Please register using /register <password>");
		} else
			return;
		
		plugin.debilitatePlayer(player);
		
		//Send data to messager API
		if(plugin.messager) {
		plugin.messaging.add(name);
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				@Override
				public void run() {
					if(plugin.messaging.contains(name)) {
						boolean register = plugin.AuthList.get(name);
						plugin.messaging.remove(name);
						if(register)
							player.sendMessage(ChatColor.RED+"Please register using /register <password>");
						else
							player.sendMessage(ChatColor.RED+"Please login using /login <password>");
					} else
						plugin.sendCustomPayload(player, "Q_LOGIN");
				}
			}, 20);
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onPlayerPreLogin(PlayerPreLoginEvent event) {
		String name = event.getName();
		//Check if the player is already online
		for(Player player : Bukkit.getServer().getOnlinePlayers()) {
			String pname = player.getName().toLowerCase();
			if(plugin.AuthList.containsKey(pname))
				continue;
			
			if(pname.equalsIgnoreCase(name)) {
				event.setResult(Result.KICK_OTHER);
				event.setKickMessage("A player with this name is already online!");
			}
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		String ip = player.getAddress().getAddress().toString();
		
		if(!plugin.data.isRegistered(name))
			plugin.data.updateIp(name, ip);
		
		if(plugin.sesUse && !plugin.AuthList.containsKey(name) && plugin.data.isRegistered(name))
			plugin.thread.getSession().put(name, plugin.sesDelay);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		
		if(plugin.AuthList.containsKey(name))
			player.teleport(event.getFrom());
				
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		
		if(plugin.AuthList.containsKey(name))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		
		if(plugin.AuthList.containsKey(name))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		
		if(plugin.AuthList.containsKey(name))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		
		if(plugin.AuthList.containsKey(name))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerChat(PlayerChatEvent chat){
		Player player = chat.getPlayer();
		String pname = player.getName().toLowerCase();
		if(plugin.AuthList.containsKey(pname)){
			chat.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Entity entity = event.getWhoClicked();
		if(!(entity instanceof Player))
			return;
		Player player = (Player)entity;
		String pname = player.getName().toLowerCase();
			
		if(plugin.AuthList.containsKey(pname)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		if(plugin.AuthList.containsKey(name)){
		    if(!event.getMessage().startsWith("/login") && !event.getMessage().startsWith("/register")) {
		    	//faction fix start
		    	if(event.getMessage().startsWith("/f"))
		    		event.setMessage("/" + RandomStringUtils.randomAscii(name.length())); //this command does not exist :P
		    	//faction fix end
		    	event.setCancelled(true);
		  	}
		}
	}
}
