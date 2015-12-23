package com.lenis0012.bukkit.ls.commands;

import com.lenis0012.bukkit.ls.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lenis0012.bukkit.ls.LoginSecurity;
import com.lenis0012.bukkit.ls.encryption.PasswordManager;
import java.util.logging.Level;

public class ChangePassCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSecurity plugin = LoginSecurity.instance;
		if (!(sender instanceof Player)) {
			sender.sendMessage(Lang.MUST_BE_PLAYER.toString());
			return true;
		}

		Player player = (Player) sender;
		String uuid = player.getUniqueId().toString().replaceAll("-", "");

		if (!plugin.data.isRegistered(uuid)) {
			player.sendMessage(Lang.NOT_REG.toString());
			return true;
		}
		if (args.length < 2) {
			player.sendMessage(Lang.INVALID_ARGS.toString());
			player.sendMessage(Lang.USAGE + cmd.getUsage());
			return true;
		}
		if (!PasswordManager.checkPass(uuid, args[0])) {
			player.sendMessage(Lang.INVALID_PSW.toString());
			LoginSecurity.log.log(Level.WARNING, "[LoginSecurity] {0} failed to change password", player.getName());
			return true;
		}

		String newPass = plugin.hasher.hash(args[1]);
		plugin.data.updatePassword(uuid, newPass, plugin.hasher.getTypeId());
		player.sendMessage(Lang.PSW_CHANGED.toString());
		LoginSecurity.log.log(Level.INFO, "[LoginSecurity] {0} sucessfully changed password", player.getName());

		return true;
	}
}
