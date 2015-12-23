package com.lenis0012.bukkit.ls.commands;

import com.lenis0012.bukkit.ls.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lenis0012.bukkit.ls.LoginSecurity;
import java.util.logging.Level;

public class RegisterCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSecurity plugin = LoginSecurity.instance;
		if (!(sender instanceof Player)) {
			sender.sendMessage(Lang.MUST_BE_PLAYER.toString());
			return true;
		}

		Player player = (Player) sender;
		String uuid = player.getUniqueId().toString().replaceAll("-", "");
		String name = player.getName().toLowerCase();

		if (plugin.data.isRegistered(uuid)) {
			player.sendMessage(Lang.ALREADY_REG.toString());
			return true;
		}
		if (args.length < 1) {
			player.sendMessage(Lang.INVALID_ARGS.toString());
			player.sendMessage(Lang.USAGE + cmd.getUsage());
			return true;
		}

		String password = plugin.hasher.hash(args[0]);
		plugin.data.register(uuid, password, plugin.hasher.getTypeId(), player.getAddress().getAddress().toString());
		plugin.authList.remove(name);
		plugin.thread.timeout.remove(name);
		plugin.rehabPlayer(player, name);
		player.sendMessage(Lang.REGISTERED.toString());
		LoginSecurity.log.log(Level.INFO, "[LoginSecurity] {0} registered sucessfully", player.getName());
		return true;
	}
}
