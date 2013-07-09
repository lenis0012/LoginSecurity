package com.lenis0012.bukkit.ls;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class ThreadManager {
	private LoginSecurity plugin;
	private int msg = -1, ses = -1, to = -1;
	private BukkitTask main = null;
	public Map<String, Integer> session = new HashMap<String, Integer>();
	public Map<String, Integer> timeout = new HashMap<String, Integer>();
	private long nextRefresh;
	
	public ThreadManager(LoginSecurity plugin) {
		this.plugin = plugin;
	}
	
	public synchronized Map<String, Integer> getSession(){
		return this.session;
	}
	
	public void startMainTask() {
		this.nextRefresh = System.currentTimeMillis() + 3000000;
		main = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {

			@Override
			public void run() {
				long time = System.currentTimeMillis();
				if(time >= nextRefresh) {
					if(plugin != null) {
						if(plugin.data != null) {
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
		if(this.main != null) {
			this.main.cancel();
			this.main = null;
		}
	}
	
	public void startMsgTask() {
		msg = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				for(Player player : Bukkit.getServer().getOnlinePlayers()) {
					String name = player.getName();
					if(plugin.authList.containsKey(name)) {
						boolean register = plugin.authList.get(name);
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
		msg = -1;
	}
	
	public void startSessionTask() {
		ses = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				Iterator<String> it = getSession().keySet().iterator();
				while(it.hasNext()) {
					String user = it.next();
					int current = getSession().get(user);
					if(current >= 1) {
						current-= 1;
						getSession().put(user, current);
					} else
						it.remove();
				}
			}
		}, 20, 20);
	}
	
	public void stopSessionTask() {
		if(ses >= 0)
			plugin.getServer().getScheduler().cancelTask(ses);
		ses = -1;
	}
	
	public void startTimeoutTask() {
		ses = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				Iterator<String> it = timeout.keySet().iterator();
				while(it.hasNext()) {
					String user = it.next();
					int current = timeout.get(user);
					if(current >= 1) {
						current -= 1;
						timeout.put(user, current);
					} else {
						it.remove();
						Player player = Bukkit.getPlayer(user);
						if(player != null && player.isOnline())
							player.kickPlayer("Login timed out");
					}
				}
			}
		}, 20, 20);
	}
	
	public void stopTimeoutTask() {
		if(to >= 0)
			plugin.getServer().getScheduler().cancelTask(to);
		to = -1;
	}
}
