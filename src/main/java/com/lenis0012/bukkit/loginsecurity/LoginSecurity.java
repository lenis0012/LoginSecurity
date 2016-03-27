package com.lenis0012.bukkit.loginsecurity;

import com.lenis0012.bukkit.loginsecurity.modules.migration.MigrationModule;
import com.lenis0012.bukkit.loginsecurity.modules.storage.StorageModule;
import com.lenis0012.bukkit.loginsecurity.session.SessionManager;
import com.lenis0012.pluginutils.PluginHolder;
import com.lenis0012.pluginutils.modules.configuration.ConfigurationModule;

public class LoginSecurity extends PluginHolder {

    /**
     * Get session manager.
     *
     * @return SessionManager
     */
    public static SessionManager getSessionManager() {
        return getInstance().getModule(StorageModule.class).getSessionManager();
    }

    /**
     * Get configuration settings.
     *
     * @return Configuration
     */
    public static LoginSecurityConfig getConfiguration() {
        return ((LoginSecurity) getInstance()).config();
    }

    private LoginSecurityConfig config;

    public LoginSecurity() {
        super(ConfigurationModule.class);
    }

    @Override
    public void enable() {
        // Load config
        ConfigurationModule module = getModule(ConfigurationModule.class);
        this.config = module.createCustomConfig(LoginSecurityConfig.class);
        config.reload();
        config.save();

        // Register modules
        registry.registerModules(StorageModule.class, MigrationModule.class);
    }

    @Override
    public void disable() {
    }

    public LoginSecurityConfig config() {
        return config;
    }
}
