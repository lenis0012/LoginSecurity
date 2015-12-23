package com.lenis0012.bukkit.ls.commands;

import com.lenis0012.bukkit.ls.Lang;
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
			sender.sendMessage(Lang.MUST_BE_PLAYER.toString());
			return true;
		}
		
		Player player = (Player)sender;
		String uuid = player.getUniqueId().toString().replaceAll("-", "");
		
		if(!plugin.data.isRegistered(uuid)) {
			player.sendMessage(Lang.NOT_REG.toString());
			return true;
		}if(args.length < 1) {
			player.sendMessage(Lang.INVALID_ARGS.toString());
			player.sendMessage(Lang.USAGE + cmd.getUsage());
			return true;
		} if(!PasswordManager.checkPass(uuid, args[0])) {
			player.sendMessage(Lang.INVALID_PSW.toString());
			return true;
		} if(plugin.required) {
			player.sendMessage(Lang.REQUIRED_PSW.toString());
			return true;
		}
		
		plugin.data.removeUser(uuid);
		player.sendMessage(Lang.REMOVED_PSW.toString());
		return true;
	}
}
