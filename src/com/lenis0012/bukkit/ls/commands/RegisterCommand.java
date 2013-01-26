package com.lenis0012.bukkit.ls.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lenis0012.bukkit.ls.LoginSecurity;
import com.lenis0012.bukkit.ls.data.ValueType;
import com.lenis0012.bukkit.ls.util.EncryptionUtil;

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
			return true;
		}
		
		String password = EncryptionUtil.getMD5(args[0]);
		plugin.data.setValue(name, ValueType.INSERT, password);
		player.sendMessage(ChatColor.GREEN+"Registered with password: "+args[0]);
		return true;
	}
}
