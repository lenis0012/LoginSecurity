package com.lenis0012.bukkit.loginsecurity.modules.general;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.LoginSecurityConfig;
import com.lenis0012.bukkit.loginsecurity.commands.*;
import com.lenis0012.bukkit.loginsecurity.modules.language.LanguageModule;
import com.lenis0012.pluginutils.modules.Module;
import com.lenis0012.pluginutils.updater.Updater;
import com.lenis0012.pluginutils.updater.UpdaterFactory;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class GeneralModule extends Module<LoginSecurity> {
    private LocationMode locationMode = LocationMode.DEFAULT;
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

        String locationMode = LoginSecurity.getConfiguration().getLocation();
        try {
            this.locationMode = LocationMode.valueOf(locationMode.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger().log(Level.WARNING, "Using unsupported location mode '{0}'. This will do noting.", locationMode);
        }
    }

    @Override
    public void disable() {
    }

    public LocationMode getLocationMode() {
        return locationMode;
    }

    private void setupMetrics() {
        // Create metrics
        final Metrics metrics = new Metrics(plugin, 4637);
        metrics.addCustomChart(new SimplePie("language", () ->
                plugin.getModule(LanguageModule.class).getTranslation().getName()));
    }

    private void setupUpdater() {
        final LoginSecurityConfig config = LoginSecurity.getConfiguration();
        if(config.isUpdaterEnabled()) {
            this.updater = UpdaterFactory.provideBest(plugin, plugin.getInternalClassLoader())
                .getUpdater(plugin);
        } else {
            this.updater = null;
        }
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

    public void checkUpdates(Player player) {
        if(updater != null) {
            updater.notifyIfUpdateAvailable(player);
        }
    }
}
