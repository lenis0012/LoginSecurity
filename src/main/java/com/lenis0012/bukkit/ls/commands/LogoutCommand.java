package com.lenis0012.bukkit.ls.commands;

import com.lenis0012.bukkit.ls.Lang;
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
			sender.sendMessage(Lang.MUST_BE_PLAYER.toString());
			return true;
		}

		Player player = (Player) sender;
		String name = player.getName().toLowerCase();

		if (plugin.authList.containsKey(name)) {
			player.sendMessage(Lang.MUST_LGN_FIRST.toString());
			return true;
		}
		if (!plugin.data.isRegistered(name)) {
			player.sendMessage(Lang.NOT_REG.toString());
		}

		plugin.authList.put(name, false);
		plugin.debilitatePlayer(player, name, true);
		// terminate user's current session
		if (plugin.sesUse) {
			plugin.thread.getSession().remove(name);
		}

		player.sendMessage(Lang.LOGOUT.toString());
		LoginSecurity.log.log(Level.INFO, "[LoginSecurity] {0} logged out", player.getName());
		return true;
	}
}
