package com.lenis0012.bukkit.ls;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.lenis0012.bukkit.ls.util.Updater.UpdateResult;
import com.lenis0012.bukkit.ls.util.Updater.UpdateType;

public class LoginListener implements Listener {
	private LoginSecurity plugin;
	public LoginListener(LoginSecurity i) { this.plugin = i; }
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		
		if(plugin.data.isSet(name)) {
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
		
		final Player p = player;
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				Player pla = p;
				if(pla.hasPermission("ls.admin")) {
					if(plugin.updater != null) {
						plugin.updater.update(UpdateType.NO_DOWNLOAD, false);
						if(plugin.updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
							pla.sendMessage(ChatColor.GREEN+"LoginSecurity has a new update, check BukkitDev");
						} else
							pla.sendMessage(ChatColor.GREEN+"LoginSecurity did not find updates");
					}
				}
			}
		}, 25);
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
