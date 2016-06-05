package com.lenis0012.bukkit.loginsecurity;

import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import com.google.common.collect.Lists;
import com.lenis0012.bukkit.loginsecurity.modules.general.GeneralModule;
import com.lenis0012.bukkit.loginsecurity.modules.migration.MigrationModule;
import com.lenis0012.bukkit.loginsecurity.modules.storage.StorageModule;
import com.lenis0012.bukkit.loginsecurity.modules.threading.ThreadingModule;
import com.lenis0012.bukkit.loginsecurity.session.SessionManager;
import com.lenis0012.bukkit.loginsecurity.storage.ActionEntry;
import com.lenis0012.bukkit.loginsecurity.storage.Migration;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.pluginutils.PluginHolder;
import com.lenis0012.pluginutils.modules.configuration.ConfigurationModule;

import javax.persistence.PersistenceException;
import java.util.List;
import java.util.logging.Level;

public class LoginSecurity extends PluginHolder {

    /**
     * Get session manager.
     *
     * @return SessionManager
     */
    public static SessionManager getSessionManager() {
        return null;
//        return getInstance().getModule(StorageModule.class).getSessionManager();
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

        // Load database
        try {
            getDatabase().find(PlayerProfile.class).findRowCount();
        } catch(PersistenceException e) {
            getLogger().log(Level.INFO, "Installing database due to first time use...");
            installDDL();
        }

        // Register modules
        registry.registerModules(
                StorageModule.class,
                MigrationModule.class,
                GeneralModule.class,
                ThreadingModule.class);
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
        return list;
    }
}
