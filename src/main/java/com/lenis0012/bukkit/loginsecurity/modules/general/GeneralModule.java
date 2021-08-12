package com.lenis0012.bukkit.loginsecurity.modules.general;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.LoginSecurityConfig;
import com.lenis0012.bukkit.loginsecurity.commands.*;
import com.lenis0012.bukkit.loginsecurity.modules.language.LanguageModule;
import com.lenis0012.pluginutils.Module;
import com.lenis0012.updater.api.ReleaseType;
import com.lenis0012.updater.api.Updater;
import com.lenis0012.updater.api.UpdaterFactory;
import com.lenis0012.updater.api.Version;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class GeneralModule extends Module<LoginSecurity> {
    private LocationMode locationMode;
    private Updater updater;

    public GeneralModule(LoginSecurity plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        registerCommands();
        registerListeners();
        setupUpdater();
        setupMetrics();

        // This line is so alone :(  I feel bad for him
        this.locationMode = LocationMode.valueOf(LoginSecurity.getConfiguration().getLocation().toUpperCase());
    }

    @Override
    public void disable() {
    }

    public LocationMode getLocationMode() {
        return locationMode;
    }

    private void setupMetrics() {
        // Create metrics
        final Metrics metrics = new Metrics(plugin);
        metrics.addCustomChart(new Metrics.SimplePie("language", () ->
                plugin.getModule(LanguageModule.class).getTranslation().getName()));
    }

    private void setupUpdater() {
        final UpdaterFactory factory = new UpdaterFactory(plugin);
        final LoginSecurityConfig config = LoginSecurity.getConfiguration();
        this.updater = factory.newUpdater(getPluginFile(), config.isUpdaterEnabled());
        updater.setChannel(ReleaseType.valueOf(config.getUpdaterChannel().toUpperCase()));
    }

    private void registerCommands() {
        logger().log(Level.INFO, "Registering commands...");
        register(new CommandLogin(plugin), "login");
        register(new CommandRegister(plugin), "register");
        register(new CommandChangePass(plugin), "changepassword");
        register(new CommandLogout(plugin), "logout");
        register(new CommandUnregister(plugin), "unregister");
        register(new CommandAdmin(plugin), "lac");
    }

    private void registerListeners() {
        logger().log(Level.INFO, "Registering listeners...");
        register(new PlayerListener(this));

        if(Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            InventoryPacketListener.register(plugin);
        }
    }

    private File getPluginFile() {
        try {
            Method method = JavaPlugin.class.getDeclaredMethod("getFile");
            method.setAccessible(true);
            return (File) method.invoke(plugin);
        } catch(Exception e) {
            throw new RuntimeException("Couldn't get plugin file", e);
        }
    }

    public Updater getUpdater() {
        return updater;
    }

    public void checkUpdates(final Player player) {
        LoginSecurity.getExecutorService().execute(() -> {
            if(!updater.hasUpdate()) {
                return;
            }

            final Version version = updater.getNewVersion();
            if(version == null || version.getType() == null) {
                logger().log(Level.WARNING, "Updater was in unexpected state, please report on https://github.com/lenis0012/LoginSecurity-2/issues");
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&bA new &3" + version.getType().toString() + " &bbuild for LoginSecurity is available! &3" +
                                version.getName() + " &bfor &3" + version.getServerVersion()));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&bUse &3/lac update &bto download the new version."));
            });
        });
    }
}
