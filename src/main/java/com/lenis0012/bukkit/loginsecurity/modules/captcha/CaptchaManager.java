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
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.lang.reflect.Method;
import java.util.Random;
import java.util.logging.Level;

public class CaptchaManager extends Module<LoginSecurity> implements Listener {
    private final Random random = new Random();
    private MapView view;
    private Method setMapIdMethod;

    private int mapViewId;
    private boolean failedToLoadMapView = false;

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

        // Load set ID method
        try {
            setMapIdMethod = MapMeta.class.getMethod("setMapId", int.class);
            LoginSecurity.getInstance().getLogger().log(Level.INFO, "Using 1.12+ map captcha renderer");
        } catch (Exception e) {
        }

        // Get map view ID
        try {
            for(Method method : MapView.class.getMethods()) {
                if(!method.getName().equals("getId")) continue;
                Object rawMapId = method.invoke(view);
                if(rawMapId instanceof Integer) this.mapViewId = (int) rawMapId;
                else if(rawMapId instanceof Short) this.mapViewId = (int) (short) rawMapId;
                else throw new RuntimeException("Unknown map ID type " + rawMapId.getClass().getName());
            }
        } catch (Exception e) {
            LoginSecurity.getInstance().getLogger().log(Level.WARNING, "Failed to load captcha map", e);
        }
    }

    @Override
    public void disable() {
    }

    public void giveMapItem(Player player, Runnable callback) {
        if(failedToLoadMapView) {
            // Map loader could not be created. =(
            callback.run();
            return;
        }

        ItemStack item = new ItemStack(Material.MAP, 1, (short) mapViewId);
        ItemMeta meta = item.getItemMeta();

        if(setMapIdMethod != null) {
            try {
                setMapIdMethod.invoke(meta, mapViewId);
            } catch (Exception e) {
                LoginSecurity.getInstance().getLogger().log(Level.WARNING, "Failed to set map", e);
            }
        }

        meta.setDisplayName("Captcha [Enter In Chat]");
        item.setItemMeta(meta);
        MetaData.set(player, "ls_captcha_callback", callback);
        MetaData.set(player, "ls_captcha_value", randomCaptcha(5));
        player.setItemInHand(item);

        view.setCenterX(player.getLocation().getBlockX());
        view.setCenterZ(player.getLocation().getBlockZ());
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
