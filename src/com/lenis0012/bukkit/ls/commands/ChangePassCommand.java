package com.lenis0012.bukkit.ls.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lenis0012.bukkit.ls.LoginSecurity;
import com.lenis0012.bukkit.ls.data.ValueType;
import com.lenis0012.bukkit.ls.util.EncryptionUtil;

public class ChangePassCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSecurity plugin = LoginSecurity.instance;
		if(!(sender instanceof Player)) {
			sender.sendMessage("You must be a player");
			return true;
		}
		
		Player player = (Player)sender;
		String name = player.getName();
		
		if(!plugin.data.isSet(name)) {
			player.sendMessage(ChatColor.RED+"You are not registered on the server");
			return true;
		}
		if(args.length < 2) {
			player.sendMessage(ChatColor.RED+"Not enough arguments");
			player.sendMessage("Usage: "+cmd.getUsage());
			return true;
		}
		
		String oldPassA = EncryptionUtil.getMD5(args[0]);
		String oldPassB = (String)plugin.data.getValue(name, "password");
		String newPass = EncryptionUtil.getMD5(args[1]);
		
		if(!oldPassA.equals(oldPassB)) {
			player.sendMessage(ChatColor.RED+"Password Incorrect");
			return true;
		}
		
		plugin.data.setValue(name, ValueType.UPDATE, newPass);
		player.sendMessage(ChatColor.GREEN+"Succesfully changed password to: "+args[1]);
		return true;
	}
}
