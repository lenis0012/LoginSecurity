package com.lenis0012.bukkit.ls.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.lenis0012.bukkit.ls.LoginSecurity;
import java.util.Iterator;
import java.util.logging.Level;
import org.bukkit.GameMode;

public class LogoutCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSecurity plugin = LoginSecurity.instance;
		if(!(sender instanceof Player)) {
			sender.sendMessage("You must be a player");
			return true;
		}
		
		Player player = (Player)sender;
		String name = player.getName().toLowerCase();
		
		if(plugin.AuthList.containsKey(name)) {
			player.sendMessage(ChatColor.RED+"You must login first");
			return true;
		} if(!plugin.data.isRegistered(name)) {
			player.sendMessage(ChatColor.RED+"You are not registered!");
		}
		
		plugin.AuthList.put(name, false);
		plugin.debilitatePlayer(player, name, true);
		// terminate user's current session
		if (plugin.sesUse)
			plugin.thread.getSession().remove(name);
		
			
		player.sendMessage(ChatColor.GREEN+"Succesfully logged out");
		plugin.log.log(Level.INFO, "[LoginSecurity] {0} logged out", player.getName());
		
		//Send data to messager API
		if(plugin.messager)
			plugin.sendCustomPayload(player, "Q_LOGIN");
		return true;
	}
}
