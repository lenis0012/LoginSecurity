package com.lenis0012.bukkit.loginsecurity;

import com.lenis0012.bukkit.loginsecurity.database.LoginSecurityDatabase;
import com.lenis0012.bukkit.loginsecurity.modules.captcha.CaptchaManager;
import com.lenis0012.bukkit.loginsecurity.modules.general.GeneralModule;
import com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys;
import com.lenis0012.bukkit.loginsecurity.modules.language.LanguageModule;
import com.lenis0012.bukkit.loginsecurity.modules.language.TranslatedMessage;
import com.lenis0012.bukkit.loginsecurity.modules.storage.NewStorageModule;
import com.lenis0012.bukkit.loginsecurity.modules.threading.ThreadingModule;
import com.lenis0012.bukkit.loginsecurity.session.SessionManager;
import com.lenis0012.bukkit.loginsecurity.util.LoggingFilter;
import com.lenis0012.pluginutils.PluginHolder;
import com.lenis0012.pluginutils.modules.configuration.ConfigurationModule;
import org.apache.logging.log4j.LogManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class LoginSecurity extends PluginHolder {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Get the executor LoginSecurity uses for async processing.
     *
     * @return Executor service.
     */
    public static ExecutorService getExecutorService() {
        return executorService;
    }

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

    public static LoginSecurityDatabase getDatastore() { return ((LoginSecurity) getInstance()).datastore(); }

    /**
     * Translate a message by key.
     *
     * @param key Key to translate
     * @return Translated version by key and selected language
     */
    public static TranslatedMessage translate(LanguageKeys key) {
        return getInstance().getModule(LanguageModule.class).translate(key);
    }

    /**
     * Translate a message by key.
     *
     * @param key Key to translate
     * @return Translated version by key and selected language
     */
    public static TranslatedMessage translate(String key) {
        return getInstance().getModule(LanguageModule.class).translate(key);
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
                LanguageModule.class,
                NewStorageModule.class,
                GeneralModule.class,
                ThreadingModule.class,
                CaptchaManager.class);
    }

    @Override
    public void disable() {
        // Wait for all async processes to complete...
        getLogger().log(Level.INFO, "Waiting for queued tasks...");
        executorService.shutdown();
        getLogger().log(Level.INFO, "ExecutorService shut down, ready to disable.");
    }

    public LoginSecurityConfig config() {
        return config;
    }

    public LoginSecurityDatabase datastore() {
        return getModule(NewStorageModule.class).getDatabase();
    }
}
