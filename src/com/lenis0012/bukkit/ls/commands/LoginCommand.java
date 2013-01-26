package com.lenis0012.bukkit.ls.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lenis0012.bukkit.ls.LoginSecurity;
import com.lenis0012.bukkit.ls.util.EncryptionUtil;

public class LoginCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSecurity plugin = LoginSecurity.instance;
		if(!(sender instanceof Player)) {
			sender.sendMessage("You must be a player");
			return true;
		}
		
		Player player = (Player)sender;
		String name = player.getName();
		
		if(!plugin.AuthList.contains(name)) {
			player.sendMessage(ChatColor.RED+"You are already logged in");
			return true;
		}
		if(!plugin.data.isSet(name)) {
			player.sendMessage(ChatColor.RED+"You do not have a password set");
			return true;
		}
		if(args.length < 1) {
			player.sendMessage(ChatColor.RED+"Not enough arguments");
			return true;
		}
		
		String password = EncryptionUtil.getMD5(args[0]);
		if(plugin.data.getValue(name, "password").equals(password)) {
			plugin.AuthList.remove(name);
			player.sendMessage(ChatColor.GREEN+"Succesfully logged in");
		} else {
			player.sendMessage(ChatColor.RED+"Invalid password");
		}
		return true;
	}
}
