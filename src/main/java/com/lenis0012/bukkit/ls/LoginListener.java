package com.lenis0012.bukkit.ls;

import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

import com.lenis0012.bukkit.ls.data.MySQL;
import com.lenis0012.bukkit.ls.data.SQLite;
import com.lenis0012.bukkit.ls.util.StringUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;

@SuppressWarnings("deprecation")
public class LoginListener implements Listener {

	private LoginSecurity plugin;

	public LoginListener(LoginSecurity i) {
		this.plugin = i;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		
		if(MySQL.IS_CONVERTING || SQLite.IS_CONVERTING) {
			player.kickPlayer("The server is currently converting all login data, please join back later.");
			return;
		} if (!player.getName().equals(StringUtil.cleanString(player.getName()))) {
			player.kickPlayer("Invalid characters in username!");
			return;
		}

		plugin.playerJoinPrompt(player);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		String name = event.getName().toLowerCase();
		//Check if the player is already online
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			String pname = player.getName().toLowerCase();
			if (plugin.authList.containsKey(pname)) {
				continue;
			}

			if (pname.equalsIgnoreCase(name)) {
				event.setResult(Result.KICK_OTHER);
				event.setKickMessage("A player with this name is already online!");
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		String ip = player.getAddress().getAddress().toString();

		if (!plugin.data.isRegistered(name)) {
			plugin.data.updateIp(name, ip);
		}

		if (plugin.sesUse && !plugin.authList.containsKey(name) && plugin.data.isRegistered(name)) {
			plugin.thread.getSession().put(name, plugin.sesDelay);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		Location from = event.getFrom();
		Location to = event.getTo().clone();

		if (plugin.authList.containsKey(name)) {
			to.setX(from.getX());
			to.setZ(from.getZ());
			event.setTo(to);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();

		if (plugin.authList.containsKey(name)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();

		if (plugin.authList.containsKey(name)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();

		if (plugin.authList.containsKey(name)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();

		if (plugin.authList.containsKey(name)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent chat) {
		Player player = chat.getPlayer();
		String pname = player.getName().toLowerCase();
		if (plugin.authList.containsKey(pname)) {
			chat.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void OnHealthRegain(EntityRegainHealthEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) {
			return;
		}
		Player player = (Player) entity;
		String pname = player.getName().toLowerCase();

		if (plugin.authList.containsKey(pname)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void OnFoodLevelChange(FoodLevelChangeEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) {
			return;
		}
		Player player = (Player) entity;
		String pname = player.getName().toLowerCase();

		if (plugin.authList.containsKey(pname)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		Entity entity = event.getWhoClicked();
		if (!(entity instanceof Player)) {
			return;
		}
		Player player = (Player) entity;
		String pname = player.getName().toLowerCase();

		if (plugin.authList.containsKey(pname)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		Entity entity = event.getEntity();

		if (entity instanceof Player) {
			Player player = (Player) entity;
			String name = player.getName().toLowerCase();
			if (plugin.authList.containsKey(name)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		for (LivingEntity entity : event.getAffectedEntities()) {
			if (entity instanceof Player) {
				Player player = (Player) entity;
				String name = player.getName().toLowerCase();
				if (plugin.authList.containsKey(name)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity defender = event.getEntity();
		Entity damager = event.getDamager();

		if (defender instanceof Player) {
			Player p1 = (Player) defender;
			String n1 = p1.getName().toLowerCase();

			if (plugin.authList.containsKey(n1)) {
				event.setCancelled(true);
				return;
			}

			if (damager instanceof Player) {
				Player p2 = (Player) damager;
				String n2 = p2.getName().toLowerCase();

				if (plugin.authList.containsKey(n2)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityTarget(EntityTargetEvent event) {
		Entity entity = event.getTarget();

		if (entity instanceof Player) {
			Player player = (Player) entity;
			String name = player.getName().toLowerCase();

			if (plugin.authList.containsKey(name)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		String name = event.getPlayer().getName().toLowerCase();

		if (plugin.authList.containsKey(name)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		if (plugin.authList.containsKey(name)) {
			if (!event.getMessage().startsWith("/login") && !event.getMessage().startsWith("/register")) {
				//faction fix start
				if (event.getMessage().startsWith("/f")) {
					event.setMessage("/" + RandomStringUtils.randomAscii(name.length())); //this command does not exist :P
				}		    	//faction fix end
				event.setCancelled(true);
			}
		}
	}
}
