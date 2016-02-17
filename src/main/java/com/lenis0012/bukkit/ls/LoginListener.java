package com.lenis0012.bukkit.ls;

import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.lenis0012.bukkit.ls.data.UUIDConverter;
import com.lenis0012.bukkit.ls.util.StringUtil;
import com.lenis0012.bukkit.ls.util.Updater;
import com.lenis0012.bukkit.ls.util.Updater.UpdateResult;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class LoginListener implements Listener {

	private LoginSecurity plugin;

	public LoginListener(LoginSecurity i) {
		this.plugin = i;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();

		plugin.playerJoinPrompt(player);
		if(player.hasPermission("ls.admin")) {
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

				@Override
				public void run() {
					Updater updater = plugin.getUpdater();
					if (updater != null) {
						if(updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
							player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
								"&aA new &7%s &7build for LoginSecurtiy was found, you can get &7%s &afor &7%s &aon BukkitDev!",
								updater.getLatestType().toString().toLowerCase(),
								updater.getLatestName(),
								updater.getLatestGameVersion())));
						}
					}
				}
			}, 20L);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		//Check conversion in progress
		if(UUIDConverter.IS_CONVERTING) {
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Currently converting all login data, please check later.");
			return;
		}

		String pname = event.getName();
		//Check for valid user name
		if (!pname.equals(StringUtil.cleanString(pname))) {
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Invalid characters in username!");
			return;
		}

		String uuid = event.getUniqueId().toString();
		//Check if the player is already online
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			String puuid = player.getUniqueId().toString();
			if (plugin.authList.containsKey(puuid)) {
				continue;
			}

			if (puuid.equalsIgnoreCase(uuid)) {
				event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "A player with this name is already online!");
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		if(UUIDConverter.IS_CONVERTING) {
			return;
		}

		Player player = event.getPlayer();
		String uuid = player.getUniqueId().toString();
		String ip = player.getAddress().getAddress().toString();

		if(plugin.authList.containsKey(uuid) && plugin.spawntp && plugin.loginLocations.containsKey(uuid)) {
			player.teleport(plugin.loginLocations.remove(uuid));
		} if (plugin.data.isRegistered(uuid)) {
			plugin.data.updateIp(uuid, ip);
			if (plugin.sesUse && !plugin.authList.containsKey(uuid)) {
				plugin.thread.getSession().put(uuid, plugin.sesDelay);
			}
		}

		plugin.authList.remove(uuid);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		String uuid = player.getUniqueId().toString();
		if(plugin.loginLocations.containsKey(uuid)) {
			plugin.loginLocations.put(uuid, event.getRespawnLocation());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		String uuid = player.getUniqueId().toString();
		Location from = event.getFrom();
		Location to = event.getTo().clone();

		if (plugin.authList.containsKey(uuid)) {
			to.setX(from.getX());
			to.setZ(from.getZ());
			event.setTo(to);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		String uuid = player.getUniqueId().toString();

		if (plugin.authList.containsKey(uuid)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		String uuid = player.getUniqueId().toString();

		if (plugin.authList.containsKey(uuid)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		String uuid = player.getUniqueId().toString();

		if (plugin.authList.containsKey(uuid)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		String uuid = player.getUniqueId().toString();

		if (plugin.authList.containsKey(uuid)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent chat) {
		Player player = chat.getPlayer();
		String uuid = player.getUniqueId().toString();
		if (plugin.authList.containsKey(uuid)) {
			chat.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onHealthRegain(EntityRegainHealthEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) {
			return;
		}
		Player player = (Player) entity;
		String uuid = player.getUniqueId().toString();

		if (plugin.authList.containsKey(uuid)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) {
			return;
		}
		Player player = (Player) entity;
		String uuid = player.getUniqueId().toString();

		if (plugin.authList.containsKey(uuid)) {
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
		String uuid = player.getUniqueId().toString();

		if (plugin.authList.containsKey(uuid)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		Entity entity = event.getEntity();

		if (entity instanceof Player) {
			Player player = (Player) entity;
			String uuid = player.getUniqueId().toString();
			if (plugin.authList.containsKey(uuid)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		for (LivingEntity entity : event.getAffectedEntities()) {
			if (entity instanceof Player) {
				Player player = (Player) entity;
				String uuid = player.getUniqueId().toString();
				if (plugin.authList.containsKey(uuid)) {
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
			String u1 = p1.getUniqueId().toString();

			if (plugin.authList.containsKey(u1)) {
				event.setCancelled(true);
				return;
			}

			if (damager instanceof Player) {
				Player p2 = (Player) damager;
				String u2 = p2.getUniqueId().toString();

				if (plugin.authList.containsKey(u2)) {
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
			String uuid = player.getUniqueId().toString();

			if (plugin.authList.containsKey(uuid)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
                Player player = event.getPlayer();
		String uuid = player.getUniqueId().toString();

		if (plugin.authList.containsKey(uuid)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String uuid = player.getUniqueId().toString();
		if (plugin.authList.containsKey(uuid)) {
			if (!event.getMessage().startsWith("/login") && !event.getMessage().startsWith("/register")) {
				//faction fix start
				if (event.getMessage().startsWith("/f")) {
					event.setMessage("/" + RandomStringUtils.randomAscii(uuid.length())); //this command does not exist :P
				}		    	//faction fix end
				event.setCancelled(true);
			}
		}
	}
}
