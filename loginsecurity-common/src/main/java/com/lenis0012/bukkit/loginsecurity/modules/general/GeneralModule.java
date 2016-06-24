package com.lenis0012.bukkit.loginsecurity.modules.general;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.LoginSecurityConfig;
import com.lenis0012.bukkit.loginsecurity.commands.*;
import com.lenis0012.bukkit.loginsecurity.integrate.autoin.LoginPluginLoginSecurity;
import com.lenis0012.bukkit.loginsecurity.util.Metrics;
import com.lenis0012.bukkit.loginsecurity.util.Metrics.Graph;
import com.lenis0012.bukkit.loginsecurity.util.Metrics.Plotter;
import com.lenis0012.pluginutils.Module;
import com.lenis0012.updater.api.ReleaseType;
import com.lenis0012.updater.api.Updater;
import com.lenis0012.updater.api.UpdaterFactory;
import com.lenis0012.updater.api.Version;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
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
        try {
            setupMetrics();
        } catch(IOException e) {
            // We should probably stay silent actually :P, Don't want to annoy the user with something he can disable.
//            logger().log(Level.WARNING, "Couldn't load metrics", e);
        }

        // This line is so alone :(  I feel bad for him
        this.locationMode = LocationMode.valueOf(LoginSecurity.getConfiguration().getLocation().toUpperCase());

        // AutoIn support
        if(Bukkit.getPluginManager().isPluginEnabled("AutoIn")) {
            plugin.getLogger().log(Level.INFO, "Attempting to hook with AutoIn...");
            LoginPluginLoginSecurity hook = new LoginPluginLoginSecurity(plugin);
            hook.register();
        }
    }

    @Override
    public void disable() {
    }

    public LocationMode getLocationMode() {
        return locationMode;
    }

    private void setupMetrics() throws IOException {
        // Create metrics
        final Metrics metrics = new Metrics(plugin);
        final LoginSecurityConfig config = LoginSecurity.getConfiguration();

        // Algorithm
        Graph algorithm = metrics.createGraph("Algorithm");
        algorithm.addPlotter(new Plotter(config.getHashingAlgorithm().toString()) {
            @Override
            public int getValue() {
                return 1;
            }
        });

        // Start
        metrics.start();
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
        register(new CommandAdmin(plugin), "lac");
    }

    private void registerListeners() {
        logger().log(Level.INFO, "Registering listeners...");
        register(new PlayerListener(this));
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

    public void checkUpdates(final Player player) {
        LoginSecurity.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                if(!updater.hasUpdate()) {
                    return;
                }

                final Version version = updater.getNewVersion();
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                "&bA new &3" + version.getType().toString() + " build for LoginSecurity is available! &3" +
                                        version.getName() + " &afor &9" + version.getServerVersion()));
                    }
                });
            }
        });
    }
}
