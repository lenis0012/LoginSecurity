package com.lenis0012.bukkit.loginsecurity.modules.captcha;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.util.MetaData;
import com.lenis0012.pluginutils.Module;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.util.Random;

public class CaptchaManager extends Module<LoginSecurity> implements Listener {
    private final Random random = new Random();
    private MapView view;

    public CaptchaManager(LoginSecurity plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        this.view = Bukkit.createMap(Bukkit.getWorlds().get(0));
        for(MapRenderer renderer : view.getRenderers()) {
            view.removeRenderer(renderer);
        }
        view.addRenderer(new CaptchaRenderer());
        register(this);
    }

    @Override
    public void disable() {
    }

    public void giveMapItem(Player player, Runnable callback) {
        ItemStack item = new ItemStack(Material.MAP, 1, view.getId());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Captcha [Enter In Chat]");
        item.setItemMeta(meta);
        MetaData.set(player, "ls_captcha_callback", callback);
        MetaData.set(player, "ls_captcha_value", randomCaptcha(5));
        player.setItemInHand(item);
        player.sendMap(view);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        MetaData.unset(player, "ls_captcha_value");
        MetaData.unset(player, "ls_captcha_callback");
        MetaData.unset(player, "ls_captcha_set");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        if(!MetaData.has(player, "ls_captcha_callback")) {
            return;
        }

        String captcha = MetaData.get(player, "ls_captcha_value", String.class);
        if(!event.getMessage().trim().equalsIgnoreCase(captcha)) {
            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    player.kickPlayer("Wrong captcha! Please try again.");
                }
            });
            return;
        }

        Runnable callback = MetaData.get(player, "ls_captcha_callback", Runnable.class);
        MetaData.unset(player, "ls_captcha_callback");
        MetaData.unset(player, "ls_captcha_value");
        player.getInventory().remove(Material.MAP);
        callback.run();
    }

    private String randomCaptcha(int length) {
        final String sheet = "ABCDEFHIJKLMNOPQRSTUVWXYZ123456789";
        final StringBuilder builder = new StringBuilder();
        for(int i = 0; i < length; i++) {
            builder.append(sheet.charAt(random.nextInt(sheet.length())));
        }
        return builder.toString();
    }
}
