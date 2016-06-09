package com.lenis0012.bukkit.loginsecurity;

import com.google.common.collect.Lists;
import com.lenis0012.bukkit.loginsecurity.modules.captcha.CaptchaManager;
import com.lenis0012.bukkit.loginsecurity.modules.general.GeneralModule;
import com.lenis0012.bukkit.loginsecurity.modules.migration.MigrationModule;
import com.lenis0012.bukkit.loginsecurity.modules.storage.StorageModule;
import com.lenis0012.bukkit.loginsecurity.modules.threading.ThreadingModule;
import com.lenis0012.bukkit.loginsecurity.session.SessionManager;
import com.lenis0012.bukkit.loginsecurity.storage.*;
import com.lenis0012.bukkit.loginsecurity.util.LoggingFilter;
import com.lenis0012.pluginutils.PluginHolder;
import com.lenis0012.pluginutils.modules.configuration.ConfigurationModule;
import org.apache.logging.log4j.LogManager;

import java.util.List;

public class LoginSecurity extends PluginHolder {

    /**
     * Get session manager.
     *
     * @return SessionManager
     */
    public static SessionManager getSessionManager() {
        return ((LoginSecurity) getInstance()).sessionManager;
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
    private SessionManager sessionManager;

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

        // Load session manager
        this.sessionManager = new SessionManager();

        // Filter log
        org.apache.logging.log4j.core.Logger consoleLogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
        consoleLogger.addFilter(new LoggingFilter());

        // Register modules
        registry.registerModules(
                StorageModule.class,
                MigrationModule.class,
                GeneralModule.class,
                ThreadingModule.class,
                CaptchaManager.class);
    }

    @Override
    public void disable() {
    }

    public LoginSecurityConfig config() {
        return config;
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = Lists.newArrayList();
        list.add(Migration.class);
        list.add(PlayerProfile.class);
        list.add(ActionEntry.class);
        list.add(PlayerLocation.class);
        list.add(PlayerInventory.class);
        return list;
    }
}
