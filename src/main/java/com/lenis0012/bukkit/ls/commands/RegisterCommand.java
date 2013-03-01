package com.lenis0012.bukkit.ls.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.lenis0012.bukkit.ls.LoginSecurity;
import com.lenis0012.bukkit.ls.data.ValueType;

public class RegisterCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSecurity plugin = LoginSecurity.instance;
		if(!(sender instanceof Player)) {
			sender.sendMessage("You must be a player");
			return true;
		}
		
		Player player = (Player)sender;
		String name = player.getName();
		
		if(plugin.data.isSet(name)) {
			player.sendMessage(ChatColor.RED+"You are already registered");
			return true;
		}
		if(args.length < 1) {
			player.sendMessage(ChatColor.RED+"Not enough arguments");
			player.sendMessage("Usage: "+cmd.getUsage());
			return true;
		}
		
		String password = plugin.hasher.hash(args[0]);
		plugin.data.setValue(name, ValueType.INSERT, password, plugin.hasher.getTypeId());
		plugin.AuthList.remove(name);
		if(player.hasPotionEffect(PotionEffectType.BLINDNESS) && plugin.blindness)
			player.removePotionEffect(PotionEffectType.BLINDNESS);
		player.sendMessage(ChatColor.GREEN+"Registered with password: "+args[0]);
		return true;
	}
}
