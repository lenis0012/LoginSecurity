package com.lenis0012.bukkit.ls.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.lenis0012.bukkit.ls.LoginSecurity;
import java.util.logging.Level;

public class RegisterCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSecurity plugin = LoginSecurity.instance;
		if(!(sender instanceof Player)) {
			sender.sendMessage("You must be a player");
			return true;
		}
		
		Player player = (Player)sender;
		String name = player.getName().toLowerCase();
		
		if(plugin.data.isRegistered(name)) {
			player.sendMessage(ChatColor.RED+"You are already registered");
			return true;
		}
		if(args.length < 1) {
			player.sendMessage(ChatColor.RED+"Not enough arguments");
			player.sendMessage("Usage: "+cmd.getUsage());
			return true;
		}
		
		String password = plugin.hasher.hash(args[0]);
		plugin.data.register(name, password, plugin.hasher.getTypeId(), player.getAddress().getAddress().toString());
		plugin.AuthList.remove(name);
		plugin.thread.timeout.remove(name);
		plugin.rehabPlayer(player, name);
		player.sendMessage(ChatColor.GREEN+"Registered with password: "+args[0]);
		plugin.log.log(Level.INFO, "[LoginSecurity] {0} registered sucessfully", name);
		return true;
	}
}
