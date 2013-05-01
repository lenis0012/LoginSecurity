package com.lenis0012.bukkit.ls;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.potion.PotionEffectType;

import com.lenis0012.bukkit.ls.encryption.PasswordManager;

public class LoginMessager implements PluginMessageListener {
	private LoginSecurity plugin;
	
	public LoginMessager(LoginSecurity plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] rawMessage) {
		if(channel.equals("LoginSecurity")) {
			try {
				String message = new String(rawMessage, "UTF-8");
				if(message.startsWith("Q_")) {
					this.onQuestionReceive(player, message.substring(2));
				} else if(message.startsWith("A_")) {
					String[] data = message.substring(2).split(" ", 1);
					String question = data[0];
					String answer = data[1];
					this.onAnswerReceive(player, question, answer);
				}
			} catch (UnsupportedEncodingException e) {
				plugin.getLogger().log(Level.SEVERE, "Failed to decode client message", e);
			}
		}
	}
	
	public void onQuestionReceive(Player player, String question) {
		String name = player.getName().toLowerCase();
		if(question.equals("REG")) {
			String registered = String.valueOf(plugin.data.isRegistered(name));
			plugin.sendCustomPayload(player, "A_REG " + registered);
		} else if(question.equals("REQ")) {
			String required = String.valueOf(plugin.required);
			plugin.sendCustomPayload(player, "A_REQ " + required);
		}
	}
	
	public void onAnswerReceive(Player player, String question, String answer) {
		String name = player.getName().toLowerCase();
		if(question.equals("REG")) {
			if(!plugin.data.isRegistered(name)) {
				String pass = plugin.hasher.hash(answer);
				plugin.data.register(name, pass, plugin.hasher.getTypeId(), player.getAddress().getAddress().toString());
				plugin.AuthList.remove(name);
				plugin.thread.timeout.remove(name);
				if(player.hasPotionEffect(PotionEffectType.BLINDNESS))
					player.removePotionEffect(PotionEffectType.BLINDNESS);
			}
		} else if(question.equals("LOGIN")) {
			if(plugin.AuthList.containsKey(name)) {
				if(PasswordManager.checkPass(name, answer)) {
					plugin.AuthList.remove(name);
					plugin.thread.timeout.remove(name);
					if(player.hasPotionEffect(PotionEffectType.BLINDNESS))
						player.removePotionEffect(PotionEffectType.BLINDNESS);
				}
			}
		}
	}
}