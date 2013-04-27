package com.lenis0012.bukkit.ls.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lenis0012.bukkit.ls.LoginSecurity;
import com.lenis0012.bukkit.ls.encryption.PasswordManager;

public class RmPassCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSecurity plugin = LoginSecurity.instance;
		if(!(sender instanceof Player)) {
			sender.sendMessage("You must be a player");
			return true;
		}
		
		Player player = (Player)sender;
		String name = player.getName().toLowerCase();
		
		if(!plugin.data.isRegistered(name)) {
			player.sendMessage(ChatColor.RED+"You are not registered on the server");
			return true;
		}
		if(args.length < 1) {
			player.sendMessage(ChatColor.RED+"Not enough arguments");
			player.sendMessage("Usage: "+cmd.getUsage());
			return true;
		}
		if(!PasswordManager.checkPass(name, args[0])) {
			player.sendMessage(ChatColor.RED+"Password Incorrect");
			return true;
		}
		plugin.data.removeUser(name);
		player.sendMessage(ChatColor.GREEN+"Succesfully removed your password");
		return true;
	}
}
