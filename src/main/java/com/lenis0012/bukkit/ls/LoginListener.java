package com.lenis0012.bukkit.ls;

import java.util.Calendar;

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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.lenis0012.bukkit.ls.data.ValueType;
import com.lenis0012.bukkit.ls.util.StringUtil;

public class LoginListener implements Listener {
	private LoginSecurity plugin;
	public LoginListener(LoginSecurity i) { this.plugin = i; }
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		plugin.showVersion(player);
		
		if(!name.equals(StringUtil.cleanString(name))) {
			player.kickPlayer("Invalid username!");
			return;
		}
		
		if(plugin.sesUse && plugin.thread.session.containsKey(name) && this.checkLastIp(player)) {
			player.sendMessage("Extended session from last login");
			return;
		} else if(plugin.data.isSet(name)) {
			plugin.AuthList.put(name, false);
			player.sendMessage(ChatColor.RED+"Please login using /login <password>");
			if(plugin.blindness)
				player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1728000, 15));
		} else if(plugin.required) {
			plugin.AuthList.put(name, true);
			player.sendMessage(ChatColor.RED+"Please register using /register <password>");
			if(plugin.blindness)
				player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1728000, 15));
		}
		
		if(plugin.timeUse) {
			plugin.thread.timeout.put(name, plugin.timeDelay);
		}
	}
	
	private boolean checkLastIp(Player player) {
		String name = player.getName();
		if(plugin.lastlogin.isSet(name)) {
			String lastIp = (String)plugin.lastlogin.getValue(name, "ip");
			String currentIp = player.getAddress().getAddress().toString();
			return lastIp.equalsIgnoreCase(currentIp);
		}
		return false;
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		String ip = player.getAddress().getAddress().toString();
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int day = c.get(Calendar.DAY_OF_YEAR);
		String lastlogin = String.valueOf(year + day);
		
		if(!plugin.lastlogin.isSet(name))
			plugin.lastlogin.setValue(name, ValueType.INSERT, ip, lastlogin);
		else
			plugin.lastlogin.setValue(name, ValueType.UPDATE_IP, ip, lastlogin);
		
		if(plugin.sesUse && !plugin.AuthList.containsKey(name) && plugin.data.isSet(name))
			plugin.thread.session.put(name, plugin.sesDelay);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		
		if(plugin.AuthList.containsKey(name))
			player.teleport(event.getFrom());
		else if(player.hasPotionEffect(PotionEffectType.BLINDNESS) && plugin.blindness)
			player.removePotionEffect(PotionEffectType.BLINDNESS);
				
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		
		if(plugin.AuthList.containsKey(name))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		
		if(plugin.AuthList.containsKey(name))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		
		if(plugin.AuthList.containsKey(name))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		
		if(plugin.AuthList.containsKey(name))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent chat){
		Player player = chat.getPlayer();
		String pname = player.getName();
		if(plugin.AuthList.containsKey(pname)){
			chat.setCancelled(true);
		}
	}
	
	@EventHandler
	public void OnHealthRegain(EntityRegainHealthEvent event) {
		Entity entity = event.getEntity();
		if(!(entity instanceof Player))
			return;
		Player player = (Player)entity;
		String pname = player.getName();
			
		if(plugin.AuthList.containsKey(pname)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void OnFoodLevelChange(FoodLevelChangeEvent event) {
		Entity entity = event.getEntity();
		if(!(entity instanceof Player))
			return;
		Player player = (Player)entity;
		String pname = player.getName();
			
		if(plugin.AuthList.containsKey(pname)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Entity entity = event.getWhoClicked();
		if(!(entity instanceof Player))
			return;
		Player player = (Player)entity;
		String pname = player.getName();
			
		if(plugin.AuthList.containsKey(pname)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity defender = event.getEntity();
		Entity damager = event.getDamager();
		
		if(defender instanceof Player) {
			Player p1 = (Player) defender;
			String n1 = p1.getName();
			
			if(plugin.AuthList.containsKey(n1)) {
				event.setCancelled(true);
				return;
			}
			
			if(damager instanceof Player) {
				Player p2 = (Player) damager;
				String n2 = p2.getName();
				
				if(plugin.AuthList.containsKey(n2))
					event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		if(plugin.AuthList.containsKey(name)){
		    if(!event.getMessage().startsWith("/login") && !event.getMessage().startsWith("/register"))
		  	{
		    	//faction fix start
		    	if(event.getMessage().startsWith("/f"))
		    		event.setMessage("/blablablablabla"); //this command does not exist :P
		    	//faction fix end
		    	event.setCancelled(true);
		  	}
		}
	}
}
