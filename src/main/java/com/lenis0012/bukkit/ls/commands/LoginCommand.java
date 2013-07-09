package com.lenis0012.bukkit.ls.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.lenis0012.bukkit.ls.LoginSecurity;
import com.lenis0012.bukkit.ls.encryption.PasswordManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSecurity plugin = LoginSecurity.instance;
		if(!(sender instanceof Player)) {
			sender.sendMessage("You must be a player");
			return true;
		}
		
		Player player = (Player)sender;
		String name = player.getName().toLowerCase();
		
		if(!plugin.authList.containsKey(name)) {
			player.sendMessage(ChatColor.RED+"You are already logged in");
			return true;
		}
		if(!plugin.data.isRegistered(name)) {
			player.sendMessage(ChatColor.RED+"You do not have a password set");
			return true;
		}
		if(args.length < 1) {
			player.sendMessage(ChatColor.RED+"Not enough arguments");
			player.sendMessage("Usage: "+cmd.getUsage());
			return true;
		}
		if(PasswordManager.checkPass(name, args[0])) {
			plugin.authList.remove(name);
			plugin.thread.timeout.remove(name);
			plugin.rehabPlayer(player, name);
			player.sendMessage(ChatColor.GREEN+"Succesfully logged in");
			LoginSecurity.log.log(Level.INFO, "[LoginSecurity] {0} authenticated", player.getName());
			
		//Send data to messager API
		if(plugin.messager)
				plugin.sendCustomPayload(player, "A_PASS " + args[0]);
		} else {
			player.sendMessage(ChatColor.RED+"Invalid password");
			LoginSecurity.log.log(Level.WARNING, "[LoginSecurity] {0} entered an incorrect password", player.getName());
		}
		
		
		
		return true;
	}
}
