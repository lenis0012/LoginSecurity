package com.lenis0012.bukkit.ls.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lenis0012.bukkit.ls.LoginSecurity;
import java.util.logging.Level;

public class LogoutCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSecurity plugin = LoginSecurity.instance;
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player");
			return true;
		}

		Player player = (Player) sender;
		String uuid = player.getUniqueId().toString();

		if (plugin.authList.containsKey(uuid)) {
			player.sendMessage(ChatColor.RED + "You must login first");
			return true;
		}
		if (!plugin.data.isRegistered(uuid)) {
			player.sendMessage(ChatColor.RED + "You are not registered!");
		}

		plugin.authList.put(uuid, false);
		plugin.debilitatePlayer(player, uuid, true);
		// terminate user's current session
		if (plugin.sesUse) {
			plugin.thread.getSession().remove(uuid);
		}

		player.sendMessage(ChatColor.GREEN + "Succesfully logged out");
		LoginSecurity.log.log(Level.INFO, "[LoginSecurity] {0} logged out", player.getName());
		return true;
	}
}
