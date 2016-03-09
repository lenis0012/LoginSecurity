package com.lenis0012.bukkit.ls.commands;

import java.util.UUID;

import com.lenis0012.updater.api.Updater;
import com.lenis0012.updater.api.Version;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.google.common.base.Charsets;
import com.lenis0012.bukkit.ls.LoginSecurity;
import com.lenis0012.bukkit.ls.util.UUIDFetcher;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AdminCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
		final LoginSecurity plugin = LoginSecurity.instance;
		if(!sender.hasPermission("ls.admin")) {
			sender.sendMessage(ChatColor.RED + "You do not have permission!");
			return true;
		}
		
		try {
			if(args.length == 0) {
				sender.sendMessage("&7==========-{ &4&lL&aoginSecurity &4&lA&admin &4&lC&aommand &7}-==========".replaceAll("&", String.valueOf(ChatColor.COLOR_CHAR)));
				sender.sendMessage(ChatColor.GREEN + "/lac rmpass <user>");
				sender.sendMessage(ChatColor.GREEN + "/lac reload");
			} else if(args.length >= 2 && args[0].equalsIgnoreCase("rmpass")) {
				String user = args[1].toLowerCase();
				String uuid = Bukkit.getOfflinePlayer(user).getUniqueId().toString().replace("-", "");
				if(uuid != null && !uuid.isEmpty() && plugin.data.isRegistered(uuid)) {
					plugin.data.removeUser(uuid);
					sender.sendMessage(ChatColor.GREEN + "Removed user from accounts database!");
				} else {
					sender.sendMessage(ChatColor.RED + "Invalid username");
				}
			} else if(args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
				plugin.reloadConfig();
				sender.sendMessage(ChatColor.GREEN + "Plugin config reloaded!");
			} else if(args.length >= 1 && args[0].equalsIgnoreCase("update")) {
				final Updater updater = plugin.getUpdater();
				final Version version = updater.getNewVersion();
				if(version == null) {
					sender.sendMessage(ChatColor.RED + "Updater is disabled!");
					return true;
				}

				reply(sender, "&aDownloading " + version.getName() + "...");
				Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
					@Override
					public void run() {
						String message = updater.downloadVersion();
						final String response = message == null ? "&aUpdate successful, will be active on reboot." : "&c&lError: &c" + message;
						Bukkit.getScheduler().runTask(plugin, new Runnable() {
							@Override
							public void run() {
								reply(sender, response);
								if(!(sender instanceof Player)) {
									return;
								}

								Player player = (Player) sender;
								ItemStack changelog = updater.getChangelog();
								if(changelog == null) {
//									reply(sender, "&cChangelog isn't available for this version.");
									return;
								}

								ItemStack inHand = player.getInventory().getItemInMainHand();
								player.getInventory().setItemInMainHand(changelog);
								if(inHand != null) {
									player.getInventory().addItem(inHand);
								}

								reply(player, "&llenis> &bCheck my changelog out! (I put it in your hand)");
								player.updateInventory();
							}
						});
					}
				});
			}
			
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	private void reply(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}
}