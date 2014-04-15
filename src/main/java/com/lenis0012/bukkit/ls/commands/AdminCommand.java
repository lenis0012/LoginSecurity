package com.lenis0012.bukkit.ls.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.lenis0012.bukkit.ls.LoginSecurity;
import com.lenis0012.bukkit.ls.data.Converter;

public class AdminCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSecurity plugin = LoginSecurity.instance;
		if(!sender.hasPermission("ls.admin")) {
			sender.sendMessage(ChatColor.RED + "You do not have permission!");
			return true;
		}
		
		if(args.length == 0) {
			sender.sendMessage("&7============-{ &4&lL&aoginSecurity &4&lA&admin &4&lC&aommand &7}-============".replaceAll("&", String.valueOf(ChatColor.COLOR_CHAR)));
			sender.sendMessage(ChatColor.GREEN + "/lac rmpass <user>");
			sender.sendMessage(ChatColor.GREEN + "/lac reload");
		} else if(args.length >= 2 && args[0].equalsIgnoreCase("rmpass")) {
			String user = args[1].toLowerCase();
			String uuid = Converter.getUUIDByUsername(user);
			if(uuid != null && !uuid.isEmpty() && plugin.data.isRegistered(uuid)) {
				plugin.data.removeUser(uuid);
				sender.sendMessage(ChatColor.GREEN + "Removed user from accounts database!");
			} else
				sender.sendMessage(ChatColor.RED + "Invalid username");
		} else if(args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
			plugin.reloadConfig();
			sender.sendMessage(ChatColor.GREEN + "Plugin config reloaded!");
		}
		return true;
	}
}