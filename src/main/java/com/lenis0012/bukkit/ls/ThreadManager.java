package com.lenis0012.bukkit.ls;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class ThreadManager {

	private LoginSecurity plugin;
	private BukkitTask msg;
	private BukkitTask ses;
	private BukkitTask to;
	private BukkitTask main = null;
	public Map<String, Integer> session = new HashMap<String, Integer>();
	public Map<String, Integer> timeout = new HashMap<String, Integer>();
	private long nextRefresh;

	public ThreadManager(LoginSecurity plugin) {
		this.plugin = plugin;
	}

	public synchronized Map<String, Integer> getSession() {
		return this.session;
	}

	public void startMainTask() {
		this.nextRefresh = System.currentTimeMillis() + 3000000;
		main = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
			@Override
			public void run() {
				long time = System.currentTimeMillis();
				if (time >= nextRefresh) {
					if (plugin != null) {
						if (plugin.data != null) {
							plugin.data.closeConnection();
							plugin.data.openConnection();
						}
					}

					nextRefresh = System.currentTimeMillis() + 3000000;
				}
			}
		}, 20, 20);
	}

	public void stopMainTask() {
		if (this.main != null) {
			this.main.cancel();
			this.main = null;
		}
	}

	public void startMsgTask() {
		msg = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
			public void run() {
				for (Player player : Bukkit.getServer().getOnlinePlayers()) {
					String uuid = player.getUniqueId().toString();
					if (plugin.authList.containsKey(uuid)) {
						boolean register = plugin.authList.get(uuid);
						if (register) {
							player.sendMessage(ChatColor.RED + "Please register using /register <password>");
						} else {
							player.sendMessage(ChatColor.RED + "Please login using /login <password>");
						}
					}
				}
			}
		}, 200L, 200L);
	}

	public void stopMsgTask() {
		if (msg != null) {
			msg.cancel();
		}
		
		msg = null;
	}

	public void startSessionTask() {
		ses = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
			public void run() {
				Iterator<String> it = getSession().keySet().iterator();
				while (it.hasNext()) {
					String user = it.next();
					int current = getSession().get(user);
					if (current >= 1) {
						current -= 1;
						getSession().put(user, current);
					} else {
						it.remove();
					}
				}
			}
		}, 20, 20);
	}

	public void stopSessionTask() {
		if (ses != null) {
			ses.cancel();
		}
		
		ses = null;
	}

	public void startTimeoutTask() {
		to = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
			@Override
			public void run() {
				Iterator<String> it = timeout.keySet().iterator();
				while (it.hasNext()) {
					String user = it.next();
					int current = timeout.get(user);
					if (current >= 1) {
						current -= 1;
						timeout.put(user, current);
					} else {
						it.remove();
						Player player = Bukkit.getPlayer(UUID.fromString(user));
						if (player != null && player.isOnline()) {
							// teleport the player before kicking so that his location is not lost
							if (plugin.spawntp) {
								if (plugin.loginLocations.containsKey(user)) {
									Location fixedLocation = plugin.loginLocations.remove(user);
									fixedLocation.add(0, 0.2, 0); // fix for players falling into ground
									player.teleport(fixedLocation);
								}
							}
							player.kickPlayer("Login timed out");
							LoginSecurity.log.log(Level.INFO, "{0} was kicked for login timeout", player.getName());
						}
					}
				}
			}
		}, 20, 20);
	}

	public void stopTimeoutTask() {
		if (to != null) {
			to.cancel();
		}
		
		to = null;
	}
}
